package com.example.pawmate_ils.firebase_models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class AdopterProfile(
    val id: String = "",
    val AdopterName : String = "",
    val role: String = "",
    val email: String = "",
    val mobileNumber : String = "",
    val address : String = "",
    val password : String = " "
)