package TinderLogic_PetSwipe

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.*
import com.example.pawmate_ils.AdopShelDataStruc.ShelterRepository
import com.example.pawmate_ils.Firebase_Utils.*
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.screens.ProfileRequirementDialog
import com.google.firebase.database.PropertyName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// --- DATA STRUCTURE (Right Logic) ---
data class PetData(
    val petId: String? = null,
    val name: String? = null,
    val breed: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val healthStatus: String? = null,
    val type: String? = null,
    val imageRes: Int = 0,
    val additionalImages: List<Int> = emptyList(),
    val shelterId: String? = null,
    val shelterName: String? = null,
    val shelterAddress: String? = null,
    val validationStatus: Boolean = false,
    @get:PropertyName("shelterIsOnline") @set:PropertyName("shelterIsOnline") var shelterIsOnline: Boolean = false,
    @get:PropertyName("shelterLastActive") @set:PropertyName("shelterLastActive") var shelterLastActive: Long? = null
)

enum class PetFilter { ALL, DOGS, CATS }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PetSwipeScreen(navController: NavController) {
    var currentPetIndex by remember { mutableIntStateOf(0) }
    var petFilter by remember { mutableStateOf(PetFilter.ALL) }
    var offsetX by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var rotation by remember(currentPetIndex) { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Logic States
    var showGemDialog by remember { mutableStateOf(false) }
    var showGCashFlow by remember { mutableStateOf(false) }
    var selectedPkg by remember { mutableStateOf(GemPackage.MEDIUM) }
    var selectedItem by remember { mutableStateOf("Swipe") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = LocalViewModelStoreOwner.current!!)
    val homeViewModel: HomeViewModel = viewModel()
    val likedPetsViewmodel: LikedPetsViewModel = viewModel()
    val petRepository: PetsRepository = viewModel()
    val firestoreRepo = remember { FirestoreRepository() }

    // Tutorial (Left)
    val tutorialPrefs = remember(context) { context.getSharedPreferences("swipe_tutorial", android.content.Context.MODE_PRIVATE) }
    val tutorialSeen = remember { tutorialPrefs.getBoolean("seen", false) }
    var showTutorial by remember { mutableStateOf(!tutorialSeen) }

    val gemCount by GemManager.gemCount.collectAsState()
    val currentTier by GemManager.currentTier.collectAsState()

    // Tablet Layout Logic (Left)
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    val cardWidth = if (isTablet && isPortrait) screenWidth * 0.86f else if (!isTablet && isPortrait) screenWidth * 0.92f else screenWidth * 0.70f
    val cardHeight = if (isTablet && isPortrait) screenHeight * 0.74f else screenHeight * 0.82f

    // Theme Colors
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)

    // Data Fetching (Right)
    val adoptionViewModel: AdoptionCenterViewModel = viewModel(factory = AdoptionCenterViewMdelFactory(authViewModel))
    val firestorePets by adoptionViewModel.shelterPets.collectAsState()
    val allPets by petRepository.allPets.collectAsState()

    val combinedPets by remember(allPets, firestorePets) {
        derivedStateOf { (firestorePets + allPets).distinctBy { it.petId ?: "${it.name}-${it.shelterId}" } }
    }

    val filteredPets by remember(combinedPets, petFilter) {
        derivedStateOf {
            when (petFilter) {
                PetFilter.ALL -> combinedPets
                PetFilter.DOGS -> combinedPets.filter { it.type == "dog" }
                PetFilter.CATS -> combinedPets.filter { it.type == "cat" }
            }
        }
    }

    LaunchedEffect(Unit) {
        GemManager.init(context)
        homeViewModel.listenToChannels()
    }

