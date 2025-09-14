package com.example.pawmate_ils.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthRepository
import com.example.pawmate_ils.Firebase_Utils.UserRepository

@Composable
fun RoleBasedNavigation(navController: NavController) {
    val userRepo = UserRepository()
    val authRepo = AuthRepository()
    val userId = authRepo.getCurrentUserId()

    LaunchedEffect(userId) {
        if (userId != null) {
            val profile = userRepo.getUser(userId)
            when (profile?.role) {
                "adopter" -> navController.navigate("adopter_chat")
                "shelter" -> navController.navigate("shelter_chat")
            }
        }
    }
}
