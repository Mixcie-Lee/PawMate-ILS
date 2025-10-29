package com.example.pawmate_ils.firebase_models


data class User(
    val id: String = "",             // Firebase Auth UID
    val name: String = "",
    val email: String = "",
    val role: String = "", // "adopter" or "shelter"
    val MobileNumber: String = "",
    val Address: String = "",
    val Age: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isOnline : Boolean = false,
    val lastActive : Long = System.currentTimeMillis()
)
