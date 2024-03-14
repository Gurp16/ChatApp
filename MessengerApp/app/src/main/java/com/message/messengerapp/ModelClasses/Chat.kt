package com.message.messengerapp.ModelClasses

class Chat {

    private var sender: String = ""
    private var message: String = ""
    private var receiver: String = ""
    private var isSeen: Boolean = false
    private var url: String = ""
    private var messageId: String = ""

    constructor()

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

    fun isSeen(): Boolean {
        return isSeen
    }

    // Rename the isSeen method
    fun getIsSeen(): Boolean {
        return isSeen
    }

    // Add setter for isSeen field
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
