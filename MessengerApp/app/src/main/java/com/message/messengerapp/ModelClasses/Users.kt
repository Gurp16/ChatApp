package com.message.messengerapp.ModelClasses

// Class representing user data
class Users {

    // Properties representing various user attributes
    private var uid: String = "" // Unique user ID
    private var username: String = "" // User's username
    private var profile: String = "" // URL of the user's profile picture
    private var cover: String = "" // URL of the user's cover photo
    private var status: String = "" // User's status (e.g., online, offline)
    private var search: String = "" // Search keyword associated with the user
    private var facebook: String = "" // User's Facebook profile URL
    private var instagram: String = "" // User's Instagram username
    private var website: String = "" // User's personal website URL

    // Default constructor required for Firebase
    constructor()

    // Parameterized constructor for initializing user attributes
    constructor(
        uid: String,
        username: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String
    ) {
        this.uid = uid
        this.username = username
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
    }

    // Getter and setter methods for each user attribute
    // Each method provides access to the corresponding property

    // Getter for UID
    fun getUid(): String {
        return uid
    }

    // Setter for UID
    fun setUid(uid: String) {
        this.uid = uid
    }

    // Getter for username
    fun getUsername(): String {
        return username
    }

    // Setter for username
    fun setUsername(username: String) {
        this.username = username
    }

    // Getter for profile picture URL
    fun getProfile(): String {
        return profile
    }

    // Setter for profile picture URL
    fun setProfile(profile: String) {
        this.profile = profile
    }

    // Getter for cover photo URL
    fun getCover(): String {
        return cover
    }

    // Setter for cover photo URL
    fun setCover(cover: String) {
        this.cover = cover
    }

    // Getter for status
    fun getStatus(): String {
        return status
    }

    // Setter for status
    fun setStatus(status: String) {
        this.status = status
    }

    // Getter for search keyword
    fun getSearch(): String {
        return search
    }

    // Setter for search keyword
    fun setSearch(search: String) {
        this.search = search
    }

    // Getter for Facebook profile URL
    fun getFacebook(): String {
        return facebook
    }

    // Setter for Facebook profile URL
    fun setFacebook(facebook: String) {
        this.facebook = facebook
    }

    // Getter for Instagram username
    fun getInstagram(): String {
        return instagram
    }

    // Setter for Instagram username
    fun setInstagram(instagram: String) {
        this.instagram = instagram
    }

    // Getter for personal website URL
    fun getWebsite(): String {
        return website
    }

    // Setter for personal website URL
    fun setWebsite(website: String) {
        this.website = website
    }
}
