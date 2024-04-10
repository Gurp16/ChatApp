// WelcomeActivity.kt
package com.message.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.message.messengerapp.databinding.ActivityWelcomeBinding

// WelcomeActivity displays the welcome screen of the application
class WelcomeActivity : AppCompatActivity(), GoogleSignInManager.SignInCallback {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var googleSignInManager: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize GoogleSignInManager for handling Google sign-in
        googleSignInManager = GoogleSignInManager(this, this)

        // Set click listeners for Google sign-in, register, and login buttons
        binding.googleSignInButton.setOnClickListener {
            googleSignInManager.signIn()
        }

        binding.registerwelcomebtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginWelcomeBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // Handle result of activity launched for result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInManager.onActivityResult(requestCode, resultCode, data)
    }

    // Callback method invoked when Google sign-in succeeds
    override fun onSignInSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Callback method invoked when Google sign-in fails
    override fun onSignInFailure(errorMessage: String) {
        showErrorToast(errorMessage)
    }

    // Show a toast message with the provided error message
    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
