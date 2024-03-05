package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.example.messengerapp.databinding.ActivityMainBinding
import com.example.messengerapp.Fragments.ChatsFragment
import com.example.messengerapp.Fragments.SearchFragment
import com.example.messengerapp.Fragments.SettingsFragment
import com.example.messengerapp.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = ""

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager = binding.viewPager

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        // Add fragments to the adapter
        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchFragment(), "Search")
        viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

        viewPager.adapter = viewPagerAdapter  // Set the adapter to the ViewPager
        tabLayout.setupWithViewPager(viewPager)

        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)
                    binding.username.text = user!!.getUsername()
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile).into(binding.profileimage)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle onCancelled event
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionLogout -> {
                // Sign out from Firebase Authentication
                FirebaseAuth.getInstance().signOut()

                // Sign out from Google sign-in
                val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
                googleSignInClient.signOut().addOnCompleteListener {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}
