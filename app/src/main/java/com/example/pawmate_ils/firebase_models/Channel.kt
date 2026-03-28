package com.example.pawmate_ils.firebase_models

import com.google.firebase.firestore.PropertyName
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
    val createdAt: Long = System.currentTimeMillis(),
    //FOR TIER 3 ACTIVATION
    val adopterTier: Int = 0,      // Stores 1, 2, or 3
    @get:PropertyName("isPriority")
    @set:PropertyName("isPriority")
    @get:JvmName("getIsPriority") // Helps with some Kotlin compiler edge case
    var isPriority: Boolean = false ,// Quick flag for "VIP" sorting

    @get:PropertyName("online")
    @set:PropertyName("online")
    var isOnline: Boolean = false,

    @get:PropertyName("lastActive")
    @set:PropertyName("lastActive")
    var lastActive: Long? = null


)