package com.example.pawmate_ils.firebase_models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties // Prevents crashes if Firestore has extra fields you haven't defined yet
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String? = null,
    val receiverId: String = "",
    val messageText: String = "", // Used for the text bubble
    val imageUrl: String? = null,  // Used for the Cloudinary link
    val createdAt: Long = System.currentTimeMillis(),
    val replyToText: String? = null,
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val replyToSenderImage: String? = null
) {
    // Firebase needs this empty constructor to "read" the data on the receiver's end
    constructor() : this("", "", "", null, "", "", null, 0L)
}