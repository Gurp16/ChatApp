package com.message.messengerapp.Notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService

// Service class for handling Firebase Cloud Messaging (FCM) token updates
class MyFirebaseInstanceId : FirebaseMessagingService() {

    // Override method called when a new FCM token is generated
    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)

        // Get the current Firebase user
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        // If a user is logged in, update their FCM token
        if (firebaseUser != null) {
            updateToken(refreshedToken)
        }
    }

    // Function to update the FCM token for the current user in the Firebase Realtime Database
    private fun updateToken(refreshedToken: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Tokens")

        // Create a Token object with the new token
        val token = Token(refreshedToken)

        // Update the token in the database under the current user's ID
        firebaseUser?.uid?.let {
            ref.child(it).setValue(token)
        }
    }
}
