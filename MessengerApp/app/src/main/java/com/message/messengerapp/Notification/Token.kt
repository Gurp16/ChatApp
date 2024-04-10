package com.message.messengerapp.Notification

import com.google.firebase.database.IgnoreExtraProperties

// Annotation to ignore extra properties when parsing from Firebase Database
@IgnoreExtraProperties
// Token class to store device registration tokens for push notifications
class Token {

    // Token string
    var token: String = ""

    // Default constructor required for Firebase
    constructor() {}

    // Constructor with token parameter
    constructor(token: String) {
        this.token = token
    }

    // Method to retrieve the token
    fun retrieveToken(): String {
        return token
    }

    // Method to update the token
    fun updateToken(token: String) {
        this.token = token
    }
}
