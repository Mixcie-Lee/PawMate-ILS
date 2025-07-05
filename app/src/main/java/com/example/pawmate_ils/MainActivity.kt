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
import com.example.pawmate_ils.ui.screens.LoginScreen
import com.example.pawmate_ils.ui.screens.SignUpScreen
import com.example.pawmate_ils.ui.screens.UserTypeSelectionScreen
import TinderLogic_PetSwipe.PetSwipeScreen
import TinderLogic_CatSwipe.CatSwipeScreen
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.ui.screens.ShelterOwnerSignUpScreen
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

class MainActivity : ComponentActivity() {
    private val sharedViewModel : SharedViewModel by viewModels()
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

                            NavHost(navController = navController, startDestination = "user_type") {
                                composable("user_type") {
                                    UserTypeSelectionScreen(navController = navController)
                                }
                                composable("signup") {
                                    SignUpScreen(
                                        navController = navController,
                                        sharedViewModel = sharedViewModel,
                                        onSignUpClick = { _, _, _, _ ->
                                            navController.navigate("pet_selection") {
                                                popUpTo("user_type") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        },
                                        onLoginClick = {
                                            navController.navigate("login") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onSellerAuthClick = {
                                            navController.navigate("seller_signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composable("login") {
                                    LoginScreen(
                                        onLoginClick = { _, _ ->
                                            navController.navigate("pet_selection") {
                                                popUpTo("user_type") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        },
                                        onSignUpClick = {
                                            navController.navigate("signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onSellerAuthClick = {
                                            navController.navigate("seller_signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composable("seller_signup") {
                                    ShelterOwnerSignUpScreen(
                                        onSignUpClick = { _, _, _, _, _, _ ->
                                            // Handle successful seller signup (e.g., navigate to seller dashboard)
                                        },
                                        onLoginClick = {
                                            navController.navigate("login") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onUserAuthClick = {
                                            navController.navigate("signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composable("pet_selection") {
                                    PetSelectionScreen(navController = navController)
                                }
                                composable("pet_swipe") {
                                    PetSwipeScreen(userName = "User")
                                }
                                composable("cat_swipe") {
                                    CatSwipeScreen(userName = "User")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}