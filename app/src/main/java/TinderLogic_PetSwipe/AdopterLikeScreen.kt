package TinderLogic_PetSwipe

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.LikedPet
import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import kotlinx.coroutines.delay

/**
 * [painterResource] only supports types that decode to a bitmap/vector.
 * Liked pets may reference a solid [ColorDrawable] ID (valid res but crashes on load).
 */
private fun android.content.Context.canLoadDrawableAsComposePainter(resId: Int): Boolean {
    if (resId == 0 || resId == -1) return false
    return try {
        resources.getResourceName(resId)
        when (val d = ContextCompat.getDrawable(this, resId)) {
            null -> false
            is ColorDrawable -> false
            is BitmapDrawable, is VectorDrawable -> true
            else -> d.javaClass.name.contains("VectorDrawable", ignoreCase = true)
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun AdopterLikeScreen(navController: NavController) {
    var showGemDialog by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }
    var showDogAnimation by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val likedPetsViewModel: LikedPetsViewModel = viewModel()
    val likedPets by likedPetsViewModel.likedPets.collectAsState(initial = emptyList())
    LaunchedEffect(likedPets) {
        Log.d("AdopterLikeScreen", "likedPets updated: ${likedPets.size} pets")
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    val isDarkMode = ThemeManager.isDarkMode
    val pageBg = if (isDarkMode) Color(0xFF1C1A1B) else Color(0xFFF7F2F5)
    val surface = if (isDarkMode) Color(0xFF262224) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkMode) Color(0xFFF8F0F3) else Color(0xFF1A1A1A)
    val subtitleColor = if (isDarkMode) Color(0xFFB0A8AB) else Color(0xFF7A7377)
    val pinkAccent = if (isDarkMode) Color(0xFFFF7BA1) else Color(0xFFE84D7A)
    val heartColor = if (isDarkMode) Color(0xFFFF6B8A) else Color(0xFFE85A6A)
    val filteredPets = remember(likedPets, searchQuery) {
        if (searchQuery.isBlank()) likedPets
        else likedPets.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.breed.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = pageBg,
        bottomBar = {
            AdopterBottomBar(navController = navController, selectedTab = "Favorites")
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                tapCount++
                                if (tapCount >= 4) {
                                    tapCount = 0
                                    showDogAnimation = true
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = pinkAccent,
                            modifier = Modifier.size(if (isTablet) 26.dp else 22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FAVORITES — ${likedPets.size}",
                            fontSize = if (isTablet) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { searchOpen = !searchOpen }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search favorites",
                            tint = pinkAccent
                        )
                    }
                }

                if (searchOpen) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Search…", color = subtitleColor) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = pinkAccent,
                            unfocusedBorderColor = subtitleColor.copy(alpha = 0.35f),
                            focusedTextColor = titleColor,
                            unfocusedTextColor = titleColor,
                            cursorColor = pinkAccent,
                            focusedContainerColor = surface,
                            unfocusedContainerColor = surface
                        )
                    )
                }

                if (likedPets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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
                            text = "No favorites yet",
                            fontSize = if (isTablet) 26.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
                        )
                        Text(
                            text = "Start swiping to save pets you love.",
                            fontSize = if (isTablet) 17.sp else 15.sp,
                            color = subtitleColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = if (isTablet) 28.dp else 22.dp)
                        )
                        Button(
                            onClick = { navController.navigate("pet_swipe") },
                            colors = ButtonDefaults.buttonColors(containerColor = pinkAccent),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth(if (isTablet) 0.5f else 0.82f)
                                .height(if (isTablet) 56.dp else 52.dp)
                        ) {
                            Text(
                                text = "Find pets",
                                fontSize = if (isTablet) 18.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                } else if (filteredPets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches for \"$searchQuery\"",
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(pageBg),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = filteredPets,
                            key = { p -> p.documentId.ifBlank { p.name } + p.name }
                        ) { pet ->
                            FavoriteListRow(
                                pet = pet,
                                isTablet = isTablet,
                                titleColor = titleColor,
                                subtitleColor = subtitleColor,
                                heartColor = heartColor,
                                onUnfavorite = { likedPetsViewModel.removeLikedPet(pet) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = subtitleColor.copy(alpha = 0.12f)
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
private fun SafePetImage(
    imageRes: Int,
    petName: String,
    size: Dp,
    circle: Boolean = true
) {
    val context = LocalContext.current
    var isValidResource by remember(imageRes) { mutableStateOf(false) }

    LaunchedEffect(imageRes) {
        isValidResource = context.canLoadDrawableAsComposePainter(imageRes)
    }

    val shape = if (circle) CircleShape else RoundedCornerShape(12.dp)
    if (isValidResource) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = petName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(
                    if (ThemeManager.isDarkMode)
                        Color(0xFF5C2D3F).copy(alpha = 0.5f)
                    else
                        Color(0xFFFFE4EE)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = petName,
                tint = if (ThemeManager.isDarkMode) Color(0xFFFF7BA1) else Color(0xFFE84D7A),
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun FavoriteListRow(
    pet: LikedPet,
    isTablet: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    heartColor: Color,
    onUnfavorite: () -> Unit
) {
    val avatarSize = if (isTablet) 56.dp else 52.dp
    val subtitle = when {
        pet.breed.isNotBlank() && pet.age.isNotBlank() -> "${pet.breed} · ${pet.age}"
        pet.breed.isNotBlank() -> pet.breed
        pet.age.isNotBlank() -> pet.age
        pet.type.isNotBlank() -> pet.type
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SafePetImage(
            imageRes = pet.imageRes,
            petName = pet.name,
            size = avatarSize,
            circle = true
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = pet.name,
                fontSize = if (isTablet) 18.sp else 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = if (isTablet) 14.sp else 13.sp,
                    color = subtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onUnfavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Remove from favorites",
                tint = heartColor,
                modifier = Modifier.size(if (isTablet) 26.dp else 24.dp)
            )
        }
    }
}

@Composable
private fun DogEmojiAnimation(
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
            repeat(8) { index ->
                val animatedOffset by rememberInfiniteTransition(label = "dog_anim_$index").animateFloat(
                    initialValue = -200f,
                    targetValue = 1200f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            delayMillis = index * 200
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dog_offset_$index"
                )

                Text(
                    text = "🐕",
                    fontSize = (24 + index * 4).sp,
                    modifier = Modifier.offset(
                        x = animatedOffset.dp,
                        y = (index * 40 - 160).dp
                    )
                )
            }
        }
    }
}
