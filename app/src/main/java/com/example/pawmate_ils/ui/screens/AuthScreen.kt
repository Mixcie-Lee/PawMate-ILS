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
import com.example.pawmate_ils.Firebase_Utils.AuthState
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
     onSignUpClick: (String, String, String, String) -> Unit,


     ) {
  //FOR FIRESTORE PURPOSE PLS DON'T DELETE THIS
    val scope = rememberCoroutineScope()
    val AuthViewModel : AuthViewModel = viewModel()
    val authState  = AuthViewModel.authState.observeAsState()
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


//FIREBASE AUTHENTICATION AGAIN


    PawMateILSTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showShelterOwnerAuth -> {
                    if (showShelterOwnerSignUp) {
                        ShelterOwnerSignUpScreen(
                            navController = navController,
                            onSignUpClick = { _,_,_,_ ->
                                onAuthComplete()
                                scope.launch {
                                    //ITO ANG SINASABI KO PLEASE WAG BURAHIN, HANGGAT MAARI GA WAG KA MAGCOPY PASTE FROM CURSOR
                                    //  IF MAG FIX KA NG ERRORS :D
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                    if (uid == null) {
                                        // user not signed in (maybe Auth step failed)
                                        errorMessage =
                                            "User not signed in. Please complete Step 1."
                                        isLoading = false
                                        return@launch
                                    }
                                    val user = User(
                                        id = uid,
                                        name = "$firstName $lastName",
                                        email = email,
                                        MobileNumber = mobileNumber,
                                        Address = address,
                                        Age = age,
                                        role = "adopter" // <-- or "shelter" depending on selection
                                    )
                                    try {
                                        firestoreRepo.addUser(user)
                                        onSignUpClick(firstName, email, lastName, mobileNumber)
                                        sharedViewModel.username.value = "$firstName, $lastName"
                                        delay(50)
                                        navController.navigate("adoption_center_dashboard") {
                                            popUpTo("user_type") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to save user data"
                                    } finally {
                                        isLoading = false
                                    }
                                }

                            },
                            onLoginClick = { showShelterOwnerSignUp = false },
                            onUserAuthClick = { showShelterOwnerAuth = false },
                            sharedViewModel = sharedViewModel
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
                            onSignUpClick = { _, _, _, _ ->
                                onAuthComplete()
                                navController.navigate("pet_selection")
                            },
                            onLoginClick = { showSignUp = false },
                            onSellerAuthClick = { showShelterOwnerAuth = true },
                            sharedViewModel = sharedViewModel
                        )

                    } else {
                        LoginScreen(
                            authViewModel = AuthViewModel,
                            onLoginClick = { email, password->
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