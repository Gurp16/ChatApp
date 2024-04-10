package com.message.messengerapp.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.message.messengerapp.AdapterClasses.UserAdapter
import com.message.messengerapp.ModelClasses.ChatList
import com.message.messengerapp.ModelClasses.Users
import com.message.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import com.google.firebase.messaging.FirebaseMessaging
import com.message.messengerapp.Notification.Token

class ChatsFragment : Fragment() {
    private var mUsers: MutableList<Users>? = null // List of users
    private var userAdapter: UserAdapter? = null // Adapter for user list
    private var usersChatList: MutableList<ChatList>? = null // List of user chat lists
    lateinit var recycler_view_chatlist: RecyclerView // RecyclerView for chat list
    private var firebaseUser: FirebaseUser? = null // Current Firebase user
    private lateinit var chatMessagesRef: DatabaseReference // Reference to chat messages
    // Child event listener to listen for changes in chat messages
    private val childEventListener: ChildEventListener by lazy {
        object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Update the chat list when a new chat message is added
                updateChatList()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle chat message updates if necessary
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle chat message removals if necessary
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle chat message movements if necessary
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsFragment", "Chat messages onCancelled: ${error.message}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler_view_chatlist = view.findViewById(R.id.recycle_view_chatlist)
        recycler_view_chatlist.setHasFixedSize(true)
        recycler_view_chatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()
        mUsers = mutableListOf()

        // Initialize chat messages reference
        chatMessagesRef = FirebaseDatabase.getInstance().reference.child("Chats")

        // Update token
        updateToken()

        return view
    }

    override fun onResume() {
        super.onResume()

        // Add the child event listener
        chatMessagesRef.addChildEventListener(childEventListener)

        // Fetch initial data from ChatList node
        val chatListRef = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        chatListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersChatList?.clear()

                for (dataSnapshot in snapshot.children) {
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    chatList?.let { usersChatList?.add(it) }
                }
                // After fetching ChatList data, retrieve user data
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsFragment", "ChatList onCancelled: ${error.message}")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        // Remove the child event listener
        chatMessagesRef.removeEventListener(childEventListener)
    }

    // Function to update FCM token for push notifications
    private fun updateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                token?.let {
                    val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
                    firebaseUser?.uid?.let { userId ->
                        ref.child(userId).setValue(Token(it)).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("ChatsFragment", "Token updated successfully")
                            } else {
                                Log.e("ChatsFragment", "Failed to update token", updateTask.exception)
                            }
                        }
                    }
                }
            } else {
                Log.e("ChatsFragment", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    // Function to retrieve chat list
    private fun retrieveChatList() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers?.clear()

                val matchedUsers = mutableListOf<Users>()

                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    for (eachChatList in usersChatList.orEmpty()) {
                        val chatListId = eachChatList.getId()
                        if (user?.getUid() == chatListId) {
                            user?.let { matchedUsers.add(it) }
                            Log.d("ChatsFragment", "User added to matchedUsers: ${user?.getUsername()}")
                        }
                    }
                }

                mUsers?.addAll(matchedUsers)
                setUpAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsFragment", "Users onCancelled: ${error.message}")
            }
        })
    }

    // Function to set up adapter for RecyclerView
    private fun setUpAdapter() {
        if (userAdapter == null) {
            userAdapter = UserAdapter(context!!, mUsers ?: mutableListOf(), true)
            recycler_view_chatlist.adapter = userAdapter
            Log.d("ChatsFragment", "UserAdapter set")
        } else {
            userAdapter?.notifyDataSetChanged()
            Log.d("ChatsFragment", "UserAdapter notified data set changed")
        }
    }

    // Function to update chat list
    private fun updateChatList() {
        retrieveChatList()
    }
}
