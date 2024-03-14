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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private  var searchEditText: EditText? = null
    private var recyclerView: RecyclerView? = null

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
        retrieveAllUsers()


        searchEditText!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented
            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(cs.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    searchForUsers(s.toString())
                }
            }
        })

        return view
    }

    private fun retrieveAllUsers() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        firebaseUserID?.let { uid ->
            val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
            refUsers.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (mUsers as ArrayList<Users>).clear()
                   if(searchEditText!!.text.toString() == ""){
                       for (snapshot in dataSnapshot.children) {
                           val user: Users? = snapshot.getValue(Users::class.java)
                           if (user != null && user.getUid() != uid) {
                               (mUsers as ArrayList<Users>).add(user)
                           }
                       }
                       context?.let {
                           userAdapter = UserAdapter(it, mUsers!!, false)
                           recyclerView!!.adapter = userAdapter
                       }

                   }                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }

    private fun searchForUsers(str: String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        firebaseUserID?.let { uid ->
            val queryUsers = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("search")
                .startAt(str)
                .endAt(str + "\uf8ff")

            queryUsers.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (mUsers as ArrayList<Users>).clear()
                    for (p0Child in dataSnapshot.children) {
                        val user: Users? = p0Child.getValue(Users::class.java)
                        if (user != null && user.getUid() != uid) {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                    context?.let {
                        userAdapter = UserAdapter(it, mUsers!!, false)
                        recyclerView!!.adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }
}
