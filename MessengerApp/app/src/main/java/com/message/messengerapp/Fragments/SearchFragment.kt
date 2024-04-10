package com.message.messengerapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.message.messengerapp.AdapterClasses.UserAdapter
import com.message.messengerapp.ModelClasses.Users
import com.message.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null // Adapter for displaying users
    private var mUsers: MutableList<Users>? = null // List of users
    private var searchEditText: EditText? = null // EditText for user search
    private var recyclerView: RecyclerView? = null // RecyclerView for displaying search results

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.searchist)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        searchEditText = view.findViewById(R.id.searchUserSET)

        mUsers = ArrayList()
        retrieveAllUsers() // Retrieve all users from the database

        // Add a TextWatcher to search for users as the user types in the searchEditText
        searchEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(cs.toString().toLowerCase()) // Search for users based on input text
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    // Function to retrieve all users from the database
    private fun retrieveAllUsers() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        firebaseUserID?.let { uid ->
            val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
            refUsers.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mUsers?.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        user?.let {
                            if (it.getUid() != uid) {
                                mUsers?.add(it)
                            }
                        }
                    }
                    context?.let {
                        userAdapter = UserAdapter(it, mUsers ?: mutableListOf(), false)
                        recyclerView?.adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }

    // Function to search for users based on input text
    private fun searchForUsers(str: String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        firebaseUserID?.let { uid ->
            val queryUsers = FirebaseDatabase.getInstance().reference.child("Users")
                .orderByChild("search")
                .startAt(str)
                .endAt(str + "\uf8ff")

            queryUsers.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mUsers?.clear()
                    for (p0Child in dataSnapshot.children) {
                        val user: Users? = p0Child.getValue(Users::class.java)
                        user?.let {
                            if (it.getUid() != uid) {
                                mUsers?.add(it)
                            }
                        }
                    }
                    context?.let {
                        userAdapter = UserAdapter(it, mUsers ?: mutableListOf(), false)
                        recyclerView?.adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }
}
