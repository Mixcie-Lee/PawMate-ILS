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
import com.example.pawmate_ils.ui.screens.SellerSignUpScreen
import com.example.pawmate_ils.ui.screens.SignUpScreen
import com.example.pawmate_ils.ui.screens.UserTypeSelectionScreen
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

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
                            
                            NavHost(navController = navController, startDestination = "user_type") {
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
                                        onSignUpClick = { _, _, _, _, _, _ ->
                                            navController.navigate("swipe")
                                        },
                                        onLoginClick = {
                                            navController.navigate("login")
                                        },
                                        onUserAuthClick = {
                                            navController.navigate("signup")
                                        }
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