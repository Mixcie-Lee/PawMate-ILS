package TinderLogic_PetSwipe

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import android.content.res.Configuration
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.GemPackage
import com.example.pawmate_ils.LikedPetsManager
import com.example.pawmate_ils.ThemeManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewMdelFactory
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModelFactory
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.Firebase_Utils.LikedPet
import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
import com.example.pawmate_ils.Firebase_Utils.PetsRepository
import com.example.pawmate_ils.R
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import com.example.pawmate_ils.ui.screens.ProfileRequirementDialog
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.google.firebase.Timestamp

data class PetData(
    val petId: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val type: String? = null,
    val imageRes: Int = 0,
    val additionalImages: List<Int> = emptyList(),
    val shelterId: String? = null,
    val shelterName: String? = null,
    val validationStatus: Boolean = false

)
enum class PetFilter {
    ALL, DOGS, CATS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PetSwipeScreen(navController: NavController) {
    var currentPetIndex by remember { mutableIntStateOf(0) }
    val likedPets = remember { mutableListOf<String>() }
    var petFilter by remember { mutableStateOf(PetFilter.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var offsetX by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var rotation by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var showGemDialog by remember { mutableStateOf(false) }
    //Firebase essentials for authentication needed for chat creation
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val authViewModel: AuthViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner!!
    )
    val factory = remember { ChatViewModelFactory(authViewModel) }
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val firestoreRepo = remember { FirestoreRepository() }
    val likedPetsViewmodel: LikedPetsViewModel = viewModel()
    val petRepository : PetsRepository = viewModel()
    //INITIALIZING THE VARIABLE USED FOR CONFIMATION PURCHASE DIALOG
    val pendingBuy =  GemManager.pendingPackage

    val homeViewModel: HomeViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tutorialPrefs = remember(context) {
        context.getSharedPreferences(
            "swipe_tutorial",
            android.content.Context.MODE_PRIVATE
        )
    }
    val tutorialSeen = remember { tutorialPrefs.getBoolean("seen", false) }
    // Prevent tutorial/info from reopening on every revisit.
    var showTutorial by rememberSaveable { mutableStateOf(!tutorialSeen && !TutorialRuntime.wasShownThisProcess) }
    LaunchedEffect(showTutorial) {
        if (showTutorial) TutorialRuntime.wasShownThisProcess = true
    }

    //Instanly show chat channels after swiping right
    LaunchedEffect(Unit) {
        homeViewModel.listenToChannels()
    }
    LaunchedEffect(Unit) { GemManager.init(context) } // ensure saved gem count is loaded
    val gemCount by GemManager.gemCount.collectAsState()

    //allow for adopters to see newly added pets




    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    // Determine if device is tablet based on smallest width (more reliable) and fall back to widthDp
    val isTablet = configuration.smallestScreenWidthDp >= 600 || configuration.screenWidthDp >= 600
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)

    // Responsive dimensions (larger cards on tablets; also larger in portrait)
    val cardWidth = when {
        isTablet && isPortrait -> screenWidth * 0.86f
        isTablet && !isPortrait -> screenWidth * 0.70f
        !isTablet && isPortrait -> screenWidth * 0.92f
        else -> screenWidth * 0.70f
    }
    val cardHeight = when {
        isTablet && isPortrait -> screenHeight * 0.74f
        isTablet && !isPortrait -> screenHeight * 0.82f
        !isTablet && isPortrait -> screenHeight * 0.70f
        else -> screenHeight * 0.82f
    }
    val titleFontSize = if (isTablet) 34.sp else 24.sp
    val bodyFontSize = if (isTablet) 18.sp else 14.sp
    val paddingSize = if (isTablet) 28.dp else 16.dp


    val adoptionViewModel: AdoptionCenterViewModel = viewModel(
        factory = AdoptionCenterViewMdelFactory(authViewModel)
    )
    var firestorePets by remember { mutableStateOf<List<PetData>>(emptyList()) }
    LaunchedEffect(Unit) {
        adoptionViewModel.observePets { pets ->
            firestorePets = pets
        }
    }
     val allPets by petRepository.allPets.collectAsState()


        /* val allPets: List<PetData> by remember {
        derivedStateOf<List<PetData>> {
            (petRepository.allPets.value + firestorePets)
                .distinctBy { pet: PetData -> pet.name + pet.shelterId }
        }
    }
    /
         */


   //TO ESSENTIAL TOOLS TO KEEP DDING PETS DYNAMIC, IF SHELTER ADD PET A BLANK CARD WILL APPEAR
    val filteredPets by remember(allPets, petFilter) {
        derivedStateOf {
            when (petFilter) {
                PetFilter.ALL -> allPets
                PetFilter.DOGS -> allPets.filter { it.type == "dog" }
                PetFilter.CATS -> allPets.filter { it.type == "cat" }
            }
        }
    }
    LaunchedEffect(filteredPets.size) {
        if (currentPetIndex >= filteredPets.size) {
            currentPetIndex = filteredPets.size - 1
        }
    }


    //This is the handler for a dialog that ask the user to upload or create their profile picture
    //without this the avatar icon wouldnt appear if they change it mid use
    ProfileRequirementDialog(
        authViewModel = authViewModel,
        navController = navController,
        canShow = !showTutorial
    )



    @SuppressLint("SuspiciousIndentation")
    fun swipeCard(direction: Float) {
        if (isDragging || currentPetIndex >= filteredPets.size) return

        if (direction > 0) {
            if (gemCount >= 5) {
                repeat(5) { GemManager.consumeGem() }
                val currentPet = filteredPets[currentPetIndex]
                likedPetsViewmodel.addLikedPet(currentPet)

                if (currentPetIndex == filteredPets.lastIndex) {
                    petRepository.appendBlankCard(
                        PetData(name = "Blank Card", type = currentPet.type ?: "dog")
                    )
                }


                // Move to next card to avoid duplicate swipes

                scope.launch {

                    try {
                        // Make sure shelter info comes from pet data
                        val shelterId = currentPet.shelterId
                        if (shelterId.isNullOrEmpty()) {
                            Log.w("PetSwipe", "❌ Pet has no shelterId: ${currentPet.name}")
                            return@launch
                        }

                        val allUsers = firestoreRepo.getAllUsers()
                        val shelterUser = allUsers.find { it.id == shelterId }
                        if (shelterUser == null) {
                            Log.w("PetSwipe", "❌ Shelter not found for pet: ${currentPet.name}")
                            return@launch
                        }

                        val adopterId = authViewModel.currentUser?.uid ?: return@launch
                        val adopterUser = allUsers.find { it.id == adopterId }
                        val adopterPhoto = adopterUser?.photoUri // 🆕 GRAB THE ADOPTER PHOTO
                        val shelterPhoto = shelterUser.photoUri
                        val adopterName = authViewModel.currentUser?.displayName ?: "Unknown"
                        val petName = currentPet.name

                        // Prevent duplicate channel
                        val existingChannel = homeViewModel.channels.value.firstOrNull {
                            it.adopterId == adopterId &&
                                    it.shelterId == shelterId &&
                                    it.petName == petName
                        }
                        if (existingChannel != null) {
                            Log.d("PetSwipe", "⚠️ Channel already exists. Skipping creation.")
                            return@launch
                        }

                        val channel = Channel(
                            channelId = "$adopterId-$shelterId-${petName ?: "Unknown"}",
                            adopterId = adopterId,
                            adopterName = adopterName,
                            adopterPhotoUri = adopterPhoto,
                            shelterId = shelterId,
                            shelterName = shelterUser.name,
                            shelterPhotoUri = shelterPhoto, // ✅ ADD THIS
                            petName = petName ?: "Unknown",
                            lastMessage = "",
                            timestamp = System.currentTimeMillis(),
                            unreadCount = 0,
                            createdAt = System.currentTimeMillis()
                        )

                        Log.d("PetSwipe", "✅ Creating channel: $channel")
                        homeViewModel.addChannel(channel)

                    } catch (e: Exception) {
                        Log.e("PetSwipe", "❌ Error creating channel", e)
                    }
                }
            } else {
                showGemDialog = true
            }
        }

       val nextIndex = currentPetIndex + 1

        if (nextIndex < filteredPets.size) {
            isDragging = true
            scope.launch {
                val targetX = if (direction > 0) 1200f else -1200f

                // Single pass: animate card off-screen, then instantly snap new card centered.
                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 185, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 24f).coerceIn(-18f, 18f)
                }
                // Switch to next card and snap back to center without a second animation to avoid hitch.
                currentPetIndex = nextIndex
                offsetX = 0f
                rotation = 0f
                isDragging = false
            }
        } else {
            scope.launch {
                val targetX = if (direction > 0) 1200f else -1200f

                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 185, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 24f).coerceIn(-18f, 18f)
                }

