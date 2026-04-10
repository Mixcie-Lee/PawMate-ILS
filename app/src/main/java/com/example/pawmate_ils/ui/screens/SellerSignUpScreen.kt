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
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Schedule
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
    import com.example.pawmate_ils.R
    import com.example.pawmate_ils.SettingsManager
    import com.example.pawmate_ils.SharedViewModel
    import com.example.pawmate_ils.firebase_models.User
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInClient
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.android.gms.common.api.ApiException
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.GoogleAuthProvider
    import kotlinx.coroutines.delay
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
        // --- FIREBASE AUTHENTICATION ---
        val context = LocalContext.current
        val authState  = authViewModel.authState.observeAsState()
        val firestoreRepo = remember { FirestoreRepository() }
    
        var currentStep by remember { mutableStateOf(1) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var shelterName by remember { mutableStateOf("") }
        var OwnerName by remember { mutableStateOf("") }
        var mobileNumber by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var establishedYear by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        var isGoogleLoading by remember { mutableStateOf(false) }
        var shelterHours by  remember {mutableStateOf("")}
    
        // 💎 ADDED: Dialog State
        var showVerificationDialog by remember { mutableStateOf(false) }
    
        // --- GOOGLE HANDLER (UNTAMPERED) ---
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken == null) {
                    errorMessage = "Failed to get Google ID Token."
                    return@rememberLauncherForActivityResult
                }
    
                authViewModel.signUpWithGoogle(context, idToken) { success, message ->
                    isGoogleLoading = false
                    if (success) {
                        currentStep = 2 // Move to details
                        email = account.email ?: ""
                    } else {
                        errorMessage = message ?: "Google Sign-In failed."
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
                    .verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))
    
                when (currentStep) {
                    1 -> {
                        // --- STEP 1 UI (Email/Password/Google) ---
                        Image(
                            painter = painterResource(id = R.drawable.blackpawmateicon3),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                        Text(text = "Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Shelter Sign Up", color = Color.Gray)
                        Spacer(modifier = Modifier.height(48.dp))
    
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            shape = RoundedCornerShape(28.dp)
                        )
    
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            shape = RoundedCornerShape(28.dp)
                        )
    
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Fields cannot be empty"; return@Button
                                }
                                currentStep = 2
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1)),
                            shape = RoundedCornerShape(28.dp)
                        ) { Text("Continue") }
    
                        Spacer(Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                isGoogleLoading = true; launcher.launch(
                                getGoogleSignInClient(
                                    context
                                ).signInIntent
                            )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            if (isGoogleLoading) CircularProgressIndicator(Modifier.size(24.dp))
                            else Text("Continue with Google")
                        }
    
                        TextButton(onClick = onLoginClick) { Text("Log in", color = Color(0xFFFF9999)) }
                        TextButton(onClick = onNavigateToAdopterSignUp) {
                            Text(
                                "Adopter? Sign up here",
                                color = Color.Gray
                            )
                        }
                    }
    
                    2 -> {
                        // --- STEP 2 UI (Details) ---
                        Text("About Your Shelter", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(32.dp))
    
                        OutlinedTextField(
                            value = shelterName,
                            onValueChange = { shelterName = it },
                            label = { Text("Shelter Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = OwnerName,
                            onValueChange = { OwnerName = it },
                            label = { Text("Owner Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword // Use NumberPassword or Phone
                            )
                        )
    
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = establishedYear,
                            onValueChange = { establishedYear = it },
                            label = { Text("Established Year") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            )                    )
    
                        OutlinedTextField(
                            value = shelterHours,
                            onValueChange = { input ->
                                val cleaned = input.take(15)
                                val formatted = when {
                                    // 1. If deleting, just accept the input
                                    cleaned.length < shelterHours.length -> cleaned

                                    // 2. The "Magic Dash": If it ends with am/pm and has no dash yet, add the separator
                                    (cleaned.endsWith("am", ignoreCase = true) || cleaned.endsWith("pm", ignoreCase = true)) &&
                                            !cleaned.contains("-") -> "$cleaned - "

                                    // 3. Prevent double dashes or spaces if they type it manually
                                    cleaned.endsWith("--") -> cleaned.dropLast(1)

                                    else -> cleaned
                                }
                                shelterHours = formatted
                            },
                            label = { Text("Shelter Operating Hours") },
                            placeholder = { Text("e.g., 9AM - 5PM") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(28.dp), // Matched your other fields
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFE84D7A)
                                )
                            }
                        )
    
                        Spacer(Modifier.height(24.dp))
    
                        Button(
                            onClick = {
                                if (shelterName.isBlank() || OwnerName.isBlank()) {
                                    errorMessage = "Please fill all fields"; return@Button
                                }
    
                                isLoading = true
                                authViewModel.signUp(email, password) { success, message ->
                                    if (success) {
                                        scope.launch {
                                            val firebaseuser = FirebaseAuth.getInstance().currentUser // 🎯 This is the Object
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            if (firebaseuser != null && uid != null) {
                                                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                                                    displayName = "$shelterName $OwnerName"
                                                }
                                                firebaseuser.updateProfile(profileUpdates).await()
                                                val defaultShelterPhoto = "android.resource://${context.packageName}/${R.drawable.shelter}"
    
    
                                                val user = User(
                                                    id = uid,
                                                    name = "$shelterName $OwnerName",
                                                    email = email,
                                                    MobileNumber = mobileNumber,
                                                    Address = address,
                                                    Age = establishedYear,
                                                    role = "shelter",
                                                    shelterHours = shelterHours,
                                                    photoUri = defaultShelterPhoto,
                                                    gems = 10,
                                                    likedPetsCount = 0
                                                )
                                                try {
                                                    firestoreRepo.addUser(user)
                                                    ShelterRepository().addShelter(user)
    
                                                    val settings = SettingsManager(context)
                                                    settings.setUsername("$shelterName $OwnerName")
                                                    sharedViewModel.username.value =
                                                        "$shelterName $OwnerName"
    
                                                    authViewModel.startUserProfileListener()
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
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1)),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = Color.White
                            )
                            else Text("Complete Shelter Sign Up")
                        }
                    }
                }
    
                errorMessage?.let { /* Error Card logic here */ }
            }
    
            // 💎 THE DIALOG (STAYING ON PAGE)
            // 💎 THE VERIFICATION DIALOG (Mirrored to Adopter Style)
            if (showVerificationDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color.White,
                    icon = {
                        // 🎯 Centered Icon
                        Icon(
                            painter = painterResource(id = R.drawable.blackpawmateicon3),
                            contentDescription = "PawMate Logo",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFFFB6C1)
                        )
                    },
                    title = {
                        // 🎯 Centered Bold Title
                        Text(
                            text = "Verify Shelter Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        // 🎯 Centered Content Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "We've sent a link to:",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = email,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Please check your inbox and spam folder before logging in.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
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
                        ) {
                            Text("Go to Login", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        // Centered TextButton
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextButton(
                                onClick = {
                                    authViewModel.resendVerificationEmail { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(
                                    "Resend Email",
                                    color = Color(0xFFFF9999),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                )
            }
        }
    }