package com.example.pawmate_ils.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.auth.FirebaseAuth
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ui.theme.DarkBrown
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.AdopShelDataStruc.AdopterRepository
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerSignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onSignUpClick: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onSellerAuthClick: () -> Unit,
    sharedViewModel: SharedViewModel,

    ) {
    //FIREBASE AUTHENTICATION
    val context = LocalContext.current
    val AuthViewModel : AuthViewModel = viewModel()
    val authState  = AuthViewModel.authState.observeAsState()
    val firestoreRepo = remember { FirestoreRepository() }

    var currentStep by remember { mutableStateOf(1) } // 1: Email, 2: About You
    var email by remember { mutableStateOf("") }
    var password by remember {mutableStateOf(" ")} //MOCK UP LANG TO GA, PERO I NEED YOU TO ADD PASSWORD AS WELL SA SIGN UP FOR AUTHENTICATION
    var shelterName by remember { mutableStateOf("") }
    var OwnerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isGoogleLoading by remember {mutableStateOf(false)}

    //GOOGLE SIGN-UP/SIGN-IN HANDLER
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

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    isGoogleLoading = false
                    if (task.isSuccessful) {
                        // Move to About You step for additional info
                        currentStep = 2
                        email = account.email ?: ""
                    } else {
                        errorMessage = task.exception?.message ?: "Google Sign-In failed."
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
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            when (currentStep) {
                1 -> {
                    Image(
                        painter = painterResource(id = R.drawable.blackpawmateicon3),
                        contentDescription = "PawMate Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Create Account",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This is shelter sign up, sign up to get started!",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            placeholder = { Text("Enter your email", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                cursorColor = Color(0xFFFFB6C1)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            placeholder = { Text("Create a password", color = Color.Gray.copy(alpha = 0.6f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                cursorColor = Color(0xFFFFB6C1)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        )
                    }

                    Button( //THIS IS THE BUTTON THAT HANDLES SIGN UPP FOR EMAILS!!
                        onClick = {
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email"
                                return@Button
                            }
                            if (password.isBlank()) {
                                errorMessage = "Please enter password"
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Please enter a valid email address"
                                return@Button
                            }
                            errorMessage = null
                            AuthViewModel.signUp(email, password) { success, message ->
                                if (success) {
                                    currentStep = 2 // Move to "About You" step
                                } else {
                                    errorMessage = message ?: "Sign-up failed. Please try again."
                                }
                            }
                            currentStep = 2 // Move to About You step
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB6C1),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFFFB6C1).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "or",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    //Google Button
                    OutlinedButton(
                        onClick = {
                            AuthViewModel.fetchUserRole()
                            /* Handle Google Sign In */
                            isGoogleLoading = true
                            val gsoClient = getGoogleSignInClient(context)
                            launcher.launch(gsoClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF4285F4), strokeWidth = 2.dp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "G",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4285F4),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    "Continue with Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Already have an account? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = onLoginClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Log in",
                                color = Color(0xFFFF9999),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                2 -> {
                    Text(
                        text = "About You",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tell us more about yourself",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Shelter Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = shelterName,
                                onValueChange = { shelterName = it },
                                placeholder = { Text("Cainta Shelter", color = Color.Gray.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB6C1),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFFFFB6C1)
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Owner name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = OwnerName,
                                onValueChange = { OwnerName = it },
                                placeholder = { Text("Doe", color = Color.Gray.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB6C1),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFFFFB6C1)
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Mobile Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            placeholder = { Text("+63 234 567 8900", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                cursorColor = Color(0xFFFFB6C1)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            placeholder = { Text("123 Main Street", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                cursorColor = Color(0xFFFFB6C1)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Established Year",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = establishedYear,
                            onValueChange = { establishedYear = it },
                            placeholder = { Text("25", color = Color.Gray.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB6C1),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                cursorColor = Color(0xFFFFB6C1)
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "You must be 18 or older to use PawMate",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                shelterName.isBlank() -> {
                                    errorMessage = "Please enter your first name"
                                    return@Button
                                }

                                OwnerName.isBlank() -> {
                                    errorMessage = "Please enter your last name"
                                    return@Button
                                }

                                mobileNumber.isBlank() -> {
                                    errorMessage = "Please enter your mobile number"
                                    return@Button
                                }

                                address.isBlank() -> {
                                    errorMessage = "Please enter your address"
                                    return@Button
                                }

                                establishedYear.isBlank() -> {
                                    errorMessage = "Please enter your age"
                                    return@Button
                                }

                                establishedYear.toIntOrNull() == null -> {
                                    errorMessage = "Please enter a valid year"
                                    return@Button
                                }else -> {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                        if (uid == null) {
                                            errorMessage =
                                                "User not signed in. Please complete Step 1."
                                            isLoading = false
                                            return@launch
                                        }
                                        val user = User(
                                            id = uid,
                                            name = "$shelterName $OwnerName",
                                            email = email,
                                            MobileNumber = mobileNumber,
                                            Address = address,
                                            Age = establishedYear,
                                            role = "shelter",
                                            gems = 10,
                                            likedPetsCount = 0,
                                        )
                                        val adopterRepo = AdopterRepository()
                                        val shelterRepo = ShelterRepository()



                                        try {
                                            firestoreRepo.addUser(user)
                                            if(user.role == "adopter"){
                                                adopterRepo.addAdopter(user)
                                            }else if(user.role == "shelter"){
                                                shelterRepo.addShelter(user)
                                            }

                                            AuthViewModel.fetchUserRole()

                                            onSignUpClick(shelterName, email, OwnerName, mobileNumber)
                                            sharedViewModel.username.value = "$shelterName $OwnerName"
                                            //saves username locally, so it persists across app restarts
                                            val settings = SettingsManager(context)
                                            settings.setUsername("$shelterName $OwnerName")

                                            delay(50)
                                            navController.navigate("adoption_center_dashboard") {
                                                popUpTo("user_type") { inclusive = true }
                                            }
                                        } catch (e: Exception){
                                            errorMessage = e.message ?: "Failed to save user data"
                                        }finally{
                                            isLoading = false
                                        }


                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB6C1),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFFFB6C1).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Complete Sign Up",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            errorMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFC62828),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

