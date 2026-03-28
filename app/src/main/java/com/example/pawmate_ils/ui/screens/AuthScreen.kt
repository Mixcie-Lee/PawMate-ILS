package com.example.pawmate_ils.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun AuthScreen(
    navController: NavController,
    onAuthComplete: () -> Unit,
    sharedViewModel: SharedViewModel,
    onUserAuthClick: () -> Unit,
    onSignUpClick: (String, String, String, String) -> Unit,
) {
    //FOR FIRESTORE PURPOSE PLS DON'T DELETE THIS
    val scope = rememberCoroutineScope()
    val AuthViewModel: AuthViewModel = viewModel()
    val authState = AuthViewModel.authState.observeAsState()
    val firestoreRepo = remember { FirestoreRepository() }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        SellerSignUpScreen(
                            navController = navController,
                            authViewModel = AuthViewModel,
                            onSignUpClick = { sName, sEmail, oName, mNum ->
                                onAuthComplete()
                                scope.launch {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                    if (uid == null) {
                                        errorMessage = "User not signed in. Please complete Step 1."
                                        isLoading = false
                                        return@launch
                                    }
                                    val user = User(
                                        id = uid,
                                        name = "$sName $oName",
                                        email = sEmail,
                                        MobileNumber = mNum,
                                        Address = address,
                                        Age = age,
                                        role = "shelter" // 💎 Fixed: Shelter side should be "shelter"
                                    )
                                    try {
                                        firestoreRepo.addUser(user)
                                        onSignUpClick(sName, sEmail, oName, mNum)
                                        sharedViewModel.username.value = "$sName $oName"

                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to save user data"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            onLoginClick = { showShelterOwnerSignUp = false },
                            // 💎 Changed parameter name to match SellerSignUpScreen
                            onNavigateToAdopterSignUp = { showShelterOwnerAuth = false },
                            sharedViewModel = sharedViewModel
                        )
                    } else {
                        SellerLoginScreen(
                            authViewModel = AuthViewModel,
                            // 💎 Adjusted to match () -> Unit signature
                            onLoginSuccess = {
                                onAuthComplete()
                            },
                            onSignUpClick = { showShelterOwnerSignUp = true },
                            // 💎 Changed parameter name to match SellerLoginScreen
                            onNavigateToAdopter = { showShelterOwnerAuth = false }
                        )
                    }
                }
                else -> {
                    if (showSignUp) {
                        SignUpScreen(
                            navController = navController,
                            onSignUpClick = { f, e, l, m ->
                                onAuthComplete()
                            },
                            onLoginClick = { showSignUp = false },
                            // 💎 Changed parameter name to match SignUpScreen
                            onNavigateToSellerSignUp = { showShelterOwnerAuth = true },
                            sharedViewModel = sharedViewModel
                        )
                    } else {
                        LoginScreen(
                            authViewModel = AuthViewModel,
                            // 💎 Adjusted to match () -> Unit signature
                            onLoginSuccess = {
                                onAuthComplete()
                            },
                            onSignUpClick = { showSignUp = true },
                            // 💎 Changed parameter name to match LoginScreen
                            onNavigateToShelter = { showShelterOwnerAuth = true }
                        )
                    }
                }
            }
        }
    }
}