    fun swipeCard(direction: Float) {
        if (isDragging || currentPetIndex >= filteredPets.size) return
        if (direction > 0) {
            if (GemManager.consumeGems(5)) {
                val currentPet = filteredPets[currentPetIndex]
                likedPetsViewmodel.addLikedPet(currentPet)
                scope.launch {
                    try {
                        val shelterId = currentPet.shelterId ?: return@launch
                        val allUsers = firestoreRepo.getAllUsers()
                        val shelterUser = allUsers.find { it.id == shelterId } ?: return@launch
                        val adopterId = authViewModel.currentUser?.uid ?: return@launch

                        val channel = Channel(
                            channelId = "$adopterId-$shelterId-${currentPet.name}",
                            adopterId = adopterId,
                            adopterName = authViewModel.currentUser?.displayName ?: "Unknown",
                            adopterPhotoUri = allUsers.find { it.id == adopterId }?.photoUri,
                            shelterId = shelterId,
                            shelterName = shelterUser.name,
                            shelterPhotoUri = shelterUser.photoUri,
                            petName = currentPet.name ?: "Unknown",
                            timestamp = System.currentTimeMillis(),
                            adopterTier = currentTier.level,
                            isPriority = currentTier.level == 3
                        )
                        homeViewModel.addChannel(channel)
                    } catch (e: Exception) { Log.e("PetSwipe", "Channel error", e) }
                }
            } else {
                GemManager.openPurchaseDialog()
                return
            }
        }

        isDragging = true
        scope.launch {
            animate(initialValue = offsetX, targetValue = if (direction > 0) 1200f else -1200f, animationSpec = tween(220)) { value, _ ->
                offsetX = value
                rotation = (value / 15f).coerceIn(-30f, 30f)
            }
            currentPetIndex++
            offsetX = 0f
            rotation = 0f
            isDragging = false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White) {
                listOf("Swipe" to Icons.Filled.Pets, "Liked" to R.drawable.heart, "Learn" to R.drawable.book_open, "Profile" to R.drawable.profile_d, "Message" to R.drawable.message_square).forEach { (name, icon) ->
                    NavigationBarItem(
                        selected = selectedItem == name,
                        onClick = {
                            selectedItem = name
                            when(name) {
                                "Swipe" -> {}
                                "Liked" -> navController.navigate("adopter_home")
                                "Learn" -> navController.navigate("educational")
                                "Profile" -> navController.navigate("profile_settings")
                                "Message" -> navController.navigate("chat_home")
                            }
                        },
                        icon = {
                            if (icon is androidx.compose.ui.graphics.vector.ImageVector) Icon(icon, null, tint = if(selectedItem == name) primaryColor else Color.Gray)
                            else Image(painterResource(icon as Int), null, Modifier.size(24.dp), colorFilter = ColorFilter.tint(if(selectedItem == name) primaryColor else Color.Gray))
                        },
                        label = { Text(name, color = if(selectedItem == name) primaryColor else Color.Gray) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = backgroundColor) {
            if (currentPetIndex >= filteredPets.size) {
                // End stack UI from Left...
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("All viewed!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Button(onClick = { currentPetIndex = 0 }, colors = ButtonDefaults.buttonColors(primaryColor)) { Text("Swipe Again") }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    // Header (Left UI Style)
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Image(painterResource(R.drawable.blackpawmateicon3), null, Modifier.size(56.dp))
                            Column {
                                Text("Discover", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text("Your companion awaits", fontSize = 14.sp, color = textColor.copy(0.6f))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(colors = CardDefaults.cardColors(accentColor), shape = RoundedCornerShape(24.dp)) {
                                Text("💎 $gemCount", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showTutorial = true }) { Icon(Icons.Default.Info, null, tint = primaryColor) }
                        }
                    }

                    // Swipe Deck (Merged Visuals)
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (currentPetIndex + 1 < filteredPets.size) {
                            // Colleagues "Next Card Scale" logic
                            val backgroundScale by animateFloatAsState(if (abs(offsetX) > 100f) 0.98f else 0.95f)
                            SwipeablePetCard(filteredPets[currentPetIndex+1], 0f, 0f, {}, {}, isTablet, currentTier.level, navController, authViewModel, Modifier.scale(backgroundScale))
                        }
                        SwipeablePetCard(
                            filteredPets[currentPetIndex], offsetX, rotation,
                            onDrag = { delta -> offsetX = (offsetX + delta).coerceIn(-600f, 600f); rotation = offsetX / 50f },
                            onDragEnd = { if (abs(offsetX) > 150f) swipeCard(offsetX) else scope.launch { animate(offsetX, 0f) { v, _ -> offsetX = v; rotation = v/50f } } },
                            isTablet, currentTier.level, navController, authViewModel
                        )
                    }
                }
            }

            // Dialogs Logic (Right)
            if (showGemDialog) GemPurchaseDialog({ showGemDialog = false }, { selectedPkg = it; showGemDialog = false; showGCashFlow = true })
            if (showGCashFlow) GCashMultiStepDialog(selectedPkg, { showGCashFlow = false }, homeViewModel, authViewModel)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeablePetCard(
    pet: PetData, offsetX: Float, rotation: Float, onDrag: (Float) -> Unit, onDragEnd: () -> Unit,
    isTablet: Boolean, userTier: Int, navController: NavController, authViewModel: AuthViewModel, modifier: Modifier = Modifier
) {
    var currentImageIndex by remember(pet.name) { mutableIntStateOf(0) }
    var isHolding by remember { mutableStateOf(false) }
    var shelterName by remember { mutableStateOf("Loading") }
    val shelterRepo = remember { ShelterRepository() }
    val context = LocalContext.current

    // Heartbeat System (Right)
    val isShelterLive = authViewModel.isUserActuallyOnline(User(isOnline = pet.shelterIsOnline, lastActive = pet.shelterLastActive))

    LaunchedEffect(pet.shelterId) {
        shelterName = shelterRepo.getShelterNameById(pet.shelterId ?: "") ?: "Unknown Shelter"
    }

    Card(
        modifier = modifier.width(if (isTablet) 400.dp else 320.dp).heightIn(min = 450.dp, max = 600.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .scale(if(isHolding) 0.95f else 1f)
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(pet.name) { detectHorizontalDragGestures(onDragEnd = onDragEnd) { _, drag -> onDrag(drag) } }
            .pointerInput(pet.name + "_tap") {
                detectTapGestures(
                    onTap = { offset ->
                        val total = pet.additionalImages.size + 1
                        if (total > 1) {
                            if (offset.x < size.width / 2) currentImageIndex = if (currentImageIndex > 0) currentImageIndex - 1 else total - 1
                            else currentImageIndex = (currentImageIndex + 1) % total
                        }
                    },
                 onLongPress = {
                        if (userTier >= 2) isHolding = true
                        else {
                            android.widget.Toast.makeText(context, "Detailed Info requires Tier 2!", android.widget.Toast.LENGTH_SHORT).show()
                            GemManager.openPurchaseDialog()
                        }
                    },
                    onPress = { tryAwaitRelease(); isHolding = false }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(if (isHolding) 24.dp else 8.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(painterResource(if (currentImageIndex == 0) pet.imageRes else pet.additionalImages[currentImageIndex - 1]), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            // Shelter Badge (Left Style + Right Logic)
            Row(Modifier.align(Alignment.TopStart).padding(16.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(0.3f)).padding(8.dp).clickable { navController.navigate("profile_details/${pet.shelterId}") }) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(if (isShelterLive) Color(0xFF4ADE80) else Color.Gray))
                Spacer(Modifier.width(6.dp))
                Text(shelterName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Bottom Info Overlay (Left Visuals)
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))).padding(16.dp)) {
                Column {
                    Text(pet.name ?: "Unknown", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(pet.breed ?: "Unknown", fontSize = 16.sp, color = Color.White.copy(0.9f))
                }
            }

            // Hold Modal (Right Logic: Detail Boxes + bullet points)
            AnimatedVisibility(visible = isHolding, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), Alignment.Center) {
                    Card(Modifier.fillMaxWidth(0.92f).padding(16.dp), colors = CardDefaults.cardColors(if(ThemeManager.isDarkMode) Color(0xFF2A2A2A) else Color.White), shape = RoundedCornerShape(32.dp)) {
                        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Detailed Info", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD67A7A))
                            Spacer(Modifier.height(16.dp))
                            DetailBox("Name", pet.name ?: "Unknown")
                            DetailBox("Age", pet.age ?: "N/A")
                            DetailBox("Health", pet.healthStatus ?: "Healthy")
                            DetailBox("Shelter", shelterName)
                            DetailBox("Address", pet.shelterAddress ?: "Hidden")
                        }
                    }
                }
            }
        }
    }
}

// --- SHARED LOGIC COMPONENTS (Right) ---

@Composable
fun GemPurchaseDialog(onDismiss: () -> Unit, onPurchase: (GemPackage) -> Unit) {
    val currentTier by GemManager.currentTier.collectAsState()
    var selected by remember { mutableStateOf(GemPackage.MEDIUM) }
    var showEligibilityDialog by remember { mutableStateOf(false) }
    var eligibilityDialogData by remember { mutableStateOf("" to "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upgrade PawMate", fontWeight = FontWeight.Bold, color = Color(0xFFFF9999)) },
        text = {
            Column {
                GemPackage.entries.forEach { pkg ->
                    val owned = when(pkg) { GemPackage.SMALL -> false; GemPackage.MEDIUM -> currentTier.level >= 2; GemPackage.LARGE -> currentTier.level >= 3 }
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selected = pkg }, colors = CardDefaults.cardColors(if(selected == pkg) Color(0xFFFFB6C1).copy(0.15f) else Color(0xFFF8F8F8))) {
                        Row(Modifier.padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${pkg.gemAmount} Gems", fontWeight = FontWeight.Bold, color = Color(0xFFFF9999))
                                Text(pkg.price, fontSize = 10.sp, color = Color.Gray)
                            }
                            if (owned) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                GemManager.checkTierEligibility(selected, { t, m -> eligibilityDialogData = t to m; showEligibilityDialog = true }, { onPurchase(selected) })
            }, colors = ButtonDefaults.buttonColors(Color(0xFFFFB6C1))) { Text("Purchase") }
        }
    )
    if (showEligibilityDialog) {
        AlertDialog(onDismissRequest = { showEligibilityDialog = false }, title = { Text(eligibilityDialogData.first) }, text = { Text(eligibilityDialogData.second) }, confirmButton = { Button(onClick = { onPurchase(selected); showEligibilityDialog = false }) { Text("Continue") } })
    }
}

@Composable
fun GCashMultiStepDialog(packageType: GemPackage, onDismiss: () -> Unit, homeViewModel: HomeViewModel, authViewModel: AuthViewModel) {
    var step by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = { if(step != 2) onDismiss() },
        title = { Text(if(step < 2) "Confirm Purchase" else "Success!", fontWeight = FontWeight.Bold, color = Color(0xFFFF9999)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when(step) {
                    0 -> Text("Buy ${packageType.gemAmount} Gems for ${packageType.price.split("|")[0]}?")
                    1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Scan QR with GCash", fontSize = 14.sp)
                        Icon(Icons.Default.QrCodeScanner, null, Modifier.size(160.dp), tint = Color(0xFFFFB6C1).copy(0.7f))
                    }
                    2 -> {
                        LaunchedEffect(Unit) {
                            delay(4000)
                            GemManager.confirmPurchase(authViewModel.currentUser?.uid ?: "") {
                                if(packageType == GemPackage.LARGE) homeViewModel.syncExistingChannelsToTier3()
                            }
                            step = 3
                        }
                        CircularProgressIndicator(color = Color(0xFFFF9999))
                    }
                    3 -> Text("Transaction Complete! Gems Added.")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(step < 2) step++ else if(step == 3) onDismiss() }, colors = ButtonDefaults.buttonColors(Color(0xFFFFB6C1))) {
                Text(if(step == 0) "Proceed" else if(step == 1) "I've Scanned" else "Done")
            }
        }
    )
}

@Composable
fun DetailBox(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, Color(0xFFFFB6C1).copy(0.5f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(label, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD67A7A), fontSize = 16.sp)
        Text(value, color = if(ThemeManager.isDarkMode) Color.White else Color.Black)
    }
}
