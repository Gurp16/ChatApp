package com.example.messengerapp

import com.example.messengerapp.WelcomeActivity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.messengerapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        binding.registerbtn.setOnClickListener {
            registerUser()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun registerUser() {
        val username: String = binding.usernameregister.text.toString()
        val email: String = binding.emailregister.text.toString()
        val password: String = binding.passwordregister.text.toString()

        if (username.isBlank()) {
            Toast.makeText(this@RegisterActivity, "Please write username", Toast.LENGTH_LONG).show()
        } else if (email.isBlank()) {
            Toast.makeText(this@RegisterActivity, "Please write email", Toast.LENGTH_LONG).show()
        } else if (password.isBlank()) {
            Toast.makeText(this@RegisterActivity, "Please write password", Toast.LENGTH_LONG).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)
                        val userHashMap = HashMap<String,Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-dc6dd.appspot.com/o/Profile.png?alt=media&token=1ad4ac3e-16d9-47d0-8434-46cbb00b278d"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-dc6dd.appspot.com/o/cover.jpeg?alt=media&token=57ebab5b-a696-4051-8d2e-0e629a9c2c8d"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "http://m.faceboook.com"
                        userHashMap["instagram"] = "http://m.instagram.com"
                        userHashMap["website"] = " https://www.google.com "

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@RegisterActivity, "Registration Failed: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration Failed: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
