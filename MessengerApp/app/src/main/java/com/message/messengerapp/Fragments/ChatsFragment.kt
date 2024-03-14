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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {
    private var mUsers: MutableList<Users>? = null
    private var userAdapter: UserAdapter? = null
    private var usersChatList: MutableList<ChatList>? = null
    lateinit var recycler_view_chatlist: RecyclerView
    private var firebaseUser: FirebaseUser? = null

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

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatsFragment", "ChatList onDataChange called")
                usersChatList?.clear()

                for (dataSnapshot in snapshot.children) {
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    chatList?.let { usersChatList?.add(it) }
                }
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsFragment", "ChatList onCancelled: ${error.message}")
            }
        })

        return view
    }

    private fun retrieveChatList() {
        Log.d("ChatsFragment", "retrieveChatList called")
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatsFragment", "Users onDataChange called")
                mUsers?.clear()

                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    for (eachChatList in usersChatList!!) {
                        if (user?.getUid() == eachChatList.getId()) {
                            user?.let { mUsers?.add(it) }
                        }
                    }
                }
                if (userAdapter == null) {
                    userAdapter = UserAdapter(context!!, mUsers ?: mutableListOf(), true)
                    recycler_view_chatlist.adapter = userAdapter
                } else {
                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatsFragment", "Users onCancelled: ${error.message}")
            }
        })
    }
}
