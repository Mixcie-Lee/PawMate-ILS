package com.example.pawmate_ils.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit
) {
    var showSignUp by remember { mutableStateOf(false) }
    var showSellerAuth by remember { mutableStateOf(false) }
    var showSellerSignUp by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PawMateILSTheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showSellerAuth -> {
                    if (showSellerSignUp) {
                        SellerSignUpScreen(
                            onSignUpClick = { businessName, ownerName, email, phone, password, confirmPassword ->
                                onAuthComplete()
                            },
                            onLoginClick = { showSellerSignUp = false },
                            onUserAuthClick = { showSellerAuth = false }
                        )
                    } else {
                        SellerLoginScreen(
                            onLoginClick = { email, password ->
                                onAuthComplete()
                            },
                            onSignUpClick = { showSellerSignUp = true },
                            onUserAuthClick = { showSellerAuth = false }
                        )
                    }
                }
                else -> {
                    if (showSignUp) {
                        SignUpScreen(
                            onSignUpClick = { name, email, password, confirmPassword ->
                                onAuthComplete()
                            },
                            onLoginClick = { showSignUp = false },
                            onSellerAuthClick = { showSellerAuth = true }
                        )
                    } else {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                onAuthComplete()
                            },
                            onSignUpClick = { showSignUp = true },
                            onSellerAuthClick = { showSellerAuth = true }
                        )
                    }
                }
            }
        }
    }
} 