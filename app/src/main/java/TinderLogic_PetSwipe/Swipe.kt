package TinderLogic_PetSwipe

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlin.math.sign
import kotlin.random.Random
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.CubicBezierEasing
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import com.google.firebase.database.PropertyName

private fun gemTierTitle(pkg: GemPackage) = when (pkg) {
    GemPackage.SMALL -> "Starter"
    GemPackage.MEDIUM -> "Plus"
    GemPackage.LARGE -> "Ultimate"
}

private fun gemTierSubtitle(pkg: GemPackage) = when (pkg) {
    GemPackage.SMALL -> "Perfect to try gems"
    GemPackage.MEDIUM -> "Recommended by PawMate"
    GemPackage.LARGE -> "Full PawMate perks"
}

private fun gemPriceOnly(pkg: GemPackage): String =
    pkg.price.split("|").first().trim()

private fun gemPerkDescription(pkg: GemPackage): String =
    pkg.price.split("|").drop(1).joinToString("|").trim()
        .ifBlank { "Unlock PawMate tier perks and tools." }
        .trimEnd()

/** Readable UI line for Ultimate — backend `GemPackage.LARGE.price` string unchanged. */
private fun gemPerkDescriptionUltimateUi(full: String): String {
    val raw = full.trim().trimEnd('.', ' ')
    if (raw.contains("priority inbox", ignoreCase = true) &&
        raw.contains("tier 2", ignoreCase = true)
    ) {
        return "Priority inbox for chats, plus every perk from the Plus tier."
    }
    return raw
}

private fun gemTierOwned(pkg: GemPackage, tierLevel: Int) = when (pkg) {
    GemPackage.SMALL -> false
    GemPackage.MEDIUM -> tierLevel >= 2
    GemPackage.LARGE -> tierLevel >= 3
}

/** Smooth Y spin from horizontal drag (smoothstep). */
private fun swipeRotationYFromOffset(offsetX: Float): Float {
    val t = (abs(offsetX) / 520f).coerceIn(0f, 1f)
    val eased = t * t * (3f - 2f * t)
    return sign(offsetX) * eased * 56f
}

