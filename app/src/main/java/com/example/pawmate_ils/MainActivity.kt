        package com.example.pawmate_ils

        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import androidx.activity.enableEdgeToEdge
        import androidx.compose.foundation.layout.Box
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.foundation.layout.padding
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.Text
        import androidx.compose.material3.Scaffold
        import android.app.Application
        import androidx.compose.material3.Surface
        import androidx.compose.ui.Modifier
        import androidx.navigation.compose.NavHost
        import androidx.navigation.compose.composable
        import androidx.navigation.compose.rememberNavController
        import com.example.pawmate_ils.ui.screens.LoginScreen
        import com.example.pawmate_ils.ui.screens.SignUpScreen
        import com.example.pawmate_ils.ui.screens.UserTypeSelectionScreen
        import TinderLogic_PetSwipe.PetSwipeScreen
        import androidx.activity.viewModels
        import androidx.compose.runtime.LaunchedEffect
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.ui.platform.LocalContext
        import androidx.lifecycle.viewmodel.compose.viewModel
        import com.example.pawmate_ils.SharedViewModel
        import com.example.pawmate_ils.ThemeManager
        import com.example.pawmate_ils.ui.screens.AdoptionCenterDashboard
        import com.example.pawmate_ils.ui.screens.AddPetScreen
        import com.example.pawmate_ils.ui.theme.PawMateILSTheme
        import TinderLogic_PetSwipe.AdopterLikeScreen
        import android.util.Log
        import androidx.compose.runtime.livedata.observeAsState
        import androidx.navigation.NavType
        import androidx.navigation.navArgument
        import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewMdelFactory
        import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
        import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
        import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
        import com.example.pawmate_ils.Firebase_Utils.ChatViewModelFactory
        import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
        import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
    import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
    import com.example.pawmate_ils.Firebase_Utils.PetsRepository
        import com.example.pawmate_ils.chatScreen.ChatScreen
        import com.example.pawmate_ils.ui.screens.ProfileSettingsScreen
        import com.example.pawmate_ils.ui.screens.AccountSettingsScreen
        import com.example.pawmate_ils.onboard.OnboardingScreen
        import com.example.pawmate_ils.onboard.OnboardingUtil
        //import com.example.pawmate_ils.chatScreen.ChatScreen
        //import com.example.pawmate_ils.ui.screens.AdoptionCenterApplications
        import com.example.pawmate_ils.chatScreen.HomeScreen
        import com.example.pawmate_ils.ui.screens.EducationalScreen
        import com.example.pawmate_ils.ui.screens.ShopScreen
        import com.example.pawmate_ils.ui.screens.EducationalDetailScreen
        import com.example.pawmate_ils.ui.screens.SellerLoginScreen
        import com.example.pawmate_ils.ui.screens.SellerSignUpScreen
        import com.example.pawmate_ils.ui.screens.AdoptionCenterPets
        import com.example.pawmate_ils.ui.screens.EditPetScreen
        import com.example.pawmate_ils.ui.screens.ShelterProfileScreen
        import com.example.pawmate_ils.ui.screens.HelpSupportScreen
        import com.example.pawmate_ils.ui.screens.AboutScreen
        import com.google.firebase.Firebase
        import com.google.firebase.app
        import com.google.firebase.auth.FirebaseAuth
        import com.google.firebase.firestore.FirebaseFirestore
        import kotlinx.coroutines.Dispatchers
        import kotlinx.coroutines.tasks.await
        import kotlinx.coroutines.withContext

        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.setValue
        import androidx.compose.material3.AlertDialog
        import androidx.compose.material3.Button
        import androidx.compose.material3.ButtonDefaults
        import androidx.compose.material3.TextButton
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.foundation.shape.RoundedCornerShape
        import androidx.compose.material3.CircularProgressIndicator
        import androidx.compose.runtime.collectAsState
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.unit.dp
        import kotlinx.coroutines.delay


        class MainActivity : androidx.fragment.app.FragmentActivity() {
            private val sharedViewModel: SharedViewModel by viewModels()
            private val authViewModel: AuthViewModel by viewModels()





            //STATUS OF USER(ONLINE OR OFFLINE) IMMEDIATELY INTIALIZED UPON STARTING
            override fun onStart() {
                super.onStart()
                // 🟢 Only set online if there's an actual active session
                if (FirebaseAuth.getInstance().currentUser != null) {
                    authViewModel.updateOnlineStatus(true)
                }
            }

            override fun onStop() {
                super.onStop()
                // 🔴 Set Offline when app goes to background
                // We use a safe check here as well
                if (FirebaseAuth.getInstance().currentUser != null) {
                    authViewModel.updateOnlineStatus(false)
                }
            }




            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                GemManager.init(applicationContext)
                CloudinaryHelper.init(this)




                setContent {







                    PawMateILSTheme(darkTheme = ThemeManager.isDarkMode) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            /*FIREBASE FUNCTIONALITIES AND OTHER TOOLS FOR AUTHENTICATED USERS(LOGGED IN/SIGNED IN) BYPASS THE SIGN UP PAGE
                              AUTHENTICATED USERS WILL GO STRAIGHT FROM THEIR RESPECTIVE DESTINATION PAGE(ADOPTER HOME/SHELTER HOME)
                              !DO NOT DELETE!
                            */
                            val authState = authViewModel.authState.observeAsState()
                            val db = remember { FirestoreRepository() }
                            val adoptionCenterViewModel: AdoptionCenterViewModel = viewModel(
                                factory = AdoptionCenterViewMdelFactory(authViewModel)
                            )

                            // Pre-warm: these fire Firestore listeners in init{},
                            // populating the local cache while the welcome screen shows.
                            // The swipe screen's own ViewModels then hit warm cache instantly.
                            val preWarmPets: PetsRepository = viewModel()
                            val preWarmHome: HomeViewModel = viewModel()
                            val preWarmLiked: LikedPetsViewModel = viewModel()

                            val context = LocalContext.current
                            val navController = rememberNavController()
                            val onboardingUtil = OnboardingUtil(applicationContext)

                            //HANDLER FUNCTION THAT CHECKS IF THE NEW USER HAVE PHOTOURI because without this the image
                            //in the message would not load leaving the profile icon blank







                            LaunchedEffect(Unit) {
                                authViewModel.checkSessionExpiry(context) {
                                    // 🚨 This callback triggers if the 7-day threshold is met
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    android.widget.Toast.makeText(
                                        context,
                                        "Session expired for your security. Please log in again.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }


                            val startDestination = remember {
                                val isUserAuthenticated = FirebaseAuth.getInstance().currentUser != null
                                val isCompleted = onboardingUtil.isOnboardingCompleted()

                                when {
                                    !isCompleted && !isUserAuthenticated -> "onboarding"
                                    isUserAuthenticated -> "welcome_popup"
                                    isCompleted -> "user_type"
                                    else -> "onboarding"
                                }
                            }
                            val currentUserRole by authViewModel.currentUserRole.collectAsState()

                            LaunchedEffect(authState.value, currentUserRole) {
                                if (!onboardingUtil.isOnboardingCompleted()) return@LaunchedEffect

                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val currentRoute = navController.currentBackStackEntry?.destination?.route

                                // [NEW FIX]: Prevent re-navigating if we are already on a Home screen
                                if (currentRoute == "adoption_center_dashboard" || currentRoute == "pet_swipe") return@LaunchedEffect
                               // if (currentRoute == "welcome_popup") return@LaunchedEffect

                                if (currentUser != null) {
                                    // [NEW FIX]: Force a fetch if the role is currently null
                                    if (currentUserRole == null) {
                                        authViewModel.fetchUserRole()
                                    } else {
                                        if (currentRoute == "welcome_popup") {
                                            // Matches the duration in your WelcomePopupScreen.kt
                                            delay(1800L)
                                        }
                                        handleRoleBasedNavigation(authViewModel, navController)
                                    }
                                }

                                if (currentUser == null && currentRoute != "login" && currentRoute != "signup") {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }



                            // Handle authenticated user navigation after NavHost is created
                            /* LaunchedEffect(authState.value) {
                                if (onboardingUtil.isOnboardingCompleted()) {
                                    val currentUser = AuthViewModel.currentUser
                                    if (currentUser != null) {
                                        try {
                                            val snapshot = db.usersCollection("users").document(currentUser.uid).get().await()
                                            val role = snapshot.getString("role")
                                            val destination = when (role) {
                                                "adopter" -> "pet_swipe"
                                                "shelter" -> "adoption_center_dashboard"
                                                else -> "user_type"
                                            }
                                            if (destination != "user_type") {
                                                navController.navigate(destination) {
                                                    popUpTo("user_type") { inclusive = true }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Stay on user_type if there's an error
                                        }
                                    }
                                }
                            }               */
                            LaunchedEffect(Unit) {
                                FirebaseAuth.getInstance().currentUser?.let {
                                    authViewModel.fetchUserRole()
                                }
                            }


                            NavHost(navController = navController, startDestination = startDestination) {
                                composable("onboarding") {
                                    OnboardingScreen(
                                        onComplete = {
                                            onboardingUtil.setOnboardingCompleted()
                                            navController.navigate("user_type") {
                                                popUpTo("onboarding") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("user_type") {
                                    UserTypeSelectionScreen(navController = navController)
                                }
                                composable("signup") {
                                    SignUpScreen(
                                        navController = navController,

                                        onSignUpClick = { _, _, _, _ ->
                                            handleRoleBasedNavigation(authViewModel, navController)
                                        },
                                        onLoginClick = {
                                            navController.navigate("login") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToSellerSignUp = {
                                            navController.navigate("seller_signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        sharedViewModel = sharedViewModel
                                    )
                                }
                                composable("login") {
                                    LoginScreen(
                                        authViewModel = authViewModel,
                                        onLoginSuccess = {
                                            handleRoleBasedNavigation(authViewModel, navController)
                                        },
                                        onSignUpClick = {
                                            navController.navigate("signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToShelter = { navController.navigate("seller_login") }

                                    )
                                }
                                composable("seller_signup") {
                                    SellerSignUpScreen(
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        onSignUpClick = { _, _, _, _ ->
                                            handleRoleBasedNavigation(authViewModel, navController)
                                        },
                                        onLoginClick = {
                                            navController.navigate("seller_login") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToAdopterSignUp = { // ✅ This matches your new parameter name
                                            navController.navigate("signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        sharedViewModel = sharedViewModel,

                                    )
                                }
                                composable("seller_login") {
                                    SellerLoginScreen(
                                        authViewModel = authViewModel,
                                        onLoginSuccess = {
                                            // 🚥 DYNAMIC NAVIGATION: Same here!
                                            handleRoleBasedNavigation(authViewModel, navController)
                                        },
                                        onSignUpClick = {
                                            navController.navigate("seller_signup") {
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToAdopter = {
                                            navController.navigate("login") // 💎 This goes back to Adopter side
                                        }

                                    )
                                }
                                composable("adoption_center_dashboard") {
                                    AdoptionCenterDashboard(
                                        navController = navController,
                                        adoptionCenterViewModel = adoptionCenterViewModel,
                                        centerName = "Your Shelter"
                                    )
                                }
                                composable("adoption_center_pets") {
                                    AdoptionCenterPets(
                                        navController = navController,
                                        viewModel = adoptionCenterViewModel,
                                        authViewModel = authViewModel,
                                        onBackClick = { navController.popBackStack() },
                                        onAddPet = { navController.navigate("add_pet") },

                                    )
                                }
                               /* composable("adoption_center_applications") {
                                    AdoptionCenterApplications(
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                */
                                composable("ShelterProfSettings") {
                                    // No need to redeclare authViewModel; it's already defined at the top of setContent
                                    val factory = AdoptionCenterViewMdelFactory(authViewModel)

                                    // 2. Get the adoptionCenterViewModel USING the factory
                                    val adoptionCenterViewModel: AdoptionCenterViewModel = viewModel(factory = factory)

                                    // 3. Pass it to the screen
                                    ShelterProfileScreen(
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        adoptionCenterViewModel = adoptionCenterViewModel
                                    )
                                }


                                composable("add_pet"){
                                    AddPetScreen(navController = navController, authViewModel = authViewModel)
                                }
                                composable("pet_swipe") {
                                    PetSwipeScreen(navController = navController)
                                }
                                composable("adopter_home") {
                                    AdopterLikeScreen(navController = navController)
                                }
                                // 🧩 --- CHAT ROUTES START HERE ---


                                composable("chat_home") {
                                    val factory = remember { ChatViewModelFactory(authViewModel) }
                                    val chatViewModel: ChatViewModel = viewModel(factory = factory)

                                    HomeScreen(navController, authViewModel, chatViewModel)
                                }
                                composable(
                                    "message/{channelId}",
                                    arguments = listOf(
                                        navArgument("channelId"){
                                            type = NavType.StringType
                                        }
                                    )
                                ) { backStackEntry ->
                                    val factory = remember { ChatViewModelFactory(authViewModel) }
                                    val chatViewModel : ChatViewModel = viewModel(factory = factory)
                                    val channelId =  backStackEntry.arguments?.getString("channelId") ?:""


                                    // ✅ Navigate to the actual message chat UI
                                    ChatScreen(
                                        navController = navController,
                                        channelId = channelId,
                                        authViewModel = authViewModel,
                                        chatViewModel = chatViewModel
                                    )

                                }

                                composable("welcome_popup") {
                                    val userRoleState = remember { mutableStateOf("adopter") }

                                    LaunchedEffect(Unit) {
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser != null) {
                                            try {
                                                val user = withContext(Dispatchers.IO) {
                                                    db.getUserById(currentUser.uid)
                                                }
                                                userRoleState.value = user?.role ?: "adopter"
                                            } catch (e: Exception) {
                                                userRoleState.value = "adopter"
                                            }
                                        }
                                    }

                                    WelcomePopupScreen(
                                        navController = navController,
                                        userType = userRoleState.value
                                    )
                                }



                                composable("profile_settings") {
                                    ProfileSettingsScreen(
                                        navController = navController,
                                        username = sharedViewModel.username.value ?: "User",
                                    )
                                }
                                composable("account_settings") {
                                    AccountSettingsScreen(navController = navController)
                                }
                                composable("educational") {
                                    EducationalScreen(navController = navController)
                                }
                                composable("shop") {
                                    ShopScreen(navController = navController)
                                }
                                composable(
                                    route = "educational_detail/{articleId}",
                                    arguments = listOf(
                                        navArgument("articleId") {
                                            type = NavType.IntType
                                        }
                                    )
                                ) { backStackEntry ->
                                    val articleId = backStackEntry.arguments?.getInt("articleId") ?: 1
                                    EducationalDetailScreen(
                                        navController = navController,
                                        articleId = articleId
                                    )
                                }
                                composable("help_support") {
                                    HelpSupportScreen(navController = navController)
                                }
                                composable("about_app") {
                                    AboutScreen(navController = navController)
                                }
                                //COMPOSABLE FOR EDIT PET SCREEN
                                // Inside your NavHost in MainActivity.kt
                                composable(
                                    route = "edit_pet_screen/{petId}",
                                    arguments = listOf(navArgument("petId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val petId = backStackEntry.arguments?.getString("petId") ?: ""
                                    EditPetScreen(
                                        navController = navController,
                                        viewModel = adoptionCenterViewModel,
                                        petId = petId,
                                        authViewModel = authViewModel
                                    )
                                }
                                // Inside NavHost(navController = navController, ...)
                                // Inside NavHost(navController = navController, ...) around line 450
                                composable(
                                    route = "profile_details/{userId}",
                                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val userId = backStackEntry.arguments?.getString("userId") ?: ""

                                    // 🚀 Use the NEW dynamic screen we built
                                    // This screen handles its own data fetching via the ViewModel
                                    com.example.pawmate_ils.ui.screens.ProfileDetailsScreen(
                                        targetUserId = userId,
                                        navController = navController,
                                        authViewModel = authViewModel
                                    )
                                }
                                }

                                }
                            }
                        }
                    }
                }


        //THIS SECTION IS FOR ORGANIZED ROLE BASED NAVIGATION, WAG TO PAPAKIELAMAN PLEASE HEHEHHE
        private fun handleRoleBasedNavigation(authViewModel: AuthViewModel, navController: androidx.navigation.NavHostController) {
            val role = authViewModel.currentUserRole.value
            val currentRoute = navController.currentBackStackEntry?.destination?.route

            // 🛑 If we are already where we need to be, STOP. This prevents the "reset" bug.
            if (role == "shelter" && currentRoute == "adoption_center_dashboard") return
            if (role == "adopter" && currentRoute == "pet_swipe") return

            if (role == null) {
                Log.d("NAV_DEBUG", "Role is null, staying put.")
                return
            }

            when (role) {
                "shelter" -> {
                    navController.navigate("adoption_center_dashboard") {
                        popUpTo("welcome_popup") { inclusive = true } // 👈 Change popUpTo(0) to this
                    }
                }
                "adopter" -> {
                    navController.navigate("pet_swipe") {
                        popUpTo("welcome_popup") { inclusive = true }
                    }
                }
            }
        }


