package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

@Composable
fun AuthScreen(
     navController: NavController,
    onAuthComplete: () -> Unit,

) {
    var showSignUp by remember { mutableStateOf(false) }
    var showShelterOwnerAuth by remember { mutableStateOf(false) }
    var showShelterOwnerSignUp by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PawMateILSTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showShelterOwnerAuth -> {
                    if (showShelterOwnerSignUp) {
                        ShelterOwnerSignUpScreen(
                            onSignUpClick = { businessName, ownerName, email, phone, password, confirmPassword ->
                                onAuthComplete()
                            },
                            onLoginClick = { showShelterOwnerSignUp = false },
                            onUserAuthClick = { showShelterOwnerAuth = false }
                        )
                    } else {
                        ShelterOwnerLoginScreen(
                            onLoginClick = { email, password ->
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
                            onSignUpClick = { name, email, password, confirmPassword ->
                                onAuthComplete()
                                navController.navigate("pet_selection")
                            },
                            onLoginClick = { showSignUp = false },
                            onSellerAuthClick = { showShelterOwnerAuth = true }
                        )

                    } else {
                        LoginScreen(
                            onLoginClick = { email, password ->
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