package com.message.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import com.squareup.picasso.Picasso
class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null

    var chatsAdapter: ChatsAdapter? = null
    var mChatList: MutableList<Chat>? = null
    lateinit var recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null

    private lateinit var binding: ActivityMessageChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                // Corrected binding ID for profile image
                Picasso.get().load(user.getProfile()).into(binding.profileImageChat)
                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle cancelled event
            }
        })

        binding.sendMessageBtn.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            binding.textMessage.setText("")
        }

        binding.attachImageFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        seenMessage(userIdVisit)
    }

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
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })

                    val chatListReceiverRef = FirebaseDatabase.getInstance().reference
                        .child("ChatList")
                        .child(userIdVisit)
                        .child(firebaseUser!!.uid)
                    chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                }
            }
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
                progressBar.dismiss()
            }
        }
    }
    var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot in snapshot.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender().equals(userId))
                    {
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