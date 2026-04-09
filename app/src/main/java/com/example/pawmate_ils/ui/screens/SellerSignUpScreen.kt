package com.example.pawmate_ils.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.R
import com.example.pawmate_ils.SettingsManager
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.firebase_models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerSignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onSignUpClick: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToAdopterSignUp: () -> Unit,
    sharedViewModel: SharedViewModel,
) {
    val context = LocalContext.current
    val firestoreRepo = remember { FirestoreRepository() }

    var currentStep by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var shelterName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }
    var shelterHours by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.signUpWithGoogle(context, idToken) { success, message ->
                    isGoogleLoading = false
                    if (success) {
                        currentStep = 2
                        email = account.email ?: ""
                    } else {
                        errorMessage = message ?: "Google Sign-In failed."
                    }
                }
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            errorMessage = "Google sign in failed: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF0F5), Color(0xFFFFE4E9), Color(0xFFFFD6E0))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                1 -> {
                    Image(
                        painter = painterResource(id = R.drawable.blackpawmateicon3),
                        contentDescription = "PawMate Logo",
                        modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
                    )

                    Text(text = "Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Shelter Sign Up", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(48.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Enter your email", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFB6C1), unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Create a password", color = Color.Gray.copy(alpha = 0.6f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFB6C1), unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Fields cannot be empty"
                                return@Button
                            }
                            errorMessage = null
                            currentStep = 2
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                        Text(text = "or", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            isGoogleLoading = true
                            launcher.launch(getGoogleSignInClient(context).signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF4285F4), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), modifier = Modifier.padding(end = 12.dp))
                                Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                        TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                            Text("Log in", color = Color(0xFFFF9999), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onNavigateToAdopterSignUp, modifier = Modifier.fillMaxWidth()) {
                        Text("Are you an Adopter? Sign up here", color = Color(0xFFFF9999).copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }

                2 -> {
                    Text("About Your Shelter", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(value = shelterName, onValueChange = { shelterName = it }, label = { Text("Shelter Name") }, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("Mobile Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = establishedYear, onValueChange = { establishedYear = it }, label = { Text("Established Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(
                        value = shelterHours,
                        onValueChange = { shelterHours = it },
                        label = { Text("Operating Hours") },
                        placeholder = { Text("e.g., 9AM - 5PM") },
                        shape = RoundedCornerShape(28.dp),
                        leadingIcon = { Icon(Icons.Default.Schedule, null, tint = Color(0xFFE84D7A)) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (shelterName.isBlank() || ownerName.isBlank()) {
                                errorMessage = "Please fill all fields"
                                return@Button
                            }
                            isLoading = true
                            authViewModel.signUp(email, password) { success, message ->
                                if (success) {
                                    scope.launch {
                                        try {
                                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                                            val uid = firebaseUser?.uid
                                            if (firebaseUser != null && uid != null) {
                                                firebaseUser.sendEmailVerification().await()
                                                val user = User(
                                                    id = uid,
                                                    shelterName = shelterName,
                                                    ownerName = ownerName,
                                                    email = email,
                                                    MobileNumber = mobileNumber,
                                                    Address = address,
                                                    Age = establishedYear,
                                                    role = "shelter",
                                                    shelterHours = shelterHours,
                                                    photoUri = "android.resource://${context.packageName}/${R.drawable.shelter}",
                                                    gems = 10
                                                )
                                                firestoreRepo.addUser(user)
                                                ShelterRepository().addShelter(user)
                                                val settings = SettingsManager(context)
                                                settings.setUsername(shelterName)
                                                sharedViewModel.username.value = shelterName
                                                isLoading = false
                                                showVerificationDialog = true
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage = message
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1)),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        else Text("Complete Shelter Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = it, color = Color(0xFFC62828), fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                }
            }
        }

        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = { Icon(painterResource(id = R.drawable.blackpawmateicon3), null, Modifier.size(48.dp), Color(0xFFFFB6C1)) },
                title = { Text("Verify Shelter Email", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("We've sent a link to:", fontSize = 14.sp, color = Color.Gray)
                        Text(email, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.height(12.dp))
                        Text("Please check your inbox and spam folder before logging in.", textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.signOut(context) {
                                showVerificationDialog = false
                                onLoginClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Go to Login", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(
                            onClick = {
                                authViewModel.resendVerificationEmail { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Resend Email", color = Color(0xFFFF9999), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            )
        }
    }
}