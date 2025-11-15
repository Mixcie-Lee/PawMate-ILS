package com.example.pawmate_ils.firebase_models




data class User(
    val id: String = "",
    val name: String = "",
    val shelterName: String = "",
    val adopterName: String = "",
    val email: String = "",
    val role: String = "",
    val MobileNumber: String = "",
    val Address: String = "",
    val Age: String = "",
    val photoUri: String = "",
    val gems: Int  = 10,
    val createdAt: Long = System.currentTimeMillis(),  // <-- use Long
    val isOnline : Boolean = false,
    val lastActive: Long? = null,                     // optional: store as Long
    val likedPetsCount : Int = 0,
    val isNewUser: Boolean = true
)
