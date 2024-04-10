package com.message.messengerapp.ModelClasses

// Class representing a chat message
class Chat {

    // Private properties representing various attributes of a chat message
    private var sender: String = ""
    private var message: String = ""
    private var receiver: String = ""
    private var isSeen: Boolean = false // Indicates whether the message has been seen by the receiver
    private var url: String = "" // URL for any attached media (e.g., images)
    private var messageId: String = "" // Unique ID for each message

    // Default constructor required for Firebase
    constructor()

    // Parameterized constructor for initializing all properties of the chat message
    constructor(
        sender: String,
        message: String,
        receiver: String,
        isSeen: Boolean,
        url: String,
        messageId: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isSeen = isSeen
        this.url = url
        this.messageId = messageId
    }

    // Getter and setter methods for each property of the chat message
    fun getSender(): String {
        return sender
    }

    fun setSender(sender: String) {
        this.sender = sender
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getReceiver(): String {
        return receiver
    }

    fun setReceiver(receiver: String) {
        this.receiver = receiver
    }

    // Getter method for isSeen field
    fun getIsSeen(): Boolean {
        return isSeen
    }

    // Setter method for isSeen field
    fun setIsSeen(isSeen: Boolean) {
        this.isSeen = isSeen
    }

    fun getUrl(): String {
        return url
    }

    fun setUrl(url: String) {
        this.url = url
    }

    fun getMessageId(): String {
        return messageId
    }

    fun setMessageId(messageId: String) {
        this.messageId = messageId
    }
}
