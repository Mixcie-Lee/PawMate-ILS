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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.GemPackage
import com.example.pawmate_ils.LikedPetsManager
import com.example.pawmate_ils.ThemeManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.pawmate_ils.ui.screens.ProfileRequirementDialog
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import com.google.firebase.database.PropertyName

data class PetData(
    val petId: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val age: String? = null,
    val gender: String? = null, // Maps to "Sex" in the UI
    val description: String? = null,
    val healthStatus: String? = null, // Can be parsed for bullet points
    val type: String? = null,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val additionalImages: List<Int> = emptyList(),

    // Shelter Info for the New Design
    val shelterId: String? = null,
    val shelterName: String? = null,
    val shelterAddress: String? = null, // 🆕 Added for the Address Box

    val validationStatus: Boolean = false,

    // Heartbeat System (Verified & Working)
    @get:PropertyName("shelterIsOnline")
    @set:PropertyName("shelterIsOnline")
    var shelterIsOnline: Boolean = false,

    @get:PropertyName("shelterLastActive")
    @set:PropertyName("shelterLastActive")
    var shelterLastActive: Long? = null
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
    var showGCashFlow by remember { mutableStateOf(false) } // This triggers the NEW multi-step flow
    var selectedPkg by remember { mutableStateOf(GemPackage.MEDIUM) }
    //track which icon is selected(swipe...etc)
    var selectedItem by remember { mutableStateOf("Swipe") }

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
    var showTutorial by remember { mutableStateOf(!tutorialSeen) }
    LaunchedEffect(showTutorial) {
        if (showTutorial) TutorialRuntime.wasShownThisProcess = true
    }

    LaunchedEffect(Unit) {
        homeViewModel.listenToChannels()
    }
    LaunchedEffect(Unit) { GemManager.init(context) } // ensure saved gem count is loaded
    val gemCount by GemManager.gemCount.collectAsState()
    val currentTier by GemManager.currentTier.collectAsState()






    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

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
    val firestorePets by adoptionViewModel.shelterPets.collectAsState()
    val allPets by petRepository.allPets.collectAsState()

// 3. COMBINE THEM (Important so both show up in the swipe stack)
    val combinedPets by remember(allPets, firestorePets) {
        derivedStateOf {
            // Put firestorePets FIRST so they take priority in distinctBy
            (firestorePets + allPets).distinctBy {
                // Use petId if available, otherwise fallback to a unique combo of name and shelter
                it.petId ?: "${it.name}-${it.shelterId}"
            }
        }
    }


    /* val allPets: List<PetData> by remember {
    derivedStateOf<List<PetData>> {
        (petRepository.allPets.value + firestorePets)
            .distinctBy { pet: PetData -> pet.name + pet.shelterId }
    }
}
/
     */


    //TO ESSENTIAL TOOLS TO KEEP DDING PETS DYNAMIC, IF SHELTER ADD PET A BLANK CARD WILL APPEAR
    val filteredPets by remember(combinedPets, petFilter) {
        derivedStateOf {
            when (petFilter) {
                PetFilter.ALL -> combinedPets
                PetFilter.DOGS -> combinedPets.filter { it.type == "dog" }
                PetFilter.CATS -> combinedPets.filter { it.type == "cat" }
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

            val hasGem = GemManager.consumeGems(5)
            if (hasGem) {
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
                        Log.d("PetSwipe", "💎 Current User Tier Level: ${currentTier.level}")

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
                            createdAt = System.currentTimeMillis() ,
                            //VIP    PRIORITY in CHAT
                            adopterTier = currentTier.level,
                            isPriority = currentTier.level == 3
                        )

                        Log.d("PetSwipe", "✅ Creating channel: $channel")
                        homeViewModel.addChannel(channel)

                    } catch (e: Exception) {
                        Log.e("PetSwipe", "❌ Error creating channel", e)
                    }
                }
            }else {
                // ❌ STOP: The user doesn't have enough "Fuel"
                android.widget.Toast.makeText(
                    context,
                    "You're out of gems! Buy a pack to keep swiping or unlock Tiers.",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                // 🛍️ Open the shop so they can refill immediately
                GemManager.openPurchaseDialog()

                return
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
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 15f).coerceIn(-30f, 30f)
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
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 15f).coerceIn(-30f, 30f)
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
                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
            ) { value, _ ->
                offsetX = value
                rotation = value / 40f
            }
        }
    }



    Scaffold(
        bottomBar = {
            AdopterBottomBar(
                navController = navController,
                selectedTab = "Swipe"
            )
        }

    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = backgroundColor
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
                                painter = painterResource(id = R.drawable.blackpawmateicon3),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.diamond),
                                    contentDescription = "Gems",
                                    modifier = Modifier.size(if (isTablet) 26.dp else 22.dp)
                                )
                                Text(
                                    text = gemCount.toString(),
                                    color = textColor,
                                    fontSize = if (isTablet) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = { showGemDialog = true }
                            ) {
                                Text(
                                    text = "Buy",
                                    color = Color(0xFFFF9999),
                                    fontSize = if (isTablet) 14.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { showTutorial = true },
                                modifier = Modifier.size(if (isTablet) 48.dp else 44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Show Tutorial",
                                    tint = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFF9999),
                                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
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
                                userTier = currentTier.level,
                                navController = navController,
                                authViewModel = authViewModel,
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
                                    val newOffset = (offsetX + deltaX).coerceIn(-600f, 600f)
                                    // Interpolate rotation a bit slower for smoothness
                                    val newRotation = (newOffset / 50f)
                                    // Lightweight interpolation to avoid jank during rapid drags
                                    offsetX = newOffset
                                    rotation = newRotation
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
                            userTier = currentTier.level,
                            navController = navController,
                            authViewModel = authViewModel,
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
                            onDismissRequest = {
                                tutorialPrefs.edit().putBoolean("seen", true).apply()
                                showTutorial = false
                            },
                            containerColor = if (ThemeManager.isDarkMode) Color(0xFF2A2A2A) else Color.White,
                            title = {
                                Text(
                                    text = when (tutorialStep) {
                                        0 -> "Welcome to PawMate"
                                        1 -> "How to Swipe"
                                        2 -> "Discover Pets"
                                        else -> "Welcome to Swipe"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (ThemeManager.isDarkMode) Color(0xFFFF9999) else Color(0xFFFF9999)
                                )
                            },
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = when (tutorialStep) {
                                                0 -> R.drawable.tutorial1
                                                1 -> R.drawable.tutorial2
                                                2 -> R.drawable.tutorial3
                                                else -> R.drawable.tutorial1
                                            }
                                        ),
                                        contentDescription = "Tutorial ${tutorialStep + 1}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (isTablet) 700.dp else 600.dp),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (tutorialStep < 2) {
                                        TextButton(
                                            onClick = { tutorialStep++ },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (ThemeManager.isDarkMode) Color(0xFFFF9999) else Color(0xFFFF9999)
                                            )
                                        ) {
                                            Text("Next")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                tutorialPrefs.edit().putBoolean("seen", true).apply()
                                                showTutorial = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFB6C1),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Start swiping")
                                        }
                                    }
                                }
                            },
                            dismissButton = {
                                if (tutorialStep > 0) {
                                    TextButton(
                                        onClick = { tutorialStep-- },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (ThemeManager.isDarkMode) Color(0xFFFF9999) else Color(0xFFFF9999)
                                        )
                                    ) {
                                        Text("Back")
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            tutorialPrefs.edit().putBoolean("seen", true).apply()
                                            showTutorial = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (ThemeManager.isDarkMode) Color(0xFFFF9999) else Color(0xFFFF9999)
                                        )
                                    ) {
                                        Text("Close")
                                    }
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
                            onClick = {
                                // 💎 FIX: Get the UID from your AuthViewModel
                                val currentUid = authViewModel.currentUser?.uid ?: ""

                                if (currentUid.isNotEmpty()) {
                                    GemManager.confirmPurchase(currentUid) {
                                        // This calls the function you showed me in HomeViewModel
                                        homeViewModel.syncExistingChannelsToTier3()
                                    }
                                } else {
                                    Log.e("PetSwipe", "Cannot confirm purchase: User ID is null")
                                }
                            },
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
                        // 1. Store the package they picked
                        selectedPkg = packageType
                        // 2. Close the "Shop" list
                        showGemDialog = false
                        // 3. Open the "GCash" multi-step flow
                        showGCashFlow = true
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
            if (showGCashFlow) {
                GCashMultiStepDialog(
                    packageType = selectedPkg,
                    onDismiss = { showGCashFlow = false },
                    homeViewModel = homeViewModel,
                    authViewModel = authViewModel
                )
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
    userTier: Int,
    navController: NavController, // Ensure this is passed in
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    var currentImageIndex by remember(pet.name) { mutableIntStateOf(0) }
    var isHolding by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var shelterName by remember { mutableStateOf("Loading") }
    val scope = rememberCoroutineScope()
    val shelterRepository = remember { ShelterRepository() }

    val isShelterLive = authViewModel.isUserActuallyOnline(
        User(
            isOnline = pet.shelterIsOnline,
            lastActive = pet.shelterLastActive
        )
    )


    LaunchedEffect(pet.shelterId) {
        if (pet.shelterId.isNullOrEmpty()) {
            shelterName = "No shelter found"
            return@LaunchedEffect
        }
        try {
            val name = shelterRepository.getShelterNameById(pet.shelterId!!)
            shelterName = name ?: "Unknown Shelter"
        } catch (e: Exception) {
            shelterName = "No shelter found"
        }
    }

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
            isHolding -> 0.95f
            abs(offsetX) > 100f -> 1.05f
            abs(offsetX) < 10f -> idlePulse
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = when {
            isHolding -> 24f
            abs(offsetX) > 50f -> 16f
            else -> 8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_elevation"
    )

    val nameSize = if (isTablet) 32.sp else 28.sp
    val infoSize = if (isTablet) 20.sp else 16.sp
    val descSize = if (isTablet) 18.sp else 14.sp
    val cardPadding = if (isTablet) 24.dp else 16.dp
    val indicatorSize = if (isTablet) 12.dp else 8.dp
    val indicatorSpacing = if (isTablet) 12.dp else 8.dp
    val cardWidth = if (isTablet) 400.dp else 320.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .heightIn(min = 450.dp, max = 600.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
                shadowElevation = elevation
            }
            .pointerInput(pet.name) {
                detectHorizontalDragGestures(onDragEnd = { onDragEnd() }) { _, dragAmount -> onDrag(dragAmount) }
            }
            .pointerInput(pet.name + "_tap", userTier) {
                detectTapGestures(
                    onTap = { offset ->
                        val cardWidthVal = size.width
                        val totalImages = pet.additionalImages.size + 1
                        if (totalImages > 1) {
                            if (offset.x < cardWidthVal / 2) {
                                currentImageIndex = if (currentImageIndex > 0) currentImageIndex - 1 else totalImages - 1
                            } else {
                                currentImageIndex = (currentImageIndex + 1) % totalImages
                            }
                        }
                    },
                    onLongPress = {
                        if (userTier >= 2) { isHolding = true }
                        else {
                            isHolding = false
                            android.widget.Toast.makeText(context, "Detailed Info requires Tier 2 Unlock!", android.widget.Toast.LENGTH_SHORT).show()
                            GemManager.openPurchaseDialog()
                        }
                    },
                    onPress = { tryAwaitRelease(); isHolding = false }
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

            Box(modifier = Modifier.fillMaxSize().background(backgroundOverlay))

            val currentImage = when {
                currentImageIndex == 0 -> {
                    // 🛡️ FIX: If there is a URL, use it. Otherwise, use the resource ID.
                    if (!pet.imageUrl.isNullOrEmpty()) pet.imageUrl else pet.imageRes.takeIf { it != 0 } ?: R.drawable.placeholder
                }
                currentImageIndex <= pet.additionalImages.lastIndex + 1 && pet.additionalImages.isNotEmpty() -> {
                    pet.additionalImages.getOrNull(currentImageIndex - 1) ?: R.drawable.placeholder
                }
                else -> R.drawable.placeholder
            }

            AsyncImage(
                // 🚀 THE FIX: Use 'currentImage' here instead of the hardcoded 'pet.imageUrl'
                model = currentImage,
                contentDescription = pet.name,
                placeholder = painterResource(id = R.drawable.blackpawmateicon3),
                error = painterResource(id = R.drawable.placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🏠 SHELTER BADGE (TOP LEFT - AS REQUESTED)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        if (!pet.shelterId.isNullOrEmpty()) {
                            navController.navigate("profile_details/${pet.shelterId}")
                        }
                    }
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    // Circle Icon
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    // Green Dot
                    if (isShelterLive) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80)) // PawMate Active Green
                                .border(1.5.dp, Color.White, CircleShape)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                        )
                    }
                }
                Text(
                    text = shelterName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (pet.additionalImages.isNotEmpty()) {
                Card(
                    modifier = Modifier.align(Alignment.TopCenter).padding(cardPadding),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(indicatorSpacing)) {
                        repeat(pet.additionalImages.size + 1) { index ->
                            val isSelected = index == currentImageIndex
                            val indicatorScale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "indicator_scale_$index")
                            Box(modifier = Modifier.size(indicatorSize).scale(indicatorScale).background(color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), shape = CircleShape))
                        }
                    }
                }
            }

            // Swipe Indicators (Keep as is)
            androidx.compose.animation.AnimatedVisibility(visible = offsetX > 50f, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                Box(modifier = Modifier.align(Alignment.Center).scale(1f + (offsetX / 400f).coerceIn(0f, 0.3f)).background(Color.Green.copy(alpha = (offsetX / 200f).coerceIn(0.3f, 1f)), RoundedCornerShape(20.dp)).padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💚", fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(text = "LIKE", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = offsetX < -50f, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                Box(modifier = Modifier.align(Alignment.Center).scale(1f + (abs(offsetX) / 400f).coerceIn(0f, 0.3f)).background(Color.Red.copy(alpha = (abs(offsetX) / 200f).coerceIn(0.3f, 1f)), RoundedCornerShape(20.dp)).padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💔", fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(text = "PASS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Info overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = if (isHolding) listOf(Color.Transparent, Color.Transparent)
                            else listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.8f))
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(cardPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = pet.name ?: "Unknown", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = nameSize))
                    Text(text = pet.age ?: "Unknown", color = Color.White.copy(alpha = 0.9f), fontSize = infoSize, fontWeight = FontWeight.Medium)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)), shape = RoundedCornerShape(12.dp)) {
                        Text(text = pet.breed ?: "Unknown", color = Color.White, fontSize = descSize, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Text(text = pet.description ?: "no description", color = Color.White.copy(alpha = 0.85f), fontSize = descSize, lineHeight = (descSize.value * 1.4f).sp)
                }
            }

            // 🟢 HOLD MODAL (Moved to end of Box stack so it stays on top)
            androidx.compose.animation.AnimatedVisibility(
                visible = isHolding,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)), // Dims the background
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (isTablet) 0.85f else 0.94f)
                            .fillMaxHeight(0.88f)
                            .padding(if (isTablet) 24.dp else 12.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            // 🛑 SOLID background color to prevent image bleed
                            containerColor = if (ThemeManager.isDarkMode) Color(0xFF2A2A2A) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()) // 🟢 Scrollable content
                                .padding(if (isTablet) 32.dp else 16.dp)
                        ) {
                            // 🏷️ "Info" Header Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFB6C1).copy(alpha = 0.2f))
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Info",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD67A7A)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 📋 DYNAMIC SECTIONS
                            DetailBox(label = "Name", value = pet.name ?: "Unknown")
                            DetailBox(label = "Age", value = pet.age ?: "N/A")
                            DetailBox(label = "Sex", value = pet.gender ?: "Unknown")
                            DetailBox(label = "Shelter", value = pet.shelterName ?: "Private Shelter")
                            DetailBox(label = "Address", value = pet.shelterAddress ?: "Address Hidden")

                            // 🏥 Health Status Section
                            HealthStatusBox(status = pet.healthStatus)

                            Spacer(modifier = Modifier.height(20.dp))

                            // 📝 Final Description Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (ThemeManager.isDarkMode) Color(0xFF3A3A3A) else Color(0xFFFFB6C1).copy(alpha = 0.1f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                Text(text = "About ${pet.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9999))
                                Text(text = pet.description ?: "No description available", fontSize = 14.sp, color = if (ThemeManager.isDarkMode) Color.White else Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun InfoCard(
    icon: String,
    label: String,
    value: String,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkMode = ThemeManager.isDarkMode

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFFFB6C1).copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isTablet) 20.dp else 16.dp,
                    vertical = if (isTablet) 20.dp else 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = if (isTablet) 32.sp else 28.sp,
                modifier = Modifier.padding(end = if (isTablet) 16.dp else 12.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label,
                    fontSize = if (isTablet) 13.sp else 11.sp,
                    color = if (isDarkMode) Color.LightGray.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = if (isTablet) 20.sp else 18.sp,
                    color = Color(0xFFFF9999),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EnhancedInfoChip(
    icon: String,
    label: String,
    value: String,
    isTablet: Boolean
) {
    val isDarkMode = ThemeManager.isDarkMode
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )

    Card(
        modifier = Modifier
            .scale(animatedScale)
            .widthIn(min = 110.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFFFB6C1).copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = if (isTablet) 24.dp else 20.dp,
                vertical = if (isTablet) 20.dp else 16.dp
            )
        ) {
            Text(
                text = icon,
                fontSize = if (isTablet) 32.sp else 28.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = label,
                fontSize = if (isTablet) 14.sp else 12.sp,
                color = if (isDarkMode) Color.LightGray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = if (isTablet) 18.sp else 16.sp,
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
}@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (GemPackage) -> Unit
) {
    val currentTier by GemManager.currentTier.collectAsState()
    var selected by remember { mutableStateOf(GemPackage.MEDIUM) }
    val context = LocalContext.current

    // States for the Eligibility Dialog
    var showEligibilityDialog by remember { mutableStateOf(false) }
    var eligibilityDialogData by remember { mutableStateOf("" to "") }
    var selectedPackageForAction by remember { mutableStateOf<GemPackage?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.diamond),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Upgrade PawMate",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9999)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Choose a package to unlock perks:",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                GemPackage.entries.forEach { packageType ->
                    val isAlreadyOwned = when (packageType) {
                        GemPackage.SMALL -> false
                        GemPackage.MEDIUM -> currentTier.level >= 2
                        GemPackage.LARGE -> currentTier.level >= 3
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected == packageType)
                                Color(0xFFFFB6C1).copy(alpha = 0.15f)
                            else Color(0xFFF8F8F8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { selected = packageType }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${packageType.gemAmount} Gems",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9999)
                                    )
                                    if (isAlreadyOwned) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(start = 4.dp).size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isAlreadyOwned) "Incl. Permanent Perks | ${packageType.price}" else packageType.price,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    lineHeight = 12.sp
                                )
                            }

                            Text(
                                text = packageType.price.split("|").first().trim(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF9999)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isOwned = when (selected) {
                GemPackage.SMALL -> false
                GemPackage.MEDIUM -> currentTier.level >= 2
                GemPackage.LARGE -> currentTier.level >= 3
            }

            Button(
                onClick = {
                    GemManager.checkTierEligibility(
                        selectedPackage = selected,
                        onShowDialog = { title, message ->
                            // 🟢 FIX: Correctly assigning data to show the dialog
                            eligibilityDialogData = title to message
                            selectedPackageForAction = selected
                            showEligibilityDialog = true
                        },
                        onDirectPurchase = {
                            onPurchase(selected)
                        }
                    )
                },
                enabled = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))
            ) {
                Text(if (isOwned) "Buy More Gems" else "Purchase", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = 12.sp, color = Color.Gray)
            }
        }
    )

    // 🟢 NEW ADDITION: The actual Eligibility Dialog UI
    if (showEligibilityDialog) {
        AlertDialog(
            onDismissRequest = { showEligibilityDialog = false },
            title = { Text(eligibilityDialogData.first, fontWeight = FontWeight.Bold) },
            text = { Text(eligibilityDialogData.second) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPackageForAction?.let { pkg -> onPurchase(pkg) }
                        showEligibilityDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEligibilityDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}@Composable
fun GCashMultiStepDialog(
    packageType: GemPackage,
    onDismiss: () -> Unit,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
    // 🚦 0: Final Assurance | 1: QR Code | 2: Processing | 3: Success
    var currentStep by remember { mutableIntStateOf(0) }

    // 🎨 PAWMATE COLOR PALETTE SYNC
    val isDarkMode = ThemeManager.isDarkMode
    val pawMatePink = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val pawMateSurface = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val pawMateText = if (isDarkMode) Color.White else Color.Black

    AlertDialog(
        onDismissRequest = { if (currentStep != 2) onDismiss() },
        containerColor = pawMateSurface,
        titleContentColor = pawMatePink,
        textContentColor = pawMateText,
        title = {
            // 🎯 Centering the Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when(currentStep) {
                        0 -> "Confirm Purchase"
                        1 -> "GCash Scan to Pay"
                        2 -> "Verifying..."
                        else -> "Payment Successful!"
                    },
                    fontWeight = FontWeight.Bold,
                    color = pawMatePink,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when (currentStep) {
                    0 -> { // 🛡️ STEP 0: FINAL ASSURANCE
                        Text("You are buying ${packageType.gemAmount} Gems for ${packageType.price.split("|")[0]}", textAlign = TextAlign.Center, color = pawMateText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Are you sure you want to proceed?", fontWeight = FontWeight.SemiBold, color = pawMateText)
                    }
                    1 -> { // 📱 STEP 1: QR CODE
                        Text("Scan this QR with your GCash App", fontSize = 14.sp, color = pawMateText)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mock QR Icon themed to your app's accent
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(160.dp),
                            tint = pawMatePink.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reference: PM-${System.currentTimeMillis().toString().takeLast(6)}", fontSize = 10.sp, color = Color.Gray)
                    }
                    2 -> { // ⏳ STEP 2: PROCESSING
                        LaunchedEffect(Unit) {
                            delay(5000)
                            GemManager.initiatePurchase(packageType)
                            val uid = authViewModel.currentUser?.uid ?: ""
                            GemManager.confirmPurchase(uid) {
                                if (packageType == GemPackage.LARGE) {
                                    homeViewModel.syncExistingChannelsToTier3()
                                }
                            }
                            currentStep = 3
                        }
                        CircularProgressIndicator(color = pawMatePink)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Waiting for GCash response...", textAlign = TextAlign.Center, color = pawMateText)
                    }
                    3 -> { // 🎉 STEP 3: SUCCESS
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF4CAF50) // Keep success green
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Transaction Complete!", fontWeight = FontWeight.Bold, color = pawMateText)
                        Text("${packageType.gemAmount} Gems have been added to your account.", fontSize = 13.sp, textAlign = TextAlign.Center, color = pawMateText)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentStep < 2) currentStep++
                    else if (currentStep == 3) onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = pawMatePink),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when(currentStep) {
                        0 -> "Yes, Proceed"
                        1 -> "I've Scanned & Paid"
                        else -> "Done"
                    },
                    color = Color.White
                )
            }
        },
        dismissButton = {
            if (currentStep < 2) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = if (isDarkMode) Color.LightGray else Color.Gray)
                }
            }
        }
    )
}
@Composable
fun DetailBox(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFD67A7A),
            fontSize = 16.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = if(ThemeManager.isDarkMode) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HealthStatusBox(status: String?) {
    val medicalList = status?.split(",", "\n")?.filter { it.isNotBlank() } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Health Status",
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFD67A7A),
            fontSize = 16.sp
        )

        if (medicalList.isEmpty()) {
            Text(text = "Healthy", fontSize = 14.sp, color = Color.Gray)
        } else {
            medicalList.forEach { point ->
                Text(
                    text = "• ${point.trim()}",
                    fontSize = 13.sp,
                    color = if(ThemeManager.isDarkMode) Color.White else Color.Black
                )
            }
        }
    }
}




@Preview(showBackground = true, apiLevel = 35)
@Composable
fun PetSwipeScreenPreview() {
    val mockNavController = rememberNavController()
    PetSwipeScreen(navController = mockNavController)
}