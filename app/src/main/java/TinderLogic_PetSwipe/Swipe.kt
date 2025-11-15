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
import com.example.pawmate_ils.Firebase_Utils.PetRepository
import com.example.pawmate_ils.R
import com.example.pawmate_ils.firebase_models.Channel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.google.firebase.Timestamp

data class PetData(
    val name: String? = null,
    val breed: String? = null,
    val age: String? = null,
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
    //track which icon is selected(swipe...etc)
    var selectedItem by remember { mutableStateOf("Swipe") }

    // Force-show on first screen entry; user dismissal persists the flag.
    var showTutorial by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(showTutorial) {
        if (showTutorial) TutorialRuntime.wasShownThisProcess = true
    }
    //Firebase essentials for authentication needed for chat creation
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val authViewModel: AuthViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner!!
    )
    val factory = remember { ChatViewModelFactory(authViewModel) }
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val firestoreRepo = remember { FirestoreRepository() }
    val likedPetsViewmodel: LikedPetsViewModel = viewModel()
    val petRepository : PetRepository = viewModel()


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
                            shelterId = shelterId,
                            shelterName = shelterUser.name,
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
            NavigationBar(
                containerColor = navBarColor,
                contentColor = textColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = "Swipe",

                            tint = if (selectedItem == "Swipe") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩 dynamically change color
                        )
                    },
                    label = {
                        Text(
                            "Swipe",
                            color = if (selectedItem == "Swipe") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩 dynamically change label color
                        )
                    },
                    selected = selectedItem == "Swipe",
                    onClick = {
                        selectedItem = "Swipe" // 🟩 update selectedItem
                        navController.navigate("pet_swipe")
                    },

                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF9999),
                        selectedTextColor = Color(0xFFFF9999),
                        indicatorColor = Color(0xFFFFD6E0)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.heart),
                            contentDescription = "Liked",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(
                                if (selectedItem == "Liked") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                            )
                        )
                    },
                    label = {
                        Text(
                            "Liked",
                            color = if (selectedItem == "Liked") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                        )
                    },
                    selected = selectedItem == "Liked",
                    onClick = {
                        selectedItem = "Liked"
                        navController.navigate("adopter_home")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF9999),
                        selectedTextColor = Color(0xFFFF9999),
                        indicatorColor = Color(0xFFFFD6E0)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id =  R.drawable.book_open),
                            contentDescription = "Learn",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(
                                if (selectedItem == "Learn") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                            )
                        )
                    },
                    label = {
                        Text(
                            "Learn",
                            color = if (selectedItem == "Learn") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                        )
                    },
                    selected = selectedItem == "Learn",
                    onClick = {
                        selectedItem = "Learn"
                        navController.navigate("educational")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF9999),
                        selectedTextColor = Color(0xFFFF9999),
                        indicatorColor = Color(0xFFFFD6E0)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.profile_d),
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(
                                if (selectedItem == "Profile") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                            )
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            color = if (selectedItem == "Profile") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                        )
                    },
                    selected = selectedItem == "Profile",
                    onClick = {
                        selectedItem = "Profile"
                        navController.navigate("profile_settings")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF9999),
                        selectedTextColor = Color(0xFFFF9999),
                        indicatorColor = Color(0xFFFFD6E0)
                    )
                )

            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.message_square),
                        contentDescription = "Message",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(
                            if (selectedItem == "Message") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                        )
                    )
                },
                label = {
                    Text(
                        "Message",
                        color = if (selectedItem == "Message") Color(0xFFFF9999) else Color.Gray.copy(alpha = 0.6f) // 🟩
                    )
                },
                selected = selectedItem == "Message",
                onClick = {
                    selectedItem = "Message"
                    navController.navigate("chat_home")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF9999),
                    selectedTextColor = Color(0xFFFF9999),
                    indicatorColor = Color(0xFFFFD6E0)
                )
            )
        }
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
                                            Text("Tap the + button to buy gems. Likes cost 5 gems.", fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Swipe right to like (costs 5 gems)", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Swipe left to pass", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Tap left/right on the card to change photos", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Hold the card to see detailed information", fontSize = 14.sp, color = Color.DarkGray)
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

            if (showGemDialog) {
                GemPurchaseDialog(
                    onDismiss = { showGemDialog = false },
                    onPurchase = { packageType ->
                        GemManager.purchaseGems(packageType)
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
    var isHolding by remember { mutableStateOf(false) }

     var shelterName by remember {mutableStateOf("Loading")}
    val scope = rememberCoroutineScope()
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
            isHolding -> 0.95f
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
            isHolding -> 24f
            abs(offsetX) > 50f -> 16f
            else -> 8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_elevation"
    )

    // Responsive sizing for card content
    val nameSize = if (isTablet) 32.sp else 28.sp
    val infoSize = if (isTablet) 20.sp else 16.sp
    val descSize = if (isTablet) 18.sp else 14.sp
    val overlayTitleSize = if (isTablet) 40.sp else 32.sp
    val overlayBodySize = if (isTablet) 24.sp else 20.sp
    val overlayDescSize = if (isTablet) 20.sp else 16.sp
    val overlayHintSize = if (isTablet) 18.sp else 14.sp
    val cardPadding = if (isTablet) 24.dp else 16.dp
    val overlayPadding = if (isTablet) 32.dp else 24.dp
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
                rotationZ = rotation
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
                        val cardWidth = size.width
                        val totalImages = pet.additionalImages.size + 1 // +1 for main image

                        if (totalImages > 1) {
                            if (offset.x < cardWidth / 2) {
                                // Left tap - previous image
                                currentImageIndex = if (currentImageIndex > 0) {
                                    currentImageIndex - 1
                                } else {
                                    totalImages - 1 // Wrap to last image
                                }
                            } else {
                                // Right tap - next image
                                currentImageIndex = (currentImageIndex + 1) % totalImages
                            }
                        }
                    },
                    onLongPress = {
                        isHolding = true
                    },
                    onPress = {
                        tryAwaitRelease()
                        isHolding = false
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
                            Color.Green.copy(alpha = likeAlpha),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💚",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "LIKE",
                            color = Color.White,
                            fontSize = 28.sp,
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
                            Color.Red.copy(alpha = passAlpha),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💔",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "PASS",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Enhanced hold information modal
            androidx.compose.animation.AnimatedVisibility(
                visible = isHolding,
                enter = fadeIn(
                    animationSpec = tween(300)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (isTablet) 0.75f else 0.92f)
                            .padding(if (isTablet) 24.dp else 16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ThemeManager.isDarkMode) Color(0xFF1E1E1E) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(if (isTablet) 32.dp else 24.dp)
                        ) {
                            // Pet avatar with animated border
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                val borderColor by animateColorAsState(
                                    targetValue = if (pet.type == "dog") Color(0xFF4CAF50) else Color(0xFF9C27B0),
                                    animationSpec = tween(1000),
                                    label = "border_color"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(if (isTablet) 120.dp else 100.dp)
                                        .background(
                                            borderColor.copy(alpha = 0.2f),
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (pet.type == "dog") "🐕" else "🐱",
                                        fontSize = if (isTablet) 60.sp else 50.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 20.dp))

                            // Pet name with gradient background
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFFB6C1).copy(alpha = 0.15f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text =  pet.name ?: "Unknown",
                                    fontSize = if (isTablet) 32.sp else 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9999),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 20.dp))

                            // Enhanced info chips with better layout
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    EnhancedInfoChip("🎂", "Age", pet.age ?: "N/A", isTablet)
                                    EnhancedInfoChip("🧬", "Breed", pet.breed ?: "Unknown", isTablet)
                                }
                                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))
                                EnhancedInfoChip(
                                    if (pet.type.equals("dog", ignoreCase = true)) "🐕" else "🐱",
                                    "Type",
                                    pet.type?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                                    isTablet
                                )
                            }

                            Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 24.dp))

                            // About section with better styling
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📖 About ${pet.name}",
                                    fontSize = if (isTablet) 22.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9999),
                                    modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = pet.description ?: "unknown",
                                        fontSize = if (isTablet) 18.sp else 16.sp,
                                        color = if (ThemeManager.isDarkMode) Color.White.copy(alpha = 0.9f) else Color.Gray,
                                        lineHeight = if (isTablet) 26.sp else 24.sp,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 20.dp))

                            // Instructions with icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFFB6C1).copy(alpha = 0.08f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "👆",
                                    fontSize = if (isTablet) 20.sp else 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Tap left/right to see more photos",
                                    fontSize = if (isTablet) 16.sp else 14.sp,
                                    color = Color(0xFFFF9999),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart) // fills the width at the bottom
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = if (isHolding) listOf(Color.Transparent, Color.Transparent)
                            else listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(cardPadding)
            ) {
                // Pet info column on the left
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
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
                        text = pet.description ?: "no description",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = descSize,
                        lineHeight = (descSize.value * 1.4f).sp
                    )
                }

                // Shelter name on the bottom-right
                Text(
                    text = " Shelter: ${shelterName}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = infoSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // now valid because we're in BoxScope
                        .padding(end = 12.dp, bottom = 8.dp),
                    textAlign = TextAlign.End
                )
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