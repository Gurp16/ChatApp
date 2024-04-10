package com.message.messengerapp.Notification

// Class representing data for notifications
class Data {

    // Properties for notification data
    private var user: String = ""
    private var icon: Int = 0
    private var body: String = ""
    private var title: String = ""
    private var sented: String = ""

    // Default constructor
    constructor()

    // Parameterized constructor
    constructor(user: String, icon: Int, body: String, title: String, sented: String) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
    }

    // Getter and setter methods for user
    fun getUser(): String? {
        return user
    }

    fun setUser(user: String) {
        this.user = user
    }

    // Getter and setter methods for icon
    fun getIcon(): Int {
        return icon
    }

    fun setIcon(icon: Int) {
        this.icon = icon
    }

    // Getter and setter methods for body
    fun getBody(): String? {
        return body
    }

    fun setBody(body: String) {
        this.body = body
    }

    // Getter and setter methods for title
    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    // Getter and setter methods for sented
    fun getSented(): String? {
        return sented
    }

    fun setSented(sented: String) {
        this.sented = sented
    }
}
