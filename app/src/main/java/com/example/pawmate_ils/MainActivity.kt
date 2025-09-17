package com.example.pawmate_ils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.pawmate_ils.ui.screens.ShelterOwnerLoginScreen
import com.example.pawmate_ils.ui.screens.AdoptionCenterDashboard
import com.example.pawmate_ils.ui.screens.AdoptionCenterApplications
import com.example.pawmate_ils.ui.screens.AdoptionCenterPets
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import com.example.pawmate_ils.PetSelectionScreen
import com.example.pawmate_ils.ui.screens.AdopterHomeScreen
import com.example.pawmate_ils.ui.screens.ProfileSettingsScreen

class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // EXTREME fullscreen - hide everything
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
            android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
            android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        
        // Force hide system UI
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContent {
            PawMateILSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
                                    ShelterOwnerLoginScreen(
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
                                            navController.navigate("signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composable("adoption_center_dashboard") {
                                    AdoptionCenterDashboard(navController = navController, centerName = "Your Shelter")
                                }
                                composable("adoption_center_pets") {
                                    AdoptionCenterPets(
                                        onBackClick = { navController.popBackStack() },
                                        onAddPet = { navController.navigate("add_pet") }
                                    )
                                }
                                composable("adoption_center_applications") {
                                    AdoptionCenterApplications(
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                composable("adoption_center_statistics") { Text("Statistics - Coming Soon") }
                                composable("settings") { Text("Settings - Coming Soon") }
                                composable("add_pet") { Text("Add Pet - Coming Soon") }
                                composable("pet_selection") {
                                    PetSelectionScreen(
                                        navController = navController,
                                        userName = sharedViewModel.username.value ?: "User"
                                    )
                                }
                                composable("pet_swipe") {
                                    PetSwipeScreen(navController = navController)
                                }
                                composable("cat_swipe") {
                                    CatSwipeScreen(navController = navController)
                                }
                                composable("adopter_home") {
                                    AdopterHomeScreen(navController = navController)
                                }
                                composable("profile_settings") {
                                    ProfileSettingsScreen(navController = navController)
                                }
                    }
                }
            }
        }
    }
}