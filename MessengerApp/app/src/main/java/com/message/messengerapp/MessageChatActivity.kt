package com.message.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.message.messengerapp.AdapterClasses.ChatsAdapter
import com.message.messengerapp.ModelClasses.Chat
import com.message.messengerapp.ModelClasses.Users
import com.message.messengerapp.databinding.ActivityMessageChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.message.messengerapp.Fragments.APIService
import com.message.messengerapp.Notification.Client
import com.message.messengerapp.Notification.Data
import com.message.messengerapp.Notification.MyResponse
import com.message.messengerapp.Notification.Sender
import com.message.messengerapp.Notification.Token
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// MessageChatActivity class manages the chat interface between users
class MessageChatActivity : AppCompatActivity() {

    private var userIdVisit: String = ""
    private var firebaseUser: FirebaseUser? = null

    private var chatsAdapter: ChatsAdapter? = null
    private var mChatList: MutableList<Chat>? = null
    private lateinit var recycler_view_chats: RecyclerView
    private var reference: DatabaseReference? = null

    private var notify = false
    private var apiService: APIService? = null

    private lateinit var binding: ActivityMessageChatBinding
    private var seenListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        userIdVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = binding.recycleViewChats
        recycler_view_chats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                binding.usernameMchat.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(binding.profileImageChat)
                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle cancelled event
            }
        })

        binding.sendMessageBtn.setOnClickListener {
            notify = true
            val message = binding.textMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            binding.textMessage.setText("")
        }

        binding.attachImageFile.setOnClickListener {
            notify = true
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        seenMessage(userIdVisit)
    }

    // Retrieve chat messages between users from the database
    private fun retrieveMessages(senderId: String?, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mChatList?.clear()
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat != null && (chat.getReceiver() == senderId && chat.getSender() == receiverId
                                || chat.getReceiver() == receiverId && chat.getSender() == senderId)) {
                        mChatList?.add(chat)
                    }
                }
                chatsAdapter = ChatsAdapter(this@MessageChatActivity, mChatList!!, receiverImageUrl!!)
                recycler_view_chats.adapter = chatsAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    // Send a message to the user
    private fun sendMessageToUser(senderId: String, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageId = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageId

        reference.child("Chats").child(messageId!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListReference = FirebaseDatabase.getInstance().reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatListReference.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatListReference.child("id").setValue(userIdVisit)

                                val chatListReceiverRef = FirebaseDatabase.getInstance().reference
                                    .child("ChatList")
                                    .child(userIdVisit)
                                    .child(firebaseUser!!.uid)
                                chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user =p0.getValue(Users::class.java)
                if(notify){
                    sendNotification(receiverId, user!!.getUsername(),message)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle error
            }
        })
    }

    // Send a notification to the user
    private fun sendNotification(receiverId: String?, username: String?, message: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher,"$username: $message","New Message",userIdVisit)
                    val sender = Sender(data!!,token!!.retrieveToken().toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()!!.success !== 1) {
                                        Toast.makeText(
                                            this@MessageChatActivity,
                                            "Failed , Nothing Happened",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                // Handle failure
                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle cancelled event
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is uploading, please wait.....")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            val uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@continueWithTask filePath.downloadUrl
            }.addOnCompleteListener { task ->
                val downloadUrl = task.result
                val url = downloadUrl.toString()

                val messageHashMap = HashMap<String, Any?>()
                messageHashMap["sender"] = firebaseUser!!.uid
                messageHashMap["message"] = "sent you an image"
                messageHashMap["receiver"] = userIdVisit
                messageHashMap["isSeen"] = false
                messageHashMap["url"] = url
                messageHashMap["messageId"] = messageId

                ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            progressBar.dismiss()
                            val reference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
                            reference.addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(p0: DataSnapshot) {
                                    val user =p0.getValue(Users::class.java)
                                    if(notify) {
                                        sendNotification(userIdVisit, user!!.getUsername(),"sent you an image")
                                    }
                                    notify = false
                                }

                                override fun onCancelled(p0: DatabaseError) {
                                    // Handle error
                                }
                            })
                        }
                    }
            }
        }
    }

    // Listen for message seen status changes
    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender().equals(userId)) {
                        val hashMap = HashMap<String,Any>()
                        hashMap["isSeen"]  = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference?.removeEventListener(seenListener!!)
    }
}