data class PetData(
    val petId: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val age: String? = null,
    @get:PropertyName("gender") @set:PropertyName("gender")
    var gender: String? = null,// Maps to "Sex" in the UI
    val description: String? = null,
    val healthStatus: String? = null, // Can be parsed for bullet points
    val type: String? = null,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
    // Shelter Info for the New Design
    @get:PropertyName("shelterId") @set:PropertyName("shelterId")
    var shelterId: String? = null,

    @get:PropertyName("shelterName") @set:PropertyName("shelterName")
    var shelterName: String? = null,
    @get:PropertyName("shelterAddress") @set:PropertyName("shelterAddress")
    var shelterAddress: String? = null, // 🆕 Added for the Address Box

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
    /** Bumps on user "Shuffle" so [filteredPets] reorders with a new seed (stable when unchanged). */
    var shuffleEpoch by remember { mutableIntStateOf(0) }
    var shuffleIconSpinTarget by remember { mutableFloatStateOf(0f) }
    val shuffleIconRotation by animateFloatAsState(
        targetValue = shuffleIconSpinTarget,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "shuffle_icon_spin"
    )
    var showFilterDialog by remember { mutableStateOf(false) }

    var offsetX by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var offsetY by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var rotation by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var rotationYSwipe by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var frontCardZIndex by remember { mutableFloatStateOf(50f) }
    var isDragging by remember { mutableStateOf(false) }
    var entranceProgress by remember { mutableFloatStateOf(1f) }
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

    var swipedPetIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val currentUserId = authViewModel.currentUser?.uid ?: ""

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
    LaunchedEffect(currentPetIndex) {
        if (entranceProgress < 1f) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 400f
                )
            ) { value, _ ->
                entranceProgress = value
            }
        }
    }
    val gemCount by GemManager.gemCount.collectAsState()
    val currentTier by GemManager.currentTier.collectAsState()

    var backPressedTime by remember { mutableLongStateOf(0L) }


    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            swipedPetIds = firestoreRepo.getSwipedPetIds(currentUserId)
        }
    }

    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // 🎯 1. Mark them offline so they don't show a green dot while gone
            authViewModel.updateOnlineStatus(false)

            // 🎯 2. Minimize the app instead of "Finishing" it.
            // This keeps the session ALIVE in the background.
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_HOME)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            backPressedTime = currentTime
            android.widget.Toast.makeText(context, "Swipe again to exit PawMate", android.widget.Toast.LENGTH_SHORT).show()
        }
    }




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
            val dynamicOnly = (firestorePets + allPets).filter { pet ->
                // Logic: Mock pets have IDs like "shelter1", "shelter2"...
                // Real Firestore pets have long random strings.
                // We hide any pet that belongs to the mock "shelter" IDs.
                !pet.shelterId?.startsWith("shelter")!! && pet.name != "Blank Card"
            }

            dynamicOnly.distinctBy {
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
    val filteredPets by remember(combinedPets, petFilter, shuffleEpoch) {
        derivedStateOf {
            val base = when (petFilter) {
                PetFilter.ALL -> combinedPets
                PetFilter.DOGS -> combinedPets.filter { it.type == "dog" }
                PetFilter.CATS -> combinedPets.filter { it.type == "cat" }
            }

            val unswiped = base.filter { pet ->
                val id = pet.petId ?: "${pet.name}-${pet.shelterId}"
                !swipedPetIds.contains(id)
            }

            val seed = unswiped // ✅ Use the filtered list
                .map { it.petId ?: "${it.name}-${it.shelterId}" }
                .sorted()
                .joinToString()
                .hashCode()
                .toLong() xor (petFilter.ordinal * 31L) xor (shuffleEpoch.toLong() * 0x9E3779B97L)
            unswiped.shuffled(Random(seed)) // ✅ Use the filtered list
        }
    }
    var isCompositionReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(350L)
        isCompositionReady = true
    }

    var hasPlayedInitialEntrance by remember { mutableStateOf(false) }
    LaunchedEffect(filteredPets.size) {
        if (currentPetIndex >= filteredPets.size && filteredPets.isNotEmpty()) {
            currentPetIndex = filteredPets.size - 1
        }
        if (!hasPlayedInitialEntrance && filteredPets.isNotEmpty()) {
            hasPlayedInitialEntrance = true
        }
    }
    LaunchedEffect(hasPlayedInitialEntrance, isCompositionReady) {
        if (hasPlayedInitialEntrance && isCompositionReady) {
            entranceProgress = 0f
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 400f
                )
            ) { value, _ ->
                entranceProgress = value
            }
        }
    }


    //This is the handler for a dialog that ask the user to upload or create their profile picture
    //without this the avatar icon wouldnt appear if they change it mid use
    ProfileRequirementDialog(
        authViewModel = authViewModel,
        navController = navController,
        canShow = !showTutorial
    )

    fun resetCardPosition() {
        if (isDragging || currentPetIndex >= filteredPets.size) return

        scope.launch {
            val startX = offsetX
            val startY = offsetY
            val startRy = rotationYSwipe
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
            ) { t, _ ->
                val k = 1f - t
                offsetX = startX * k
                offsetY = startY * k
                rotation = offsetX / 50f
                rotationYSwipe = startRy * k
            }
        }
    }




    @SuppressLint("SuspiciousIndentation")
    fun swipeCard(direction: Float) {
        if (isDragging || currentPetIndex >= filteredPets.size) return

        val currentPet = filteredPets[currentPetIndex]
        val petIdToRecord = currentPet.petId ?: "${currentPet.name}-${currentPet.shelterId}"


        if (direction > 0) {
            val hasGem = GemManager.consumeGems(5)
            if (hasGem) {
                likedPetsViewmodel.addLikedPet(currentPet)

               /* scope.launch {
                    firestoreRepo.markAsSwiped(currentUserId, petIdToRecord)
                    swipedPetIds = swipedPetIds + petIdToRecord
                }
                */


                // Reset the card position instead of letting it fly away

                if (currentPetIndex == filteredPets.lastIndex) {
                    petRepository.appendBlankCard(
                        PetData(name = "Blank Card", type = currentPet.type ?: "dog")
                    )
                }



                scope.launch {
                    try {
                        val currentUser = authViewModel.userData.value
                        val shelterId = currentPet.shelterId
                        if (shelterId.isNullOrEmpty()) return@launch

                        val allUsers = firestoreRepo.getAllUsers()
                        val shelterUser = allUsers.find { it.id == shelterId } ?: return@launch

                        val adopterId = authViewModel.currentUser?.uid ?: return@launch
                        val adopterUser = allUsers.find { it.id == adopterId }
                        val adopterName = authViewModel.currentUser?.displayName ?: "Unknown"
                        val shelterName = shelterUser.name ?: "Unknown Shelter"

                        val adopterPhoto = currentUser?.photoUri?.takeIf { it.isNotBlank() } ?: ""
                        val shelterPhoto = shelterUser.photoUri?.takeIf { it.isNotBlank() } ?: ""

                        // 🏷️ NEWLY ADDED: Variable Sync for consolidation
                        val petNameToAdd = currentPet.name ?: "Unknown"
                        val uniqueChannelId = "$adopterId-$shelterId"

                        // 🏷️ NEWLY ADDED: Check local state for an existing channel pair
                        val existingChannel = homeViewModel.channels.value.firstOrNull {
                            it.channelId == uniqueChannelId
                        }

                        if (existingChannel != null) {
                            // ==========================================================
                            // 🏷️ NEWLY ADDED: CONSOLIDATION LOGIC
                            // ==========================================================
                            Log.d("PetSwipe", "⚠️ Channel exists. Merging $petNameToAdd")

                            val currentPets = existingChannel.petNames
                            if (!currentPets.contains(petNameToAdd)) {
                                val updatedList = currentPets + petNameToAdd
                                // Update Firestore immediately with the new list
                                firestoreRepo.updateChannelPetNames(uniqueChannelId, updatedList)
                                Log.d("PetSwipe", "✅ Consolidated: $petNameToAdd added to $uniqueChannelId")
                            }
                        } else {
                            // ==========================================================
                            // 🏷️ NEWLY ADDED: NEW CHANNEL LOGIC (First match only)
                            // ==========================================================
                            val channel = Channel(
                                channelId = uniqueChannelId,
                                adopterId = adopterId,
                                adopterName = adopterName,
                                adopterPhotoUri = adopterPhoto,
                                shelterId = shelterId,
                                shelterName = shelterName,
                                shelterPhotoUri = shelterPhoto,
                                petNames = listOf(petNameToAdd),
                                lastMessage = "Interested in $petNameToAdd",
                                timestamp = System.currentTimeMillis(),
                                unreadCount = 0,
                                createdAt = System.currentTimeMillis(),
                                adopterTier = currentTier.level,
                                isPriority = currentTier.level == 3
                            )
                            Log.d("PetSwipe", "✅ Created new consolidated channel: $uniqueChannelId")
                            homeViewModel.addChannel(channel)
                        }
                    } catch (e: Exception) {
                        Log.e("PetSwipe", "❌ Consolidation Error: ${e.message}")
                    }
                }
            } else {
                android.widget.Toast.makeText(
                    context,
                    "You're out of gems!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                GemManager.openPurchaseDialog()
                return
            }
        }

        val nextIndex = currentPetIndex + 1
        if (nextIndex < filteredPets.size) {
            isDragging = true
            scope.launch {
                val targetX = if (direction > 0) 1200f else -1200f
                val exitEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 360, easing = exitEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 50f).coerceIn(-14f, 14f)
                    rotationYSwipe = swipeRotationYFromOffset(value)
                }
                entranceProgress = 0f
                currentPetIndex = nextIndex
                offsetX = 0f
                offsetY = 0f
                rotation = 0f
                rotationYSwipe = 0f
                isDragging = false
            }
        } else {
            scope.launch {
                val targetX = if (direction > 0) 1200f else -1200f
                val exitEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 360, easing = exitEasing)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 50f).coerceIn(-14f, 14f)
                    rotationYSwipe = swipeRotationYFromOffset(value)
                }
                currentPetIndex = filteredPets.size
            }
        }
    }





    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            AdopterBottomBar(
                navController = navController,
                selectedTab = "Home"
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
                                    petFilter = if (petFilter == PetFilter.DOGS) PetFilter.ALL else PetFilter.DOGS
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
                                    petFilter = if (petFilter == PetFilter.CATS) PetFilter.ALL else PetFilter.CATS
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
                        IconButton(
                            onClick = {
                                if (filteredPets.size <= 1) return@IconButton
                                shuffleIconSpinTarget += 360f
                                shuffleEpoch++
                                currentPetIndex = 0
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                                rotationYSwipe = 0f
                                isDragging = false
                            },
                            enabled = filteredPets.size > 1
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle deck",
                                tint = if (filteredPets.size > 1) primaryColor else textColor.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(if (isTablet) 28.dp else 26.dp)
                                    .graphicsLayer {
                                        rotationZ = shuffleIconRotation
                                    }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                    if (isCompositionReady) {
                        AnimatedContent(
                            targetState = shuffleEpoch,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                                        scaleIn(
                                            initialScale = 0.92f,
                                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                                        )
                                        ).togetherWith(
                                        fadeOut(animationSpec = tween(180)) +
                                                scaleOut(
                                                    targetScale = 0.94f,
                                                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                                                )
                                    )
                            },
                            modifier = Modifier
                                .width(cardWidth)
                                .height(cardHeight),
                            label = "shuffle_card_refresh"
                        ) {epoch ->
                            val _ignore = epoch
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = if (isTablet) 0.dp else 0.dp),
                                contentAlignment = Alignment.Center
                            ) { // ✅ Fix 2: Restored opening brace for Box
                                val densityFloat = density.density
                                val stackPull by animateFloatAsState(
                                    targetValue = (abs(offsetX) / 260f).coerceIn(0f, 1f),
                                    animationSpec = spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 350f
                                    ),
                                    label = "stack_pull"
                                )

                                val stackDepthTotal = 3
                                for (depth in stackDepthTotal downTo 1) {
                                    val idx = currentPetIndex + depth
                                    if (idx < filteredPets.size) {
                                        val d = depth.toFloat()
                                        val messDir = if (depth % 2 == 0) 1f else -1f
                                        SwipeablePetCard(
                                            pet = filteredPets[idx],
                                            offsetX = 0f,
                                            offsetY = 0f,
                                            rotation = 0f,
                                            rotationY = 0f,
                                            onDrag = { _, _ -> },
                                            onDragEnd = { },
                                            isTablet = isTablet,
                                            userTier = currentTier.level,
                                            navController = navController,
                                            authViewModel = authViewModel,
                                            isBackgroundStack = true,
                                            showSwipeOverlays = false,
                                            onFrontLayerRaised = { },
                                            modifier = Modifier
                                                .zIndex((stackDepthTotal - depth + 1).toFloat())
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    cameraDistance = 14f * densityFloat
                                                    transformOrigin = TransformOrigin(0.5f, 0.52f)
                                                    val s = (1f - d * 0.04f + stackPull * 0.035f).coerceIn(0.82f, 1f)
                                                    scaleX = s
                                                    scaleY = s
                                                    translationX = messDir * d * (16f + stackPull * 18f)
                                                    translationY = d * (6f - stackPull * 4f)
                                                    rotationZ = messDir * d * (2.5f + stackPull * 3.5f)
                                                    rotationX = 0f
                                                }
                                        )
                                    }
                                }

                                SwipeablePetCard(
                                    pet = filteredPets[currentPetIndex],
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    rotation = rotation,
                                    rotationY = rotationYSwipe,
                                    onDrag = { deltaX, deltaY ->
                                        if (!isDragging) {
                                            val newOffset = (offsetX + deltaX).coerceIn(-600f, 600f)
                                            val newRotation = (newOffset / 50f)
                                            offsetX = newOffset
                                            rotation = newRotation
                                            rotationYSwipe = swipeRotationYFromOffset(newOffset)
                                            offsetY = (offsetY + deltaY).coerceIn(-150f, 150f)
                                        }
                                    },
                                    onDragEnd = {
                                        if (!isDragging) {
                                            if (abs(offsetX) > 100f) {
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
                                    isBackgroundStack = false,
                                    onFrontLayerRaised = { raised ->
                                        frontCardZIndex = if (raised) 220f else 50f
                                    },
                                    modifier = Modifier
                                        .zIndex(frontCardZIndex)
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            cameraDistance = 12f * densityFloat
                                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                                            rotationX = 0f
                                            val ep = entranceProgress.coerceIn(0f, 1f)
                                            val eScale = 0.94f + ep * 0.06f
                                            scaleX = eScale
                                            scaleY = eScale
                                            translationY = (1f - ep) * 10f * densityFloat
                                        }
                                )
                            } // Close Box
                        } // Close AnimatedContent
                    } else {
                        Box(
                            modifier = Modifier
                                .width(cardWidth)
                                .height(cardHeight)
                        )
                    }
                    } // Close centering Box

                    // Tutorial (Dialog + screen arrows)
                    if (showTutorial) {
                        var tutorialStep by rememberSaveable { mutableStateOf(0) }

                        // Arrow overlays pointing to UI
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (tutorialStep) {
                                0 -> { // Info button (top-right)
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
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

@Composable
private fun PetInfoBackFace(
    pet: PetData,
    shelterDisplayName: String,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFF8FA), Color(0xFFFFEEF2), Color(0xFFFFE0E8))
    )
    val orgName = if (!pet.shelterName.isNullOrBlank()) pet.shelterName else "PawMate Shelter"
    val cardPad = if (isTablet) 16.dp else 10.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = cardPad, vertical = cardPad),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFB6C1).copy(alpha = 0.45f))
                .padding(horizontal = 20.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Info",
                fontSize = if (isTablet) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE85A7A)
            )
        }
        BackFaceRow(label = "Name", value = pet.name ?: "Unknown")
        BackFaceRow(label = "Age", value = pet.age ?: "N/A")
        BackFaceRow(label = "Sex", value = pet.gender ?: "Unknown")
        BackFaceRow(label = "Shelter", value = orgName ?: "Unknown")
        BackFaceRow(label = "Address", value = pet.shelterAddress ?: "Address Loading...")
        BackFaceHealthRow(status = pet.healthStatus)
    }
}

