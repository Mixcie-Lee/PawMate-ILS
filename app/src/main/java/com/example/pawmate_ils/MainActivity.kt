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
import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.screens.LoginScreen
import com.example.pawmate_ils.ui.screens.SignUpScreen
import com.example.pawmate_ils.ui.screens.UserTypeSelectionScreen
import TinderLogic_PetSwipe.PetSwipeScreen
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.screens.ShelterOwnerSignUpScreen
import com.example.pawmate_ils.ui.screens.ShelterOwnerLoginScreen
import com.example.pawmate_ils.ui.screens.AdoptionCenterDashboard
import com.example.pawmate_ils.ui.screens.AdoptionCenterApplications
import com.example.pawmate_ils.ui.screens.AdoptionCenterPets
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import TinderLogic_PetSwipe.AdopterLikeScreen
import com.example.pawmate_ils.ui.screens.ProfileSettingsScreen
import com.example.pawmate_ils.ui.screens.AccountSettingsScreen
import com.example.pawmate_ils.onboard.OnboardingScreen
import com.example.pawmate_ils.onboard.OnboardingUtil
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // EXTREME fullscreen - hide everything
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        // Force hide system UI
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        setContent {
            PawMateILSTheme(darkTheme = ThemeManager.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    /*FIREBASE FUNCTIONALITIES AND OTHER TOOLS FOR AUTHENTICATED USERS(LOGGED IN/SIGNED IN) BYPASS THE SIGN UP PAGE
                      AUTHENTICATED USERS WILL GO STRAIGHT FROM THEIR RESPECTIVE DESTINATION PAGE(ADOPTER HOME/SHELTER HOME)
                      !DO NOT DELETE!
                    */
                    val auth: FirebaseAuth = FirebaseAuth.getInstance()
                    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                    val context = LocalContext.current
                    val navController = rememberNavController()
                    val onboardingUtil = OnboardingUtil(context)
                    
                    // Determine start destination synchronously
                    val startDestination = remember {
                        // Reset onboarding for testing - this forces onboarding to show
                        context.getSharedPreferences("onboarding", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("completed", false)
                            .apply()
                        
                        val isCompleted = onboardingUtil.isOnboardingCompleted()
                        if (!isCompleted) {
                            "onboarding"
                        } else {
                            "user_type"
                        }
                    }
                    
                    // Handle authenticated user navigation after NavHost is created
                    LaunchedEffect(Unit) {
                        if (onboardingUtil.isOnboardingCompleted()) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                try {
                                    val snapshot = db.collection("users").document(currentUser.uid).get().await()
                                    val role = snapshot.getString("role")
                                    val destination = when (role) {
                                        "adopter" -> "pet_swipe"
                                        "shelter" -> "adoption_center_dashboard"
                                        else -> "user_type"
                                    }
                                    if (destination != "user_type") {
                                        navController.navigate(destination) {
                                            popUpTo("user_type") { inclusive = true }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Stay on user_type if there's an error
                                }
                            }
                        }
                    }


                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    onComplete = {
                                        onboardingUtil.setOnboardingCompleted()
                                        navController.navigate("user_type") {
                                            popUpTo("onboarding") { inclusive = true }
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
                                        navController.navigate("pet_swipe") {
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
                                    },
                                    sharedViewModel = sharedViewModel
                                )
                            }
                            composable("login") {
                                LoginScreen(
                                    onLoginClick = { _, _ ->
                                        navController.navigate("pet_swipe") {
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
                                    navController = navController,
                                    onSignUpClick = { _, _, _, _ ->
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
                                    },
                                    sharedViewModel = sharedViewModel
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
                                AdoptionCenterDashboard(
                                    navController = navController,
                                    centerName = "Your Shelter"
                                )
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
                            composable("pet_swipe") {
                                PetSwipeScreen(navController = navController)
                            }
                            composable("adopter_home") {
                                AdopterLikeScreen(navController = navController)
                            }
                            composable("profile_settings") {
                                ProfileSettingsScreen(
                                    navController = navController,
                                    username = sharedViewModel.username.value ?: "User"
                                )

                            }
                            composable("account_settings") { AccountSettingsScreen(navController = navController) }
                            composable("help_support") { Text("Help & Support - Coming Soon") }
                            composable("about_app") { Text("About - Coming Soon") }
                        }
                    }
                }
            }
        }
    }
