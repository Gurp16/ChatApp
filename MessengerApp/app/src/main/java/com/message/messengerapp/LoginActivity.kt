package com.message.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.message.messengerapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

// Activity responsible for user login
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back navigation from the toolbar
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance()

        // Set onClickListener for the login button
        binding.loginbtn.setOnClickListener {
            loginUser()
        }
    }

    // Method to log in user
    private fun loginUser() {
        val email: String = binding.emaillogin.text.toString()
        val password: String = binding.passwordlogin.text.toString()

        // Validate email and password fields
        if (email.isBlank()) {
            Toast.makeText(this@LoginActivity, "Please write email", Toast.LENGTH_LONG).show()
            return
        }

        if (password.isBlank()) {
            Toast.makeText(this@LoginActivity, "Please write password", Toast.LENGTH_LONG).show()
            return
        }

        // Sign in with email and password using FirebaseAuth
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If login is successful, start MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    // If login fails, display error message
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: " + task.exception!!.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
