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
import androidx.compose.material.icons.filled.Message
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.GemPackage
import com.example.pawmate_ils.LikedPetsManager
import com.example.pawmate_ils.ThemeManager
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModelFactory
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.firebase_models.Channel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

data class PetData(
    val name: String,
    val breed: String,
    val age: String,
    val description: String,
    val type: String,
    val imageRes: List<Int> = emptyList(),
    val additionalImages: List<Int> = emptyList(),
    val shelterId: String = "",
    val shelterName: String = "",
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
        var gemCount by remember { mutableIntStateOf(GemManager.gemCount) }

        //Firebase essentials for authentication needed for chat creation
        val viewModelStoreOwner = LocalViewModelStoreOwner.current
        val authViewModel: AuthViewModel = viewModel(
            viewModelStoreOwner = viewModelStoreOwner!!
        )
        val factory = remember { ChatViewModelFactory(authViewModel) }
        val chatViewModel: ChatViewModel = viewModel(factory = factory)
        val firestoreRepo = remember { FirestoreRepository() }


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
        // Force-show on first screen entry; user dismissal persists the flag.
        var showTutorial by rememberSaveable { mutableStateOf(true) }
        LaunchedEffect(showTutorial) {
            if (showTutorial) TutorialRuntime.wasShownThisProcess = true
        }
        //Instanly show chat channels after swiping right
        LaunchedEffect(Unit) {
            homeViewModel.listenToChannels()
        }

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val density = LocalDensity.current

        // Determine if device is tablet based on smallest width (more reliable) and fall back to widthDp
        val isTablet = configuration.smallestScreenWidthDp >= 600 || configuration.screenWidthDp >= 600
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val isDarkMode = ThemeManager.isDarkMode
        val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
        val textColor = if (isDarkMode) Color.White else DarkBrown
        val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

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

        val allPets = listOf(
            PetData(
                "Max",
                "Golden Retriever",
                "2 years",
                "Friendly and energetic",
                "dog",
                imageRes = listOf(R.drawable.dog1), // main image URL
                additionalImages = listOf(R.drawable.dogsub1, R.drawable.dogsub2)
            ),
            PetData(
                "Charlie",
                "Labrador",
                "1 year",
                "Playful and loyal",
                "dog",
                imageRes = listOf(R.drawable.shitzu),
                additionalImages = listOf(R.drawable.shitzusub1, R.drawable.shitzusub2),
                shelterName = "Shelter Name"
            ),
            PetData(
                "Rocky",
                "German Shepherd",
                "3 years",
                "Protective and smart",
                "dog",
                imageRes = listOf(R.drawable.chow),
                additionalImages = listOf(R.drawable.chowsub1, R.drawable.chowsub2)
            ),
            PetData(
                "Alexa",
                "Persian",
                "1 year",
                "Playful and agile",
                "cat",
                imageRes = listOf(R.drawable.posaaa1),
                additionalImages = listOf(R.drawable.posaadd1, R.drawable.posaadd2)
            ),
            PetData(
                "Yuri",
                "Garfield",
                "2 years",
                "Smart and loyal",
                "cat",
                imageRes = listOf(R.drawable.posaaa2),
                additionalImages = listOf(R.drawable.posaaa1, R.drawable.posaadd2)
            ),
            PetData(
                "Oggy",
                "Siberian",
                "6 months",
                "Independent and cuddly",
                "cat",
                imageRes = listOf(R.drawable.posaaaa2),
                additionalImages = listOf(R.drawable.posaaaa1, R.drawable.posaaaa2)
            )
    )


    val filteredPets = when (petFilter) {
        PetFilter.ALL -> allPets
        PetFilter.DOGS -> allPets.filter { it.type == "dog" }
        PetFilter.CATS -> allPets.filter { it.type == "cat" }
    }


    @SuppressLint("SuspiciousIndentation")
    fun swipeCard(direction: Float) {
        if (isDragging || currentPetIndex >= filteredPets.size) return

        if (direction > 0) {
            if (GemManager.gemCount >= 5) {
                repeat(5) { GemManager.consumeGem() }
                val currentPet = filteredPets[currentPetIndex]
                likedPets.add(currentPet.name)
                LikedPetsManager.addLikedPet(currentPet)
                gemCount = GemManager.gemCount
                scope.launch {
                    try {
                        // ✅ Get shelter info from Firestore
                        val allUsers = firestoreRepo.getAllUsers()
                        val shelterUser = allUsers.firstOrNull { it.role == "shelter" }

                        if (shelterUser != null) {
                            val adopterId = authViewModel.currentUser?.uid ?: ""
                            val adopterName = authViewModel.currentUser?.displayName ?: "Unknown"
                            val shelterId = shelterUser.id
                            val shelterName = shelterUser.name
                            val petName = currentPet.name

                            // ✅ Prevent duplicate channel creation
                            val existingChannel = homeViewModel.channels.value.firstOrNull {
                                (it.adopterId == adopterId && it.shelterId == shelterId && it.petName == petName)
                            }

                            if (existingChannel != null) {
                                Log.d("PetSwipe", "⚠️ Channel already exists. Redirecting to message screen...")
                                navController.navigate("chat_screen/${existingChannel.channelId}")
                                return@launch
                            }

                            // ✅ Create a new channel
                            val channel = Channel(
                                channelId = "$adopterId-$shelterId-$petName",
                                adopterId = adopterId,
                                adopterName = adopterName,
                                shelterId = shelterId,
                                shelterName = shelterName,
                                petName = petName,
                                lastMessage = "",
                                timestamp = System.currentTimeMillis(),
                                unreadCount = 0,
                                createdAt = System.currentTimeMillis()
                            )

                            Log.d("PetSwipe", "✅ Creating channel in RTDB: $channel")

                            // ✅ Add channel to RTDB
                            homeViewModel.addChannel(channel)

                            // ✅ Navigate directly to the new chat screen


                        } else {
                            Log.w("PetSwipe", "⚠️ No shelter found in Firestore for pet: ${currentPet.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("PetSwipe", "❌ Error creating RTDB channel", e)
                    }
                }
            } else {
                showGemDialog = true
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
            NavigationBar(
                containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
                contentColor = textColor
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Pets, 
                            contentDescription = "Swipe",
                            tint = textColor
                        ) 
                    },
                    label = { Text("Swipe", color = textColor) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Favorite, 
                            contentDescription = "Liked",
                            tint = textColor
                        ) 
                    },
                    label = { Text("Liked", color = textColor) },
                    selected = false,
                    onClick = { 
                        navController.navigate("adopter_home")
                    }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Home, 
                            contentDescription = "Profile",
                            tint = textColor
                        ) 
                    },
                    label = { Text("Profile", color = textColor) },
                    selected = false,
                    onClick = { 
                        navController.navigate("profile_settings")
                    }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = "Message",
                            tint = textColor
                        )
                    },
                    label = { Text("Message", color = textColor) },
                    selected = false,
                    onClick = {
                        navController.navigate("chat_home")
                    }
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
                    color = DarkBrown,
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
                            color = DarkBrown,
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
                        containerColor = DarkBrown,
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
                    .padding(paddingSize),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                Text(
                    text = when (petFilter) {
                        PetFilter.DOGS -> "Find Your Perfect Dog!"
                        PetFilter.CATS -> "Find Your Perfect Cat!"
                        PetFilter.ALL -> "Find Your Perfect Pet!"
                    },
                    fontSize = titleFontSize,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = if (isTablet) 60.dp else 40.dp, bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showTutorial = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Show Tutorial",
                                tint = textColor
                            )
                        }
                        IconButton(
                            onClick = { showFilterDialog = true }
                        ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = textColor
                        )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBrown),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💎",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = gemCount.toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                                }
                            }
                            
                            FloatingActionButton(
                                onClick = { showGemDialog = true },
                                modifier = Modifier.size(32.dp),
                                containerColor = DarkBrown,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Buy Gems",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                    color = DarkBrown
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
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBrown, contentColor = Color.White)
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
                    gemCount = GemManager.gemCount
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
    var currentImageIndex by remember(pet.name) { mutableIntStateOf(0) }
    var isHolding by remember { mutableStateOf(false) }
    
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
    Card(
        modifier = modifier
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle background color overlay based on swipe direction
            val backgroundOverlay by animateColorAsState(
                targetValue = when {
                    offsetX > 100f -> Color.Green.copy(alpha = 0.1f)
                    offsetX < -100f -> Color.Red.copy(alpha = 0.1f)
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
                currentImageIndex == 0 -> pet.imageRes.firstOrNull()
                currentImageIndex <= pet.additionalImages.size -> pet.additionalImages[currentImageIndex - 1]
                else -> pet.imageRes.firstOrNull()
            }

            if (currentImage != null) {
                AsyncImage(
                    model = currentImage,
                    contentDescription = pet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.chowsub1), // fallback image
                    contentDescription = "No image available",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            
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
                                        DarkBrown.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = pet.name,
                                    fontSize = if (isTablet) 32.sp else 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBrown,
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
                                    EnhancedInfoChip("🎂", "Age", pet.age, isTablet)
                                    EnhancedInfoChip("🧬", "Breed", pet.breed, isTablet)
                                }
                                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))
                                EnhancedInfoChip(
                                    if (pet.type == "dog") "🐕" else "🐱", 
                                    "Type", 
                                    pet.type.replaceFirstChar { it.uppercase() }, 
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
                                    color = DarkBrown,
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
                                        text = pet.description,
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
                                        DarkBrown.copy(alpha = 0.05f),
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
                                    color = DarkBrown,
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
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = if (isHolding) 0f else 0.6f),
                        RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(cardPadding)
            ) {
                Column {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = nameSize
                        )
                    )
                    Text(
                        text = pet.age,
                        color = Color.White,
                        fontSize = infoSize
                    )
                    Text(
                        text = pet.description,
                        color = Color.White,
                        fontSize = descSize
                    )
                    Text(
                        text = pet.breed,
                        color = Color.White,
                        fontSize = descSize
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
        colors = CardDefaults.cardColors(containerColor = DarkBrown.copy(alpha = 0.1f)),
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
                color = DarkBrown,
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
            containerColor = DarkBrown.copy(alpha = 0.08f)
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
                color = DarkBrown,
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
                color = DarkBrown
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
                            colors = RadioButtonDefaults.colors(selectedColor = DarkBrown)
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
                color = DarkBrown
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
                                DarkBrown.copy(alpha = 0.1f) 
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
                                    color = DarkBrown
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
                            color = DarkBrown
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
            Button(onClick = { onPurchase(selected) }, colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)) {
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