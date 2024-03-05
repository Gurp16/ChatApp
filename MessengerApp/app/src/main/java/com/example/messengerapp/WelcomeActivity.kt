// WelcomeActivity.kt
package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messengerapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity(), GoogleSignInManager.SignInCallback {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var googleSignInManager: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleSignInManager = GoogleSignInManager(this, this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSignInSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSignInFailure(errorMessage: String) {
        showErrorToast(errorMessage)
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
