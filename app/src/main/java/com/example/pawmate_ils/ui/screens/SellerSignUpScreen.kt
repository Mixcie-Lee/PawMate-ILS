package com.example.pawmate_ils.ui.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.AdopShelDataStruc.AdopterRepository
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.firebase_models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterOwnerSignUpScreen(
    navController: NavController,
    onSignUpClick: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onUserAuthClick: () -> Unit,
    sharedViewModel : SharedViewModel
) {
    // I COMMENT THE ON SIGN UP CLICK FUNCTION YOU'VE MADE SINCE IT WILL BE HANDLED BY FIREBASE
    //Firebase Initializations & stuffs related to firebase
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val AuthViewModel : AuthViewModel = viewModel()
    val authState  = AuthViewModel.authState.observeAsState()
    val firestoreRepo = remember { FirestoreRepository() }
    val currentUserRole by AuthViewModel.currentUserRole.collectAsState()


    var currentStep by remember { mutableStateOf(1) } // 1: Email, 2: About You
    var email by remember { mutableStateOf("") }
    var shelterName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var isGoogleLoading by remember {mutableStateOf(false)}


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
            // Attempt Firebase sign-up with Google
            AuthViewModel.signUpWithGoogle(idToken) { success, error ->
                println("Google SignIn success: $success")
                isGoogleLoading = false
                if (success) {
                    currentStep = 2
                } else {
                    errorMessage = "Google Sign-In failed. Try again."
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
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            when (currentStep) {
                1 -> {
                    // Email Step
                    Text(
                        text = "PawMate",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Text(
                        text = "Create an account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Enter your email to sign up for this app",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("name@example.com", color = Color.Black) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("password", color = Color.Black) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email"
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Please enter a valid email address"
                                return@Button
                            }
                            isLoading = true
                            AuthViewModel.signUp(email, password) { success, message ->
                                isLoading = false
                                if (success) {
                                    currentStep = 2
                                    AuthViewModel.fetchUserRole()
                                } else {
                                    errorMessage = message
                                }
                            }// Move to About You step

                        }, // Move to About You step

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBrown,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "or",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Google Sign In Button
                    OutlinedButton( // BUTTON FOR GOOGLE SIGN UP
                        onClick = {
                            AuthViewModel.fetchUserRole()
                            /* Handle Google Sign In */
                            isGoogleLoading = true
                            val gsoClient = getGoogleSignInClient(context)
                            launcher.launch(gsoClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
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


                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "By clicking continue, you agree to our Terms of Service and Privacy Policy.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                2 -> {
                    // About You Step
                    Text(
                        text = "About you",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = shelterName,
                        onValueChange = {shelterName = it },
                        placeholder = { Text("Shelter Name", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                              focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        placeholder = { Text("Owner name", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        placeholder = { Text("Mobile number", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = { Text("Address", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = establishedYear,
                        onValueChange = { establishedYear = it },
                        placeholder = { Text("Established Year", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            when {
                                shelterName.isBlank() -> {
                                    errorMessage = "Please enter your shelter name"
                                    return@Button
                                }
                                ownerName.isBlank() -> {
                                    errorMessage = "Please enter owner name"
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
                                    errorMessage = "Please enter your established year"
                                    return@Button
                                }
                                else -> {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch{
                                        //STORING DATA AFTER AUTHENTICATING IN THE STEP 1
                                        /*Please do not delete this, kahit duplicated sya from auth screen kailangan sya para
                                         makapag input ng data sa firestore. Di kasi nagana pag walang ganto sa auth screen or kapag wala
                                         naman ganto dito. So kailangan meron ganto sa authscreen at dito.
                                        */
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                        if (uid == null) {
                                            // user not signed in (maybe Auth step failed)
                                            errorMessage =
                                                "User not signed in. Please complete Step 1."
                                            isLoading = false
                                            return@launch
                                        }
                                        val adopterRepo = AdopterRepository()
                                        val shelterRepo = ShelterRepository()


                                        val user = User(
                                            id = uid,
                                            name = shelterName,
                                            email = email,
                                            MobileNumber = mobileNumber,
                                            Address = address,
                                            Age = establishedYear,
                                            role = "shelter" //
                                        )
                                        try {
                                            firestoreRepo.addUser(user)
                                            if(user.role == "adopter"){
                                                adopterRepo.addAdopter(user)
                                            }else if(user.role == "shelter"){
                                                shelterRepo.addShelter(user)
                                            }


                                            AuthViewModel.fetchUserRole()

                                            sharedViewModel.username.value = shelterName
                                            onSignUpClick(shelterName, email, ownerName, mobileNumber)
                                            delay(1)
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
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBrown,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Continue",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}