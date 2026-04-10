package com.example.pawmate_ils.firebase_models

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("shelterName")
    @set:PropertyName("shelterName")
    var shelterName: String = "",

    @get:PropertyName("adopterName")
    @set:PropertyName("adopterName")
    var adopterName: String = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("gender")
    @set:PropertyName("gender")
    var gender: String = "",

    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: String = "",

    @get:PropertyName("mobileNumber")
    @set:PropertyName("mobileNumber")
    var MobileNumber: String = "",

    @get:PropertyName("address")
    @set:PropertyName("address")
    var Address: String = "",

    @get:PropertyName("age")
    @set:PropertyName("age")
    var Age: String = "",

    @get:PropertyName("aboutMe")
    @set:PropertyName("aboutMe")
    var aboutMe: String = "",

    @get:PropertyName("photoUri")
    @set:PropertyName("photoUri")
    var photoUri: String = "",

    @get:PropertyName("gems")
    @set:PropertyName("gems")
    var gems: Int = 10,

    @get:PropertyName("tier")
    @set:PropertyName("tier")
    var tier: String = "Free",

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("online")
    @set:PropertyName("online")
    var isOnline: Boolean = false,

    @get:PropertyName("lastActive")
    @set:PropertyName("lastActive")
    var lastActive: Long? = null,

    @get:PropertyName("likedPetsCount")
    @set:PropertyName("likedPetsCount")
    var likedPetsCount: Int = 0,



    @get:PropertyName("ownerName")
    @set:PropertyName("ownerName")
    var ownerName: String = "",

    @get:PropertyName("isNewUser")
    @set:PropertyName("isNewUser")
    var isNewUser: Boolean = true,

    @get:PropertyName("shelterHours")
    @set:PropertyName("shelterHours")
     var shelterHours: String = "",
)