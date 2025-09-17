package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

@Composable
fun AuthScreen(
     navController: NavController,
    onAuthComplete: () -> Unit,
     sharedViewModel: SharedViewModel

) {
    var showSignUp by remember { mutableStateOf(false) }
    var showShelterOwnerAuth by remember { mutableStateOf(false) }
    var showShelterOwnerSignUp by remember { mutableStateOf(false) }

    PawMateILSTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showShelterOwnerAuth -> {
                    if (showShelterOwnerSignUp) {
                        ShelterOwnerSignUpScreen(
                            onSignUpClick = { _, _, _, _, _, _ ->
                                onAuthComplete()
                            },
                            onLoginClick = { showShelterOwnerSignUp = false },
                            onUserAuthClick = { showShelterOwnerAuth = false }
                        )
                    } else {
                        ShelterOwnerLoginScreen(
                            onLoginClick = { _, _ ->
                                onAuthComplete()
                            },
                            onSignUpClick = { showShelterOwnerSignUp = true },
                            onUserAuthClick = { showShelterOwnerAuth = false }
                        )
                    }
                }
                else -> {
                    if (showSignUp) {
                        SignUpScreen(
                            navController = navController,
                            sharedViewModel = sharedViewModel,
                            onSignUpClick = { _, _, _, _, _ ->
                                onAuthComplete()
                                navController.navigate("pet_selection")
                            },
                            onLoginClick = { showSignUp = false },
                            onSellerAuthClick = { showShelterOwnerAuth = true }
                        )

                    } else {
                        LoginScreen(
                            onLoginClick = { _, _ ->
                                onAuthComplete()
                            },
                            onSignUpClick = { showSignUp = true },
                            onSellerAuthClick = { showShelterOwnerAuth = true }
                        )
                    }
                }
            }
        }
    }
}