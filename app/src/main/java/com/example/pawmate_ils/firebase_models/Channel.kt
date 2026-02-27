package com.example.pawmate_ils.firebase_models

// ✅ IMPORTANT: Using Long for timestamps to keep your UI logic simple
data class Channel(
    val channelId: String = "",
    val adopterId: String = "",
    val adopterName: String = "",
    val shelterId: String = "",
    val shelterName: String = "",
    val adopterPhotoUri: String? = null,
    val shelterPhotoUri: String? = null,
    val petName: String = "",
    val lastSenderId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)