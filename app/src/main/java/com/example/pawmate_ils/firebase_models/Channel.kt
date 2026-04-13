package com.example.pawmate_ils.firebase_models

import com.google.firebase.firestore.PropertyName
// ✅ IMPORTANT: Using Long for timestamps to keep your UI logic simple
data class Channel(
    val channelId: String = "",
    val adopterId: String = "",
    val shelterId: String = "",

    @get:PropertyName("adopterName")
    @set:PropertyName("adopterName")
    var adopterName: String = "",


    @get:PropertyName("shelterName")
    @set:PropertyName("shelterName")
    var shelterName: String = "",

    @get:PropertyName("ownerName")
    @set:PropertyName("ownerName")
    var ownerName: String = "",

    @get:PropertyName("shelterPhotoUri")
    @set:PropertyName("shelterPhotoUri")
    var shelterPhotoUri: String = "", // 🎯 Add PropertyName and change type

    @get:PropertyName("adopterPhotoUri")
    @set:PropertyName("adopterPhotoUri")
    var adopterPhotoUri: String = "",

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
    var lastActive: Long? = null,

    val petNames: List<String> = emptyList(),
    val petIds: List<String> = emptyList()


)