                currentPetIndex = filteredPets.size
            }
        }
    }

    fun resetCardPosition() {
        if (isDragging || currentPetIndex >= filteredPets.size) return

        scope.launch {
            // Keep this quick to avoid visible stalls during partial drags
            animate(
                initialValue = offsetX,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 135, easing = FastOutSlowInEasing)
            ) { value, _ ->
                offsetX = value
                rotation = (value / 55f).coerceIn(-12f, 12f)
            }
        }
    }
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            AdopterBottomBar(navController = navController, selectedTab = "Home")
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            if (currentPetIndex >= filteredPets.size) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (petFilter) {
                            PetFilter.DOGS -> "🐕 All Dogs Viewed!"
                            PetFilter.CATS -> "🐱 All Cats Viewed!"
                            PetFilter.ALL -> "🐾 All Pets Viewed!"
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9999),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "You liked ${likedPets.size} pets",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    if (likedPets.isNotEmpty()) {
                        Text(
                            text = "Your pet matches:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        likedPets.forEach { petName ->
                            Text(
                                text = "❤️ $petName",
                                fontSize = 14.sp,
                                color = Color(0xFFFF9999),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Button(
                        onClick = {
                            try {
                                navController.navigate("adopter_home") {
                                    popUpTo("pet_swipe") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB6C1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Continue to Home",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            currentPetIndex = 0
                            likedPets.clear()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Swipe Again",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingSize, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (isTablet) 20.dp else 8.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pawmate_logo),
                                contentDescription = "PawMate Logo",
                                modifier = Modifier.size(if (isTablet) 64.dp else 56.dp)
                            )

                            Column {
                                Text(
                                    text = when (petFilter) {
                                        PetFilter.DOGS -> "Find Dogs"
                                        PetFilter.CATS -> "Find Cats"
                                        PetFilter.ALL -> "Discover"
                                    },
                                    fontSize = if (isTablet) 32.sp else 28.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (petFilter) {
                                        PetFilter.DOGS -> "Your perfect companion"
                                        PetFilter.CATS -> "Your purrfect friend"
                                        PetFilter.ALL -> "Your new best friend"
                                    },
                                    fontSize = if (isTablet) 16.sp else 14.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = accentColor),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "💎",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = gemCount.toString(),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { navController.navigate("profile_settings") },
                                modifier = Modifier
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = textColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                onClick = {
                                    petFilter = PetFilter.DOGS
                                    currentPetIndex = 0
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (petFilter == PetFilter.DOGS) primaryColor else cardColor
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Dogs",
                                    color = if (petFilter == PetFilter.DOGS) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }

                            Card(
                                onClick = {
                                    petFilter = PetFilter.CATS
                                    currentPetIndex = 0
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (petFilter == PetFilter.CATS) primaryColor else cardColor
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Cats",
                                    color = if (petFilter == PetFilter.CATS) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        }

                        FilledIconButton(
                            onClick = { showGemDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Buy Gems",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                            .padding(horizontal = if (isTablet) 0.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentPetIndex + 1 < filteredPets.size) {
                            val backgroundScale by animateFloatAsState(
                                targetValue = if (abs(offsetX) > 100f) 0.98f else 0.95f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "background_scale"
                            )

                            SwipeablePetCard(
                                pet = filteredPets[currentPetIndex + 1],
                                offsetX = 0f,
                                rotation = 0f,
                                onDrag = { },
                                onDragEnd = { },
                                isTablet = isTablet,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(backgroundScale)
                            )
                        }

                        SwipeablePetCard(
                            pet = filteredPets[currentPetIndex],
                            offsetX = offsetX,
                            rotation = rotation,
                            onDrag = { deltaX ->
                                if (!isDragging) {
                                    val target = (offsetX + (deltaX * 0.9f)).coerceIn(-600f, 600f)
                                    // Smoothen drag updates to reduce jitter on slower devices
                                    offsetX += (target - offsetX) * 0.72f
                                    rotation = (offsetX / 60f).coerceIn(-14f, 14f)
                                }
                            },
                            onDragEnd = {
                                if (!isDragging) {
                                    if (abs(offsetX) > 150f) {
                                        swipeCard(if (offsetX > 0) 1f else -1f)
                                    } else {
                                        resetCardPosition()
                                    }
                                }
                            },
                            isTablet = isTablet,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Tutorial (Dialog + screen arrows)
                    if (showTutorial) {
                        var tutorialStep by rememberSaveable { mutableStateOf(0) }

                        // Arrow overlays pointing to UI
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (tutorialStep) {
                                0 -> { // Info button (top-right)
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 56.dp, end = 24.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text("⬅", fontSize = 28.sp, color = Color.White)
                                        Text("Info", fontSize = 14.sp, color = Color.White)
                                    }
                                }
                                1 -> { // Filter button (top-right)
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 56.dp, end = 72.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text("⬅", fontSize = 28.sp, color = Color.White)
                                        Text("Filter", fontSize = 14.sp, color = Color.White)
                                    }
                                }
                                2 -> { // Buy Gems small FAB (top row right)
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 60.dp, end = 16.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text("⬅", fontSize = 28.sp, color = Color.White)
                                        Text("Buy Gems", fontSize = 14.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        AlertDialog(
                            onDismissRequest = { showTutorial = false },
                            title = {
                                Text(
                                    text = when (tutorialStep) {
                                        0 -> "Info Button"
                                        1 -> "Filter Pets"
                                        2 -> "Buy Gems"
                                        else -> "Welcome to Swipe"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9999)
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    when (tutorialStep) {
                                        0 -> {
                                            Text("Tap the info icon to view this tutorial anytime.", fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                        1 -> {
                                            Text("Use the filter icon to switch between Dogs, Cats, or All.", fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                        2 -> {
                                            Text("Tap the + button to buy gems. Adopts cost 5 gems.", fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Swipe right to adopt (costs 5 gems)", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Swipe left for next pet", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Tap left/right on the card to change photos", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Hold the card to flip and see details", fontSize = 14.sp, color = Color.DarkGray)
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (tutorialStep < 2) {
                                        TextButton(onClick = { tutorialStep++ }) { Text("Next") }
                                    } else {
                                        Button(
                                            onClick = {
                                                tutorialPrefs.edit().putBoolean("seen", true).apply()
                                                showTutorial = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White)
                                        ) { Text("Start swiping") }
                                    }
                                }
                            },
                            dismissButton = {
                                if (tutorialStep > 0) {
                                    TextButton(onClick = { tutorialStep-- }) { Text("Back") }
                                } else {
                                    TextButton(onClick = { showTutorial = false }) { Text("Close") }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
          //PURCHASE CONFIRMATION DIALOG
            // Listen for the pending package from GemManager
            val pendingBuy = GemManager.pendingPackage

            if (pendingBuy != null) {
                AlertDialog(
                    onDismissRequest = { GemManager.cancelPurchase() },
                    title = { Text("Confirm Purchase", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Invest ${pendingBuy.price} for ${pendingBuy.gemAmount} Gems to help more pets?")
                    },
                    confirmButton = {
                        Button(
                            onClick = { GemManager.confirmPurchase() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { GemManager.cancelPurchase() }) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }





            if (showGemDialog) {
                GemPurchaseDialog(
                    onDismiss = { showGemDialog = false },
                    onPurchase = { packageType ->
                        GemManager.initiatePurchase(packageType)
                        GemManager.gemCount
                        showGemDialog = false
                    }
                )
            }

            if (showFilterDialog) {
                FilterDialog(
                    currentFilter = petFilter,
                    onFilterSelected = { selectedFilter ->
                        petFilter = selectedFilter
                        currentPetIndex = 0
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeablePetCard(
    pet: PetData,
    offsetX: Float,
    rotation: Float,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    //MAKING SHELTER NAME APPEAR IN THE BOTTOM RIGHT, "SHELTER OWNERSHIP LOGIC", SHELTER 1 OWNS PET 1
    //SHELTER 2 OWNS PET 2
    var currentImageIndex by remember(pet.name) { mutableIntStateOf(0) }
    var isFlipped by remember(pet.petId) { mutableStateOf(false) }

     var shelterName by remember {mutableStateOf("Loading")}
    val density = LocalDensity.current.density
    val shelterRepository = remember { ShelterRepository() }

    LaunchedEffect(pet.shelterId) {
        if (pet.shelterId.isNullOrEmpty()) {
            shelterName = "No shelter found"
            return@LaunchedEffect
        }

        try {
            Log.d("SwipeablePetCard", "Fetching shelter for petId=${pet.name}, shelterId=${pet.shelterId}")
            val name = shelterRepository.getShelterNameById(pet.shelterId!!) // now safe because we checked
            Log.d("SwipeablePetCard", "Found shelter name: $name")
            shelterName = name ?: "Unknown Shelter"
        } catch (e: Exception) {
            Log.e("SwipeablePetCard", "Error fetching shelter name: ${e.message}")
            shelterName = "No shelter found"
        }
    }



    // Dynamic scale with subtle pulse when idle
    val idlePulse by rememberInfiniteTransition(label = "idle_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isFlipped -> 0.98f
            abs(offsetX) > 100f -> 1.05f
            abs(offsetX) < 10f -> idlePulse
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    // Dynamic elevation based on interaction
    val elevation by animateFloatAsState(
        targetValue = when {
            isFlipped -> 14f
            abs(offsetX) > 50f -> 16f
            else -> 8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_elevation"
    )

    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "flip_rotation"
    )

    // Responsive sizing for card content
    val nameSize = if (isTablet) 32.sp else 28.sp
    val infoSize = if (isTablet) 20.sp else 16.sp
    val descSize = if (isTablet) 18.sp else 14.sp
    val cardPadding = if (isTablet) 24.dp else 16.dp
    val indicatorSize = if (isTablet) 12.dp else 8.dp
    val indicatorSpacing = if (isTablet) 12.dp else 8.dp
    val cardWidth = if (isTablet) 400.dp else 320.dp  // NEW: width
    val cardHeight = if (isTablet) 520.dp else Dp.Unspecified

    Card(
        modifier = modifier
            .width(cardWidth)
            .heightIn(min = 450.dp, max = 600.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation * 0.65f
                rotationY = flipRotation + (offsetX / 45f).coerceIn(-12f, 12f)
                rotationX = (abs(offsetX) / 90f).coerceIn(0f, 6f)
                cameraDistance = 28f * density
                shadowElevation = elevation
            }
            .pointerInput(pet.name) {
                detectHorizontalDragGestures(
                    onDragEnd = { onDragEnd() }
                ) { _, dragAmount ->
                    onDrag(dragAmount)
                }
            }
            .pointerInput(pet.name + "_tap") {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width.toFloat()
                        val tapRatio = if (width > 0f) offset.x / width else 0.5f
                        val totalImages = pet.additionalImages.size + 1

                        if (!isFlipped && totalImages > 1 && (tapRatio < 0.28f || tapRatio > 0.72f)) {
                            currentImageIndex = if (tapRatio < 0.5f) {
                                if (currentImageIndex > 0) currentImageIndex - 1 else totalImages - 1
                            } else {
                                (currentImageIndex + 1) % totalImages
                            }
                        }
                    },
                    onLongPress = {
                        isFlipped = true
                    },
                    onPress = {
                        tryAwaitRelease()
                        isFlipped = false
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backgroundOverlay by animateColorAsState(
                targetValue = when {
                    offsetX > 100f -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                    offsetX < -100f -> Color(0xFFF44336).copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                animationSpec = tween(300),
                label = "background_overlay"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundOverlay)
            )

            val currentImage = when {
                currentImageIndex == 0 -> pet.imageRes.takeIf { it != 0 } ?: R.drawable.placeholder
                currentImageIndex <= pet.additionalImages.lastIndex + 1 && pet.additionalImages.isNotEmpty() -> {
                    pet.additionalImages.getOrNull(currentImageIndex - 1) ?: R.drawable.placeholder
                }
                else -> R.drawable.placeholder
            }


            Image(
                painter = painterResource(id = currentImage),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Enhanced photo indicators with animations
            if (pet.additionalImages.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(cardPadding),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(indicatorSpacing)
                    ) {
                        repeat(pet.additionalImages.size + 1) { index ->
                            val isSelected = index == currentImageIndex
                            val indicatorScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.2f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessHigh
                                ),
                                label = "indicator_scale_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .size(indicatorSize)
                                    .scale(indicatorScale)
                                    .background(
                                        color = if (isSelected)
                                            Color.White
                                        else
                                            Color.White.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // Enhanced swipe indicators with animations
            androidx.compose.animation.AnimatedVisibility(
                visible = offsetX > 50f,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                val likeAlpha by animateFloatAsState(
                    targetValue = (offsetX / 200f).coerceIn(0.3f, 1f),
                    label = "like_alpha"
                )
                val likeScale by animateFloatAsState(
                    targetValue = 1f + (offsetX / 400f).coerceIn(0f, 0.3f),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "like_scale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(likeScale)
                        .background(
                            Color(0xFF2E7D32).copy(alpha = likeAlpha),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🐾",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "ADOPT",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = offsetX < -50f,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                val passAlpha by animateFloatAsState(
                    targetValue = (abs(offsetX) / 200f).coerceIn(0.3f, 1f),
                    label = "pass_alpha"
                )
                val passScale by animateFloatAsState(
                    targetValue = 1f + (abs(offsetX) / 400f).coerceIn(0f, 0.3f),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pass_scale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(passScale)
                        .background(
                            Color(0xFF455A64).copy(alpha = passAlpha),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏭",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "NEXT",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val frontAlpha by animateFloatAsState(
                targetValue = if (flipRotation <= 90f) 1f else 0f,
                animationSpec = tween(140),
                label = "front_alpha"
            )
            val backAlpha by animateFloatAsState(
                targetValue = if (flipRotation > 90f) 1f else 0f,
                animationSpec = tween(140),
                label = "back_alpha"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(frontAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.38f),
                                    Color.Black.copy(alpha = 0.82f)
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(cardPadding)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = pet.name ?: "Unknown",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = nameSize
                            )
                        )
                        Text(
                            text = pet.age ?: "Unknown",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = infoSize,
                            fontWeight = FontWeight.Medium
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = pet.breed ?: "Unknown",
                                color = Color.White,
                                fontSize = descSize,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "Shelter: $shelterName",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = infoSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.BottomEnd),
                        textAlign = TextAlign.End
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(backAlpha)
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        if (ThemeManager.isDarkMode)
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1E1E), Color(0xFF2A1B22))
                            )
                        else
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFF5F9), Color(0xFFFFE0EC))
                            )
                    )
                    .padding(cardPadding)
            ) {
                val backTextPrimary = if (ThemeManager.isDarkMode) Color.White else Color(0xFF2D2D2D)
                val backTextSecondary = if (ThemeManager.isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF5C5C5C)
                val backAccent = if (ThemeManager.isDarkMode) Color(0xFFFFB6C1) else Color(0xFFC16565)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row: name + gender + shelter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pet.name ?: "Unknown",
                                fontSize = if (isTablet) 30.sp else 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = backAccent
                            )
                            Text(
                                text = shelterName,
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                color = backTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (pet.gender.equals("Male", ignoreCase = true)) "♂ Male" else "♀ Female",
                                color = backTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isTablet) 14.sp else 12.sp
                            )
                            Text(
                                text = pet.type?.replaceFirstChar { it.uppercase() } ?: "Unknown type",
                                color = backTextSecondary,
                                fontSize = if (isTablet) 13.sp else 11.sp
                            )
                        }
                    }

                    // Simple info rows instead of chips/boxes
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoRow(label = "Age", value = pet.age ?: "N/A", isTablet = isTablet, labelColor = backTextSecondary, valueColor = backTextPrimary)
                        InfoRow(label = "Breed", value = pet.breed ?: "Unknown", isTablet = isTablet, labelColor = backTextSecondary, valueColor = backTextPrimary)
                        InfoRow(label = "From", value = shelterName, isTablet = isTablet, labelColor = backTextSecondary, valueColor = backTextPrimary)
                    }

                    // Description
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "About ${pet.name ?: "this pet"}",
                            color = backTextPrimary,
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = pet.description ?: "No description available.",
                            color = backTextSecondary,
                            fontSize = if (isTablet) 15.sp else 13.sp,
                            lineHeight = if (isTablet) 22.sp else 18.sp
                        )
                    }

                    // Hint
                    Text(
                        text = "Release to flip back",
                        color = backTextSecondary,
                        fontSize = if (isTablet) 13.sp else 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

        }
            }
        }

@Composable
fun InfoChip(
    label: String,
    value: String,
    isTablet: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB6C1).copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = if (isTablet) 16.dp else 12.dp,
                vertical = if (isTablet) 12.dp else 8.dp
            )
        ) {
            Text(
                text = label,
                fontSize = if (isTablet) 14.sp else 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFFFF9999),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isTablet: Boolean,
    labelColor: Color = Color.White.copy(alpha = 0.7f),
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = if (isTablet) 14.sp else 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (isTablet) 15.sp else 13.sp,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun EnhancedInfoChip(
    icon: String,
    label: String,
    value: String,
    isTablet: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )

    Card(
        modifier = Modifier.scale(animatedScale),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFB6C1).copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = if (isTablet) 20.dp else 16.dp,
                vertical = if (isTablet) 16.dp else 12.dp
            )
        ) {
            Text(
                text = icon,
                fontSize = if (isTablet) 24.sp else 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = label,
                fontSize = if (isTablet) 12.sp else 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFFFF9999),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: PetFilter,
    onFilterSelected: (PetFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Pets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9999)
            )
        },
        text = {
            Column {
                PetFilter.entries.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB6C1))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (filter) {
                                PetFilter.ALL -> "🐾 All Pets"
                                PetFilter.DOGS -> "🐕 Dogs Only"
                                PetFilter.CATS -> "🐱 Cats Only"
                            },
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = null
    )
}

@Composable
fun GemPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (GemPackage) -> Unit
) {
    var selected by remember { mutableStateOf(GemPackage.MEDIUM) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💎",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Buy Gems",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9999)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "You need 5 gems to like pets! Choose a package:",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                GemPackage.entries.forEach { packageType ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (packageType == GemPackage.MEDIUM)
                                Color(0xFFFFB6C1).copy(alpha = 0.2f)
                            else
                                Color(0xFFF8F8F8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { selected = packageType }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "💎 ${packageType.gemAmount} Gems",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9999)
                                )
                                Text(
                                    text = "Perfect for ${packageType.gemAmount / 5} likes",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (selected == packageType) {
                                    Text("Selected", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text(
                                    text = packageType.price,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9999)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💳 Secure payment with your preferred method",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onPurchase(selected) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))) {
                Text("Buy ${selected.gemAmount} Gems • ${selected.price}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true, apiLevel = 35)
@Composable
fun PetSwipeScreenPreview() {
    val mockNavController = rememberNavController()
    PetSwipeScreen(navController = mockNavController)
}