package TinderLogic_PetSwipe

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.pawmate_ils.Firebase_Utils.AuthState
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.R
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.GemPackage
import com.example.pawmate_ils.LikedPetsManager
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.theme.DarkBrown
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
    val gemCount by remember { mutableStateOf(GemManager.gemCount) }
    var tapCount by remember { mutableIntStateOf(0) }
    var showDogAnimation by remember { mutableStateOf(false) }

    val AuthViewModel : AuthViewModel = viewModel()
    val authState = AuthViewModel.authState.observeAsState()
    val context = LocalContext.current
    
    // Get liked pets from the manager
    val likedPets by remember { derivedStateOf { LikedPetsManager.likedPets } }
    
    // Responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp
    
    // Dark mode support
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color.White else DarkBrown
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

    Box(modifier = Modifier.fillMaxSize()) {
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
                        modifier = Modifier.clickable {
                            tapCount++
                            if (tapCount >= 4) {
                                tapCount = 0
                                showDogAnimation = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked",
                            tint = Color.Red,
                            modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liked Pets (${likedPets.size})",
                            fontSize = if (isTablet) 22.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                ),
                actions = {
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
                            containerColor = DarkBrown,
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
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBrown),
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
                    modifier = Modifier.fillMaxSize(),
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
                                LikedPetsManager.removeLikedPet(pet)
                            }
                        )
                    }
                }
            }
        }

        // Dog emoji animation
        if (showDogAnimation) {
            DogEmojiAnimation(
                onAnimationComplete = { showDogAnimation = false }
            )
        }

        // Gem Purchase Dialog
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

@Composable
fun LikedPetCard(
    pet: PetData,
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
