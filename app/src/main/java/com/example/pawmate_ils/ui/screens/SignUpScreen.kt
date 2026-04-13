package com.example.pawmate_ils.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.AdopShelDataStruc.AdopterRepository
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.ProfilePhotoDefaults
import com.example.pawmate_ils.R
import com.example.pawmate_ils.SettingsManager
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.firebase_models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    onSignUpClick: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSellerSignUp: () -> Unit,
    sharedViewModel: SharedViewModel,
) {
    // --- FIREBASE AUTHENTICATION ---
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val authState = authViewModel.authState.observeAsState()
    val firestoreRepo = remember { FirestoreRepository() }
    val homeViewModel: HomeViewModel = viewModel()

    var currentStep by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") } // Add this line
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }

    var gender by remember { mutableStateOf("") }
    var isGenderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female")

    // 💎 ADDED: Dialog State
    var showVerificationDialog by remember { mutableStateOf(false) }

    val newUser by authViewModel.newUser.observeAsState()

    // --- GOOGLE SIGN-UP/SIGN-IN HANDLER ---
    // --- GOOGLE SIGN-UP/SIGN-IN HANDLER ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken == null) {
                errorMessage = "Failed to get Google ID Token."
                isGoogleLoading = false
                return@rememberLauncherForActivityResult
            }
            authViewModel.signUpWithGoogle(context, idToken) { success, status ->
                isGoogleLoading = false
                if (success) {
                    if (status == "new") {
                        currentStep = 2 // Move to Step 2 for new users
                    } else {
                        navController.navigate("adopter_home") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                } else {
                    errorMessage = status ?: "Google Sign-In failed."
                }
            }
        } catch (e: Exception) { // Added missing catch block
            isGoogleLoading = false
            errorMessage = "Google sign in failed: ${e.message}"
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF0F5),
                        Color(0xFFFFE4E9),
                        Color(0xFFFFD6E0)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (currentStep) {
                1 -> {
                    Image(
                        painter = painterResource(id = R.drawable.blackpawmateicon3),
                        contentDescription = "PawMate Logo",
                        modifier = Modifier.size(80.dp).padding(bottom = 8.dp)
                    )

                    Text(text = "Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Sign up to get started", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Enter your email", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFB6C1), unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Create a password", color = Color.Gray.copy(alpha = 0.6f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFB6C1), unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (email.isBlank()) { errorMessage = "Please enter your email"; return@Button }
                            if (password.isBlank()) { errorMessage = "Please enter password"; return@Button }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { errorMessage = "Please enter a valid email address"; return@Button }
                            errorMessage = null
                            currentStep = 2
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                        Text(text = "or", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            authViewModel.fetchUserRole()
                            isGoogleLoading = true
                            val gsoClient = getGoogleSignInClient(context)
                            launcher.launch(gsoClient.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                        TextButton(
                            onClick = onLoginClick,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 1.dp)
                        ) {
                            Text("Log in", color = Color(0xFFFF9999), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = onNavigateToSellerSignUp, modifier = Modifier.fillMaxWidth()) {
                        Text("Shelter Owner? Sign up here", color = Color(0xFFFF9999).copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                2 -> {
                    Text("About You", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = firstName, onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lastName, onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = mobileNumber, onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = address, onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = age, onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = isGenderExpanded,
                        onExpandedChange = { isGenderExpanded = !isGenderExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isGenderExpanded,
                            onDismissRequest = { isGenderExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            genderOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        gender = selectionOption
                                        isGenderExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = aboutMe,
                        onValueChange = { aboutMe = it },
                        label = { Text("About Me") },
                        placeholder = { Text("Tell shelters about your experience with pets...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- UPDATED COMPLETE SIGN UP BUTTON ---
                    Button(
                        onClick = {
                            if (firstName.isBlank() || lastName.isBlank() || age.isBlank() || gender.isBlank() || mobileNumber.isBlank() || address.isBlank() || aboutMe.isBlank() ) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            errorMessage = null
                            isLoading = true

                            val firebaseUser = FirebaseAuth.getInstance().currentUser

                            // 🎯 CHECK: If this is a Google user, we skip authViewModel.signUp
                            // because they are already authenticated!
                            val isGoogleUser = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true

                            if (isGoogleUser) {
                                // --- GOOGLE USER PATH ---
                                scope.launch {
                                    val uid = firebaseUser!!.uid
                                    val defaultAvatar = ProfilePhotoDefaults.photoUriForGender(context, gender)
                                    val isNewUser = firestoreRepo.isNewUser(uid)

                                    val user = User(
                                        id = uid,
                                        name = "$firstName $lastName",
                                        email = firebaseUser.email ?: "",
                                        gender = gender,
                                        photoUri = defaultAvatar,
                                        MobileNumber = mobileNumber,
                                        Address = address,
                                        Age = age,
                                        aboutMe = aboutMe,
                                        role = "adopter",
                                        tier = "0",
                                        gems = if (isNewUser) 10 else 0,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    try {
                                        firestoreRepo.addUser(user)
                                        firestoreRepo.addAdopterProfile(user)
                                        val settings = SettingsManager(context)
                                        settings.setUsername("$firstName $lastName")
                                        sharedViewModel.username.value = "$firstName $lastName"

                                        isLoading = false
                                        // Google users go straight to home (no verification needed)
                                        navController.navigate("adopter_home") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                        isLoading = false
                                    }
                                }
                            } else {
                                // --- EMAIL/PASSWORD PATH (YOUR ORIGINAL LOGIC) ---
                                authViewModel.signUp(email, password) { success, message ->
                                    if (success) {
                                        scope.launch {
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            if (uid != null) {
                                                val defaultAvatar = ProfilePhotoDefaults.photoUriForGender(context, gender)
                                                val isNewUser = firestoreRepo.isNewUser(uid)
                                                val user = User(
                                                    id = uid,
                                                    name = "$firstName $lastName",
                                                    email = email,
                                                    gender = gender,
                                                    photoUri = defaultAvatar,
                                                    MobileNumber = mobileNumber,
                                                    Address = address,
                                                    Age = age,
                                                    aboutMe = aboutMe,
                                                    role = "adopter",
                                                    gems = if (isNewUser) 10 else 0,
                                                    createdAt = System.currentTimeMillis()
                                                )
                                                try {
                                                    firestoreRepo.addUser(user)
                                                    val settings = SettingsManager(context)
                                                    settings.setUsername("$firstName $lastName")
                                                    sharedViewModel.username.value = "$firstName $lastName"

                                                    isLoading = false
                                                    showVerificationDialog = true
                                                } catch (e: Exception) {
                                                    errorMessage = e.message
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    } else {
                                        errorMessage = message
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        // --- END OF UPDATED LOGIC ---
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1)),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Complete Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
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

        // 💎 THE VERIFICATION DIALOG
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = { Icon(painterResource(id = R.drawable.blackpawmateicon3), null, Modifier.size(48.dp), Color(0xFFFFB6C1)) },
                title = { Text("Verify Your Email", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    TextButton(
                        onClick = {
                            authViewModel.resendVerificationEmail { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { Text("Resend Email", color = Color(0xFFFF9999)) }
                }
            )
        }
    }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}