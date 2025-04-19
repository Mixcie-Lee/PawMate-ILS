package com.example.pawmate_ils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.screens.*
import com.example.pawmate_ils.PetSwipeScreen
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import com.example.pawmate_ils.onboard.OnboardingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawMateILSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            val navController = rememberNavController()
                            
                            NavHost(navController = navController, startDestination = "onboarding") {
                                composable("onboarding") {
                                    OnboardingScreen {
                                        navController.navigate("user_type") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }
                                composable("user_type") {
                                    UserTypeSelectionScreen(navController = navController)
                                }
                                composable("signup") {
                                    SignUpScreen(
                                        onSignUpClick = { _, _, _, _ ->
                                            navController.navigate("swipe")
                                        },
                                        onLoginClick = { 
                                            navController.navigate("login")
                                        },
                                        onSellerAuthClick = {
                                            navController.navigate("seller_signup")
                                        }
                                    )
                                }
                                composable("login") {
                                    LoginScreen(
                                        onLoginClick = { _, _ ->
                                            navController.navigate("swipe")
                                        },
                                        onSignUpClick = {
                                            navController.navigate("signup")
                                        },
                                        onSellerAuthClick = {
                                            navController.navigate("seller_signup")
                                        }
                                    )
                                }
                                composable("seller_signup") {
                                    SellerSignUpScreen(
                                        onSignUpClick = { businessName, ownerName, _, _, _, _ ->
                                            navController.navigate("adoption_center") {
                                                launchSingleTop = true
                                            }
                                        },
                                        onLoginClick = {
                                            navController.navigate("login")
                                        },
                                        onUserAuthClick = {
                                            navController.navigate("signup")
                                        }
                                    )
                                }
                                composable("adoption_center") {
                                    AdoptionCenterDashboard(
                                        centerName = "Adoption Center",
                                        onAddPet = { /* TODO: Implement add pet */ },
                                        onViewPets = { /* TODO: Implement view pets */ },
                                        onViewApplications = { /* TODO: Implement view applications */ },
                                        onViewStatistics = { /* TODO: Implement view statistics */ },
                                        onSettings = { /* TODO: Implement settings */ }
                                    )
                                }
                                composable("swipe") {
                                    PetSwipeScreen(userName = "Alex")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}