package com.example.pawmate_ils.firebase_models

data class Message (
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "", // lowercase for consistency
    val senderImage: String? = null,
    val receiverId: String = "",
    val messageText: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

