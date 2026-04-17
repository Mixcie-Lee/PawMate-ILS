    package com.example.pawmate_ils.ui.screens
    
    import android.util.Log
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.ImeAction
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.ui.platform.LocalContext
    import androidx.fragment.app.FragmentActivity
    import com.example.pawmate_ils.BiometricHelper
    import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
    import com.example.pawmate_ils.R
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.android.gms.common.api.ApiException
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SellerLoginScreen(
        authViewModel: AuthViewModel,
        onLoginSuccess: () -> Unit,
        onSignUpClick: () -> Unit,
       onNavigateToAdopter: () -> Unit
    ) {
    
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scrollState = rememberScrollState()
        val context = LocalContext.current
    
    
        //ADDITIONAL SECURITY : FINGERPRINT
        val prefs = remember { context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE) }
        val biometricHelper = remember { BiometricHelper(context) }
    
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            val savedEmail = prefs.getString("saved_seller_email", null) // 🎯 Unique Key
            val savedPassword = prefs.getString("saved_seller_password", null)
            if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank() && biometricHelper.isBiometricAvailable()) {
                biometricHelper.showBiometricPrompt(
                    activity = context as FragmentActivity,
                    onSuccess = {
                        isLoading = true
                        authViewModel.signIn(context, savedEmail, savedPassword) { success, message ->
                            isLoading = false
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Session expired. Please log in manually."
                            }
                        }
                    },
                    onError = { error ->
                        Log.d("BIO_AUTH", "Biometric ignored, user must type: $error")
                    }
                )
            }
        }
    
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    authViewModel.signUpWithGoogle(context,idToken) { success, message ->
                        if (success) {
                            onLoginSuccess() // 💎 Navigate to pet_swipe
                        } else {
                            // Show error toast or message
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Failed: ${e.message}")
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
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))
    
                Image(
                    painter = painterResource(id = R.drawable.blackpawmateicon3),
                    contentDescription = "PawMate Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )
    
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
    
                Spacer(modifier = Modifier.height(8.dp))
    
                Text(
                    text = "Welcome Shelter!, Sign in to continue",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
    
                Spacer(modifier = Modifier.height(48.dp))
    
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("email@domain.com", color = Color.Gray.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )
                }
    
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = Color.Gray.copy(alpha = 0.4f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
    
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email first."
                        } else {
                            authViewModel.resetPassword(email) { _, msg ->
                                errorMessage = msg
                            }
                        }
                    }) {
                        Text("Forgot Password?", color = Color(0xFFFF9999), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
    
                // 💎 Error Message Section (FIXED BRACES)
                errorMessage?.let { currentError ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = currentError, color = Color(0xFFC62828), fontSize = 14.sp)
    
                            if (currentError.contains("verified", ignoreCase = true)) {
                                TextButton(
                                    onClick = {
                                        authViewModel.resendVerificationEmail { _, resendMsg ->
                                            errorMessage = resendMsg
                                        }
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Resend verification email?", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
    
                // 💎 Sign In Button (NOW ALWAYS VISIBLE)
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill in all fields"
                            return@Button
                        }
                        isLoading = true
                        authViewModel.signIn(context,email, password) { success, message ->
                            isLoading = false
                            if (success) {
                                prefs.edit().apply {
                                    putString("saved_seller_email", email)
                                    putString("saved_seller_password", password)
                                    apply()
                                }
                                onLoginSuccess()
                            } else {
                                errorMessage = message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
    
                Spacer(modifier = Modifier.height(20.dp))
    
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                    Text("or", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                }
    
                Spacer(modifier = Modifier.height(20.dp))
    
                OutlinedButton(
                    onClick = {val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        launcher.launch(googleSignInClient.signInIntent)  },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), modifier = Modifier.padding(end = 12.dp))
                        Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
    
                Spacer(modifier = Modifier.height(20.dp))
    
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
                    TextButton(
                        onClick = onSignUpClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 1.dp)
                    ) {
                        Text("Sign Up", color = Color(0xFFFF9999), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
    
                TextButton(onClick = onNavigateToAdopter, modifier = Modifier.fillMaxWidth()) {
                    Text("Adopter? Sign in here", color = Color(0xFFFF9999).copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }