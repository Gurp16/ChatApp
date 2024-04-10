package com.message.messengerapp.Notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton class responsible for creating Retrofit client instances for sending notifications
class Client {

    // Singleton object holding the Retrofit client instance
    object Client {
        private var retrofit: Retrofit? = null

        // Function to create and return a Retrofit client instance
        fun getClient(url: String?): Retrofit? {

            // Create a Retrofit client if it doesn't exist
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }
}
