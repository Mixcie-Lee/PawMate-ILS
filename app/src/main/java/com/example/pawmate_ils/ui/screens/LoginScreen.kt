package com.example.pawmate_ils.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import  androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.withContext
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.pawmate_ils.Firebase_Utils.AuthState
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel : AuthViewModel,
    onLoginClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onSellerAuthClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)

    //HANDLES GOOGLE SIGN IN
    var isGoogleLoading by remember {mutableStateOf(false)}
    val context = LocalContext.current

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
            isGoogleLoading = true

            // Attempt Firebase sign-in/sign-up with Google
            authViewModel.signUpWithGoogle(idToken) { success, error ->
                isGoogleLoading = false
                if (success) {
                    // ✅ Navigate directly to home screen or pet swipe
                    onLoginClick(email, password)
                } else {
                    errorMessage = "Google Sign-In failed. Try again."
                }
            }

        } catch (e: ApiException) {
            isGoogleLoading = false
            errorMessage = "Google sign in failed: ${e.message}"
        }
    }



//checks if the user account is authenticated
    LaunchedEffect(authState) {
       when (authState) {
           is AuthState.Authenticated -> {
               isLoading = false
               errorMessage = null
               onLoginClick(email, password)
           }

           is AuthState.Loading -> isLoading = true
           is AuthState.Error -> {
               isLoading = false
               errorMessage = (authState as AuthState.Error).message
           }
           is AuthState.Unauthenticated -> isLoading = false
       }
   }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DarkBrown.copy(alpha = 0.2f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "PawMate Logo",
                    tint = DarkBrown,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PawMate",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Find your perfect pet companion",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Email, 
                        contentDescription = "Email",
                        tint = DarkBrown
                    ) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBrown,
                    focusedLabelColor = DarkBrown
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Lock, 
                        contentDescription = "Password",
                        tint = DarkBrown
                    ) 
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBrown,
                    focusedLabelColor = DarkBrown
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    isLoading = true
                    authViewModel.signIn(email, password) { success, error ->
                        isLoading = false
                        if (success) {
                            onLoginClick(email, password)
                        } else {
                            errorMessage = error ?: "Login failed"
                        }
                    }                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBrown,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton( // BUTTON FOR GOOGLE SIGN IN
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp),
                enabled = !isGoogleLoading
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.DarkGray,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Continue with Google",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                TextButton(onClick = onSignUpClick) {
                    Text(
                        "Sign Up",
                        color = DarkBrown,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TextButton(
                onClick = onSellerAuthClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    "Are you a shelter owner? Click here",
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

