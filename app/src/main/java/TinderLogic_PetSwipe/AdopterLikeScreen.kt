    package TinderLogic_PetSwipe

    import android.util.Log
    import androidx.compose.animation.core.*
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.Favorite
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.Pets
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.runtime.livedata.observeAsState
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalConfiguration
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavController
    import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
    import com.example.pawmate_ils.Firebase_Utils.LikedPet
    import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
    import com.example.pawmate_ils.R
    import com.example.pawmate_ils.GemManager
    //import com.example.pawmate_ils.LikedPetsManager
    import com.example.pawmate_ils.ThemeManager
    import kotlinx.coroutines.delay

    data class AdoptedPet(
        val name: String,
        val breed: String,
        val description: String,
        val imageResId: Int
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AdopterLikeScreen(navController: NavController) {
        var showGemDialog by remember { mutableStateOf(false) }
        var tapCount by remember { mutableIntStateOf(0) }
        var showDogAnimation by remember { mutableStateOf(false) }

        val AuthViewModel : AuthViewModel = viewModel()
        val authState = AuthViewModel.authState.observeAsState()
        val context = LocalContext.current
        val likedPetsViewModel: LikedPetsViewModel = viewModel()
        val likedPets by likedPetsViewModel.likedPets.collectAsState(initial = emptyList())
        LaunchedEffect(likedPets) {
            Log.d("AdopterLikeScreen", "likedPets updated: ${likedPets.size} pets")
        }
        val gemCount by GemManager.gemCount.collectAsState()


        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val isTablet = screenWidth >= 600.dp

        val isDarkMode = ThemeManager.isDarkMode
        val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
        val textColor = if (isDarkMode) Color.White else Color.Black
        val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
        val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
        val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)

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
                                tint = Color(0xFFFF9999)
                            )
                        },
                        label = { Text("Swipe", color = Color(0xFFFF9999), fontWeight = FontWeight.Bold) },
                        selected = true,
                        onClick = {navController.navigate("pet_swipe")},
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
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Liked", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
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
                                painter = painterResource(id = R.drawable.book_open),
                                contentDescription = "Learn",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Learn", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
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
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Profile", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
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
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Message", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top App Bar
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = textColor
                                )
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.clickable {
                                    tapCount++
                                    if (tapCount >= 4) {
                                        tapCount = 0
                                        showDogAnimation = true
                                    }
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.pawmate_logo),
                                    contentDescription = "PawMate Logo",
                                    modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Liked",
                                    tint = Color.Red,
                                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Liked (${likedPets.size})",
                                    fontSize = if (isTablet) 22.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFFFFF0F5)
                        ),
                        actions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💎",
                                            fontSize = if (isTablet) 18.sp else 16.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text(
                                            text = gemCount.toString(),
                                            color = Color.White,
                                            fontSize = if (isTablet) 18.sp else 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                FloatingActionButton(
                                    onClick = { showGemDialog = true },
                                    modifier = Modifier.size(if (isTablet) 36.dp else 32.dp),
                                    containerColor = accentColor,
                                    contentColor = Color.White,
                                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Buy Gems",
                                        modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Content
                    if (likedPets.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isTablet) 32.dp else 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💔",
                                fontSize = if (isTablet) 80.sp else 64.sp,
                                modifier = Modifier.padding(bottom = if (isTablet) 24.dp else 16.dp)
                            )
                            Text(
                                text = "No Liked Pets Yet",
                                fontSize = if (isTablet) 28.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = if (isTablet) 16.dp else 12.dp)
                            )
                            Text(
                                text = "Start swiping to find your perfect companion!",
                                fontSize = if (isTablet) 18.sp else 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = if (isTablet) 32.dp else 24.dp)
                            )
                            Button(
                                onClick = { navController.navigate("pet_swipe") },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier
                                    .fillMaxWidth(if (isTablet) 0.5f else 0.8f)
                                    .height(if (isTablet) 60.dp else 56.dp)
                            ) {
                                Text(
                                    text = "Find Pets",
                                    fontSize = if (isTablet) 20.sp else 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Liked pets grid
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .background(backgroundColor)
                            ,

                            contentPadding = PaddingValues(if (isTablet) 20.dp else 16.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp)
                        ) {
                            items(likedPets) { pet ->
                                LikedPetCard(
                                    pet = pet,
                                    isTablet = isTablet,
                                    textColor = textColor,
                                    cardColor = cardColor,
                                    onRemove = {
                                        likedPetsViewModel.removeLikedPet(pet.name)
                                    }
                                )
                            }
                        }
                    }
                }

                if (showDogAnimation) {
                    DogEmojiAnimation(
                        onAnimationComplete = { showDogAnimation = false }
                    )
                }

                if (showGemDialog) {
                    GemPurchaseDialog(
                        onDismiss = { showGemDialog = false },
                        onPurchase = { packageType ->
                            GemManager.purchaseGems(packageType)
                            showGemDialog = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun LikedPetCard(
        pet: LikedPet,
        isTablet: Boolean,
        textColor: Color,
        cardColor: Color,
        onRemove: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 140.dp else 120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTablet) 16.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pet Image
                Image(
                    painter = painterResource(id = pet.imageRes),
                    contentDescription = pet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(if (isTablet) 100.dp else 80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(if (isTablet) 16.dp else 12.dp))

                // Pet Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pet.name,
                        fontSize = if (isTablet) 22.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${pet.breed} • ${pet.age}",
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pet.description,
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }

                // Remove Button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .background(
                            Color.Red.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.Red,
                        modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun DogEmojiAnimation(
        onAnimationComplete: () -> Unit
    ) {
        var isVisible by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(2000)
            isVisible = false
            onAnimationComplete()
        }

        if (isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                // Multiple dog emojis moving across screen
                repeat(8) { index ->
                    val animatedOffset by rememberInfiniteTransition(label = "dog_animation").animateFloat(
                        initialValue = -200f,
                        targetValue = 1200f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 2000,
                                delayMillis = index * 200
                            ),
                            repeatMode = RepeatMode.Restart
                        ), label = "dog_offset"
                    )

                    Text(
                        text = "🐕",
                        fontSize = (24 + index * 4).sp,
                        modifier = Modifier
                            .offset(
                                x = animatedOffset.dp,
                                y = (index * 40 - 160).dp
                            )
                    )
                }
            }
        }
    }
