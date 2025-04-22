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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.screens.*
import com.example.pawmate_ils.PetSelectionScreen
import TinderLogic_PetSwipe.PetSwipeScreen
import TinderLogic_CatSwipe.CatSwipeScreen
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
                                    OnboardingScreen(
                                        onComplete = {
                                            navController.navigate("user_type") {
                                                popUpTo("onboarding") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                                composable("user_type") {
                                    UserTypeSelectionScreen(navController = navController)
                                }
                                composable("signup") {
                                    SignUpScreen(
                                        navController = navController,
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
                                    SellerSignUpScreen(
                                        onSignUpClick = { _, _, _, _, _, _ ->
                                            navController.navigate("adoption_center_dashboard") {
                                                popUpTo("user_type") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        },
                                        onLoginClick = {
                                            navController.navigate("seller_login") {
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
                                composable("seller_login") {
                                    SellerLoginScreen(
                                        onLoginClick = { _, _ ->
                                            navController.navigate("adoption_center_dashboard") {
                                                popUpTo("user_type") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        },
                                        onSignUpClick = {
                                            navController.navigate("seller_signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onUserAuthClick = {
                                            navController.navigate("login") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composable("adoption_center_dashboard") {
                                    AdoptionCenterDashboard(
                                        navController = navController,
                                        centerName = "Your Adoption Center"
                                    )
                                }
                                composable("adoption_center_pets") {
                                    AdoptionCenterPets(
                                        onBackClick = { navController.navigateUp() },
                                        onAddPet = { navController.navigate("add_pet") }
                                    )
                                }
                                composable("adoption_center_applications") {
                                    AdoptionCenterApplications(
                                        onBackClick = { navController.navigateUp() }
                                    )
                                }
                                composable("add_pet") {
                                    // TODO: Implement AddPetScreen
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text("Add Pet Screen Coming Soon!")
                                    }
                                }
                                composable("settings") {
                                    // TODO: Implement SettingsScreen
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text("Settings Screen Coming Soon!")
                                    }
                                }
                                composable("adoption_center_statistics") {
                                    // TODO: Implement StatisticsScreen
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text("Statistics Screen Coming Soon!")
                                    }
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