@Composable
private fun BackFaceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD67A7A),
            fontSize = 13.sp,
            modifier = Modifier.width(68.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color(0xFF333333),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BackFaceHealthRow(status: String?) {
    val medicalList = status?.split(",", "\n")?.filter { it.isNotBlank() } ?: emptyList()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Health",
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD67A7A),
            fontSize = 13.sp,
            modifier = Modifier.width(68.dp)
        )
        if (medicalList.isEmpty()) {
            Text(text = "Healthy", fontSize = 13.sp, color = Color(0xFF333333))
        } else {
            Column(modifier = Modifier.weight(1f)) {
                medicalList.forEach { point ->
                    Text(text = "• ${point.trim()}", fontSize = 12.sp, color = Color(0xFF333333))
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
    offsetY: Float = 0f,
    rotation: Float,
    rotationY: Float,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    isTablet: Boolean = false,
    userTier: Int,
    navController: NavController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    isBackgroundStack: Boolean = false,
    showSwipeOverlays: Boolean = true,
    onFrontLayerRaised: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tripleTapSpinAnim = remember(pet.name) { Animatable(0f) }
    LaunchedEffect(pet.name) {
        tripleTapSpinAnim.snapTo(0f)
    }
    var lastQuickTapMs by remember(pet.name) { mutableLongStateOf(0L) }
    var quickTapCount by remember(pet.name) { mutableIntStateOf(0) }

    var currentImageIndex by remember(pet.name) { mutableIntStateOf(0) }
    var isBackFace by remember(pet.name) { mutableStateOf(false) }
    var isPressed by remember(pet.name) { mutableStateOf(false) }

    /** True after long-press Info until finger is lifted (not tap-to-stick). */
    var holdInfoActive by remember(pet.name) { mutableStateOf(false) }

    var shelterName by remember { mutableStateOf("Loading") }
    val shelterRepository = remember { ShelterRepository() }

    val isShelterLive = authViewModel.isUserActuallyOnline(
        User(
            isOnline = pet.shelterIsOnline,
            lastActive = pet.shelterLastActive
        )
    )
    /*
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
        */


    LaunchedEffect(offsetX, holdInfoActive) {
        if (!holdInfoActive && abs(offsetX) > 14f) isBackFace = false
    }

    val flipRotation by animateFloatAsState(
        targetValue = if (isBackFace) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_flip"
    )

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
            abs(offsetX) > 10f -> {
                val dragNorm = (abs(offsetX) / 400f).coerceIn(0f, 1f)
                1f + dragNorm * 0.06f
            }

            else -> idlePulse
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "card_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = when {
            isBackgroundStack -> 4f
            isPressed || isBackFace || holdInfoActive -> 32f
            abs(offsetX) > 20f -> 12f + (abs(offsetX) / 400f).coerceAtMost(1f) * 18f
            else -> 12f
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "card_elevation"
    )

    val nameSize = if (isTablet) 32.sp else 28.sp
    val infoSize = if (isTablet) 20.sp else 16.sp
    val descSize = if (isTablet) 18.sp else 14.sp
    val cornerNameSize = (nameSize.value * 0.86f).sp
    val cornerInfoSize = (infoSize.value * 0.9f).sp
    val cornerDescSize = (descSize.value * 0.9f).sp
    val cardPadding = if (isTablet) 24.dp else 16.dp
    val indicatorSize = if (isTablet) 12.dp else 8.dp
    val indicatorSpacing = if (isTablet) 12.dp else 8.dp

    val allImages = remember(pet) {
        val list = mutableListOf<Any>()

        // 1. Add Main Cloudinary URL if it exists
        if (!pet.imageUrl.isNullOrBlank()) {
            list.add(pet.imageUrl!!)
        }

        // 2. Add Sub-Images from Cloudinary
        if (pet.additionalImages.isNotEmpty()) {
            list.addAll(pet.additionalImages)
        }

        // 3. Fallback to old resource only if Cloudinary is empty
        if (list.isEmpty()) {
            val res = pet.imageRes.takeIf { it != 0 } ?: R.drawable.petplaceholder
            list.add(res)
        }
        list
    }

    // 🆕 NEWLY ADDED: Dynamically manifest the image based on the index
    val currentImage = allImages.getOrNull(currentImageIndex) ?: R.drawable.petplaceholder



    val petCardShape = RoundedCornerShape(24.dp)
    // Match Discover screen bg so the card's axis-aligned bounds don't read as a dark slab when tilted in 3D.
    val petCardUnderfill =
        if (ThemeManager.isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)

    if (isBackgroundStack) {
        Card(
            modifier = modifier.offset { IntOffset(0, 0) },
            shape = petCardShape,
            colors = CardDefaults.cardColors(containerColor = petCardUnderfill),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(petCardShape)
            ) {
                AsyncImage(
                    model = currentImage.takeIf { !it.toString().isNullOrBlank() } ?: R.drawable.petplaceholder,                    contentDescription = pet.name,
                    placeholder = painterResource(id = R.drawable.petplaceholder),
                    error = painterResource(id = R.drawable.petplaceholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    LaunchedEffect(isPressed, isBackFace, holdInfoActive, offsetX) {
        onFrontLayerRaised(isPressed || isBackFace || holdInfoActive || abs(offsetX) > 20f)
    }

    val pressLiftPx by animateFloatAsState(
        targetValue = if (isPressed && abs(offsetX) < 15f) -11f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_lift"
    )

    val swipeFill = (abs(offsetX) / 380f).coerceIn(0f, 1f)
    val labelSize = ((if (isTablet) 40f else 34f) + swipeFill * 14f).sp
    val holdInteractionSpin =
        if (holdInfoActive && isBackFace) swipeRotationYFromOffset(offsetX) * 1.45f else 0f

    Card(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), (offsetY + pressLiftPx).roundToInt()) }
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
                this.rotationY = rotationY + tripleTapSpinAnim.value
                cameraDistance = 14f * density.density
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .pointerInput(pet.name) {
                detectDragGestures(
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                )
            }
            .pointerInput(pet.name + "_tap", userTier) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        holdInfoActive = false
                        isBackFace = false
                    },
                    onTap = { tapOffset ->
                        val cardWidthVal = size.width.toFloat()
                        // val totalImages = pet.additionalImages.size + 1
                        val totalImages = allImages.size
                        val x = tapOffset.x
                        val leftZoneEnd = cardWidthVal * 0.33f
                        val rightZoneStart = cardWidthVal * 0.67f

                        when {
                            x < leftZoneEnd && totalImages > 1 -> {
                                quickTapCount = 0
                                currentImageIndex = if (currentImageIndex > 0) currentImageIndex - 1 else totalImages - 1
                            }
                            x > rightZoneStart && totalImages > 1 -> {
                                quickTapCount = 0
                                currentImageIndex = (currentImageIndex + 1) % totalImages
                            }
                            else -> {
                                val now = System.currentTimeMillis()
                                if (now - lastQuickTapMs > 420L) quickTapCount = 0
                                lastQuickTapMs = now
                                quickTapCount++
                                if (quickTapCount >= 3) {
                                    quickTapCount = 0
                                    scope.launch {
                                        tripleTapSpinAnim.snapTo(0f)
                                        tripleTapSpinAnim.animateTo(
                                            targetValue = 360f,
                                            animationSpec = tween(
                                                durationMillis = 780,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        tripleTapSpinAnim.snapTo(0f)
                                    }
                                }
                            }
                        }
                    },
                    onLongPress = {
                        if (userTier >= 2) {
                            holdInfoActive = true
                            isBackFace = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Detailed Info requires Tier 2 Unlock!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            GemManager.openPurchaseDialog()
                        }
                    }
                )
            },
        shape = petCardShape,
        colors = CardDefaults.cardColors(containerColor = petCardUnderfill),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        cameraDistance = 11f * density.density
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                        this.rotationY = flipRotation + holdInteractionSpin
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(petCardShape)
                        .graphicsLayer {
                            alpha = if (flipRotation < 90f) 1f else 0f
                        }
                ) {
                    AsyncImage(
                        model = currentImage.takeIf { !it.toString().isNullOrBlank() } ?: R.drawable.petplaceholder,  contentDescription = pet.name,
                        placeholder = painterResource(id = R.drawable.petplaceholder),
                        error = painterResource(id = R.drawable.petplaceholder),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (showSwipeOverlays && offsetX > 10f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6B9A7E).copy(alpha = 0.22f + swipeFill * 0.68f),
                                            Color(0xFF8FB89E).copy(alpha = swipeFill * 0.55f),
                                            Color(0xFFA3C9B3).copy(alpha = swipeFill * 0.35f),
                                            Color(0xFFB5D4C4).copy(alpha = swipeFill * 0.22f)
                                        )
                                    )
                                )
                        )
                    }
                    if (showSwipeOverlays && offsetX < -10f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF8E5A5E).copy(alpha = swipeFill * 0.25f),
                                            Color(0xFFA87070).copy(alpha = swipeFill * 0.45f),
                                            Color(0xFFB57878).copy(alpha = 0.28f + swipeFill * 0.55f),
                                            Color(0xFFC49090).copy(alpha = 0.2f + swipeFill * 0.72f)
                                        )
                                    )
                                )
                        )
                    }

                    if (showSwipeOverlays && offsetX > 24f) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💚", fontSize = (36f + swipeFill * 10f).sp)
                            Text(
                                text = "LIKE",
                                color = Color.White,
                                fontSize = labelSize,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    if (showSwipeOverlays && offsetX < -24f) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "⏭", fontSize = (36f + swipeFill * 10f).sp)
                            Text(
                                text = "NEXT",
                                color = Color.White,
                                fontSize = labelSize,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Row(
                                modifier = Modifier
                                    .widthIn(max = 168.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(
                                        indication = null, // Removes the ripple so it looks like your colleague's design
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        if (!pet.shelterId.isNullOrEmpty()) {
                                            navController.navigate("profile_details/${pet.shelterId}")
                                        }
                                    }
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
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
                                    if (isShelterLive) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4ADE80))
                                                .border(1.5.dp, Color.White, CircleShape)
                                                .align(Alignment.BottomEnd)
                                                .offset(x = (-2).dp, y = (-2).dp)
                                        )
                                    }
                                }
                                Text(
                                    text = pet.shelterName ?: "Unknown Shelter",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    if (allImages.size > 1) {
                        Card(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).zIndex(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(indicatorSpacing)) {
                                repeat(allImages.size) { index ->
                                    Box(
                                        modifier = Modifier.size(indicatorSize)
                                            .background(if (index == currentImageIndex) Color.White else Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .zIndex(2f)
                            .fillMaxWidth(0.42f)
                            .wrapContentHeight(align = Alignment.Top)
                            .clip(RoundedCornerShape(topEnd = 24.dp, bottomStart = 16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.42f),
                                        Color.Black.copy(alpha = 0.82f)
                                    )
                                ),
                                RoundedCornerShape(topEnd = 24.dp, bottomStart = 16.dp)
                            )
                            .padding(start = 8.dp, top = 5.dp, end = 6.dp, bottom = 8.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = pet.name ?: "Unknown",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = cornerNameSize
                                ),
                                textAlign = TextAlign.End,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = pet.age ?: "Unknown",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = cornerInfoSize,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = pet.breed ?: "Unknown",
                                    color = Color.White,
                                    fontSize = cornerDescSize,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = pet.description ?: "no description",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = cornerDescSize,
                                lineHeight = (cornerDescSize.value * 1.35f).sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(petCardShape)
                        .graphicsLayer {
                            this.rotationY = 180f
                            alpha = if (flipRotation > 90f) 1f else 0f
                        }
                ) {
                    PetInfoBackFace(
                        pet = pet,
                        shelterDisplayName = pet.shelterName ?: "Unknown Shelter",                        isTablet = isTablet,
                        modifier = Modifier.fillMaxSize()
                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GemTierPackageCard(
    pkg: GemPackage,
    selected: Boolean,
    tierLevel: Int,
    accent: Color,
    accentSoft: Color,
    borderSelected: Color,
    muted: Color,
    onCardClick: () -> Unit,
    onBuyClick: () -> Unit,
    alignBuyToBottom: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isOwned = gemTierOwned(pkg, tierLevel)
    val diamondSize = when (pkg) {
        GemPackage.SMALL -> 40.dp
        GemPackage.MEDIUM -> 48.dp
        GemPackage.LARGE -> 56.dp
    }
    val isDark = ThemeManager.isDarkMode
    val cardBg = when {
        selected -> accentSoft.copy(alpha = if (isDark) 0.42f else 0.55f)
        isDark -> Color(0xFF2E2C2D)
        else -> Color(0xFFFDF8FA)
    }
    val perkText = gemPerkDescription(pkg)
    val perkBody = if (pkg == GemPackage.LARGE) gemPerkDescriptionUltimateUi(perkText) else perkText
    val perkMaxLines = if (pkg == GemPackage.LARGE) 4 else 3

    Card(
        modifier = modifier,
        onClick = onCardClick,
        shape = RoundedCornerShape(18.dp),
        border = if (selected) BorderStroke(2.dp, borderSelected) else null,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 0.dp,
            pressedElevation = 2.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp,
            draggedElevation = 4.dp,
            disabledElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (alignBuyToBottom) Modifier.fillMaxHeight() else Modifier)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gemTierTitle(pkg).uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = muted,
                letterSpacing = 0.8.sp
            )
            if (pkg == GemPackage.LARGE) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = accent.copy(alpha = if (isDark) 0.18f else 0.12f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★ Best value",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        letterSpacing = 0.6.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pkg.gemAmount.toString(),
                    fontSize = when (pkg) {
                        GemPackage.SMALL -> 26.sp
                        GemPackage.MEDIUM -> 28.sp
                        GemPackage.LARGE -> 32.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    lineHeight = 32.sp
                )
                Text(
                    text = " gems",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = muted,
                    modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = R.drawable.diamond),
                contentDescription = null,
                modifier = Modifier.size(diamondSize),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = gemTierSubtitle(pkg),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFE8E0E3) else Color(0xFF4A4246),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = perkBody,
                fontSize = if (pkg == GemPackage.LARGE) 9.5.sp else 10.sp,
                color = muted,
                lineHeight = if (pkg == GemPackage.LARGE) 12.sp else 13.sp,
                textAlign = TextAlign.Center,
                maxLines = perkMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (isOwned) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tier active — top up anytime",
                        fontSize = 10.sp,
                        color = muted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (alignBuyToBottom) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = onBuyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.diamond),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gemPriceOnly(pkg),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (GemPackage) -> Unit
) {
    val currentTier by GemManager.currentTier.collectAsState()
    var selected by remember { mutableStateOf(GemPackage.MEDIUM) }

    var showEligibilityDialog by remember { mutableStateOf(false) }
    var eligibilityDialogData by remember { mutableStateOf("" to "") }
    var selectedPackageForAction by remember { mutableStateOf<GemPackage?>(null) }

    val isDark = ThemeManager.isDarkMode
    val surface = if (isDark) Color(0xFF252224) else Color.White
    val muted = if (isDark) Color(0xFFB0A8AB) else Color(0xFF7A7377)
    val accent = if (isDark) Color(0xFFFF8BA8) else Color(0xFFE84D7A)
    val accentSoft = if (isDark) Color(0xFF4A3038) else Color(0xFFFFE4EE)
    val borderSelected = if (isDark) Color(0xFFFFB3C6) else Color(0xFFD63D6F)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val wide = configuration.screenWidthDp >= 560
    val dialogMaxHeight = with(density) { (configuration.screenHeightDp * 0.88f).dp }
    val gemDialogScrollNarrow = rememberScrollState()

    fun tryPurchase(pkg: GemPackage) {
        selected = pkg
        GemManager.checkTierEligibility(
            selectedPackage = pkg,
            onShowDialog = { title, message ->
                eligibilityDialogData = title to message
                selectedPackageForAction = pkg
                showEligibilityDialog = true
            },
            onDirectPurchase = { onPurchase(pkg) }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = dialogMaxHeight)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = surface,
            shadowElevation = 10.dp
        ) {
            @Composable
            fun GemDialogHeader() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Upgrade PawMate",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "Pick a gem pack for your tier — checkout unchanged.",
                                fontSize = 11.sp,
                                color = muted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = muted
                        )
                    }
                }
            }

            @Composable
            fun GemDialogFooter() {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Not now", color = muted, fontSize = 13.sp)
                    }
                }
            }

            if (wide) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    GemDialogHeader()
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GemPackage.entries.forEach { pkg ->
                            GemTierPackageCard(
                                pkg = pkg,
                                selected = selected == pkg,
                                tierLevel = currentTier.level,
                                accent = accent,
                                accentSoft = accentSoft,
                                borderSelected = borderSelected,
                                muted = muted,
                                onCardClick = { selected = pkg },
                                onBuyClick = { tryPurchase(pkg) },
                                alignBuyToBottom = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    GemDialogFooter()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .verticalScroll(gemDialogScrollNarrow)
                ) {
                    GemDialogHeader()
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GemPackage.entries.forEach { pkg ->
                            GemTierPackageCard(
                                pkg = pkg,
                                selected = selected == pkg,
                                tierLevel = currentTier.level,
                                accent = accent,
                                accentSoft = accentSoft,
                                borderSelected = borderSelected,
                                muted = muted,
                                onCardClick = { selected = pkg },
                                onBuyClick = { tryPurchase(pkg) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    GemDialogFooter()
                }
            }
        }
    }

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
}

@Composable
fun GCashMultiStepDialog(
    packageType: GemPackage,
    onDismiss: () -> Unit,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
    // 🚦 0: Final Assurance | 1: QR Code | 2: Processing | 3: Success
    var currentStep by remember { mutableIntStateOf(0) }


   //SECURITY CHECK
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val biometricHelper = remember { com.example.pawmate_ils.BiometricHelper(context) }



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
                            delay(3000)
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
                    if (currentStep < 2) {
                        // 🛡️ SECURITY ADDITION: If we are at the very start (Step 0), ask for Fingerprint
                        if (currentStep == 0 && activity != null && biometricHelper.isBiometricAvailable()) {
                            biometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = { currentStep++ }, // Success? Move to Step 1 (QR)
                                onError = { error ->
                                    android.widget.Toast.makeText(context, "Authorization Required", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            // This handles Step 1 -> Step 2, OR acts as a fallback if biometrics aren't set up
                            currentStep++
                        }
                    }
                    else if (currentStep == 3) {
                        onDismiss()
                    }
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




@Preview(showBackground = true, apiLevel = 35)
@Composable
fun PetSwipeScreenPreview() {
    val mockNavController = rememberNavController()
    PetSwipeScreen(navController = mockNavController)
}
