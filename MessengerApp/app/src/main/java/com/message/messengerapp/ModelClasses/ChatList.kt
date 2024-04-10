package com.message.messengerapp.ModelClasses

// Class representing a chat list entry
class ChatList {

    // Private property representing the ID of the chat list entry
    private var id: String = ""

    // Default constructor required for Firebase
    constructor()

    // Parameterized constructor for initializing the ID of the chat list entry
    constructor(id: String) {
        this.id = id
    }

    // Getter method for retrieving the ID of the chat list entry
    fun getId(): String {
        return id
    }

    // Setter method for setting the ID of the chat list entry
    fun setId(id: String) {
        this.id = id
    }
}
