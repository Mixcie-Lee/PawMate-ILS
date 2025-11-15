package com.example.pawmate_ils.firebase_models

import com.google.firebase.Timestamp

    data class Channel(
        val channelId: String = "",
        val adopterId: String = "",
        val adopterName: String = "",
        val shelterId: String = "",
        val shelterName: String = "",
        val petName: String = "",
        val lastMessage: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val unreadCount: Int = 0,
        val createdAt: Long = System.currentTimeMillis()
    )