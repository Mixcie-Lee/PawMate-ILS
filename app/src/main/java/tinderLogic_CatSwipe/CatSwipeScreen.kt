package TinderLogic_CatSwipe

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.GemPackage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class CatData(
    val name: String,
    val breed: String,
    val age: String,
    val description: String
)

@Composable
fun CatSwipeScreen(navController: NavController) {
    var currentCatIndex by remember { mutableIntStateOf(0) }
    val likedCats = remember { mutableListOf<String>() }
    
    var offsetX by remember(currentCatIndex) { mutableFloatStateOf(0f) }
    var rotation by remember(currentCatIndex) { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var showGemDialog by remember { mutableStateOf(false) }
    var gemCount by remember { mutableIntStateOf(GemManager.gemCount) }
    
    val scope = rememberCoroutineScope()
    
    val cats = listOf(
        CatData("Luna", "Persian Cat", "2 years", "Gentle and loving"),
        CatData("Whiskers", "Maine Coon", "1 year", "Playful and fluffy"),
        CatData("Bella", "Siamese Cat", "3 years", "Vocal and social"),
        CatData("Mittens", "British Shorthair", "1 year", "Calm and cuddly"),
        CatData("Shadow", "Russian Blue", "2 years", "Quiet and elegant"),
        CatData("Oliver", "Ragdoll", "1 year", "Docile and affectionate"),
        CatData("Cleo", "Egyptian Mau", "2 years", "Active and intelligent"),
        CatData("Smokey", "Chartreux", "3 years", "Gentle giant")
    )

    fun swipeCard(direction: Float) {
        if (isDragging || currentCatIndex >= cats.size) return
        
        if (direction > 0) {
            // Swiping right (like) - consume a gem
            if (GemManager.consumeGem()) {
                likedCats.add(cats[currentCatIndex].name)
                gemCount = GemManager.gemCount // Update local count
            } else {
                // No gems left, show purchase dialog
                showGemDialog = true
                return
            }
        }
        
        val nextIndex = currentCatIndex + 1
        
        if (nextIndex < cats.size) {
            isDragging = true
            scope.launch {
                val targetX = if (direction > 0) 1000f else -1000f
                
                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(200)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 20f)
                }
                
                currentCatIndex = nextIndex
                isDragging = false
            }
        } else {
            scope.launch {
                val targetX = if (direction > 0) 1000f else -1000f
                
                animate(
                    initialValue = offsetX,
                    targetValue = targetX,
                    animationSpec = tween(200)
                ) { value, _ ->
                    offsetX = value
                    rotation = (value / 20f)
                }
                
                currentCatIndex = cats.size
            }
        }
    }
    
    fun resetCardPosition() {
        if (isDragging || currentCatIndex >= cats.size) return
        
        scope.launch {
            animate(
                initialValue = offsetX,
                targetValue = 0f,
                animationSpec = tween(150)
            ) { value, _ ->
                offsetX = value
                rotation = value / 50f
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        if (currentCatIndex >= cats.size) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🐱 All Cats Viewed!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "You liked ${likedCats.size} cats",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                if (likedCats.isNotEmpty()) {
    Text(
                        text = "Your cat matches:",
                        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    likedCats.forEach { catName ->
                        Text(
                            text = "❤️ $catName",
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
                                popUpTo("pet_selection") { inclusive = false }
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
                        currentCatIndex = 0
                        likedCats.clear()
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
                        text = "Swipe Cats Again",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
                    .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                // Welcome text with progress
                Text(
                    text = "Find Your Perfect Cat!",
                    fontSize = 24.sp,
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 40.dp, bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentCatIndex + 1} of ${cats.size}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    // Gem Counter
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
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Cat Cards Stack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                        .height(500.dp),
            contentAlignment = Alignment.Center
        ) {
                    // Show next card behind current one
                    if (currentCatIndex + 1 < cats.size) {
                        SwipeableCatCard(
                            cat = cats[currentCatIndex + 1],
                            offsetX = 0f,
                            rotation = 0f,
                            onDrag = { },
                            onDragEnd = { },
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(0.95f)
                        )
                    }
                    
                    // Current card
                    SwipeableCatCard(
                        cat = cats[currentCatIndex],
                        offsetX = offsetX,
                        rotation = rotation,
                        onDrag = { deltaX ->
                            if (!isDragging) {
                                offsetX = (offsetX + deltaX).coerceIn(-600f, 600f)
                                rotation = (offsetX / 50f)
                            }
                        },
                        onDragEnd = {
                            if (!isDragging) {
                                if (abs(offsetX) > 150f) {
                                    // Swipe right = like, swipe left = pass
                                    swipeCard(if (offsetX > 0) 1f else -1f)
            } else {
                                    // Reset position if not swiped far enough
                                    resetCardPosition()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Action Buttons Row (X and Heart)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // X Button (Reject)
                    FloatingActionButton(
                        onClick = { 
                            if (!isDragging) {
                                swipeCard(-1f)
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        containerColor = Color.White,
                        contentColor = Color.Red,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pass",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Heart Button (Like)
                    FloatingActionButton(
                        onClick = { 
                            if (!isDragging) {
                                if (GemManager.gemCount > 0) {
                                    swipeCard(1f)
                                } else {
                                    showGemDialog = true
                                }
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        containerColor = DarkBrown,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // ADOPT Button
                Button(
                    onClick = {
                        if (!isDragging) {
                            likedCats.add(cats[currentCatIndex].name)
                            try {
                                navController.navigate("adopter_home") {
                                    popUpTo("pet_selection") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBrown,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "ADOPT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Gem Purchase Dialog
        if (showGemDialog) {
            GemPurchaseDialog(
                onDismiss = { showGemDialog = false },
                onPurchase = { packageType ->
                    GemManager.purchaseGems(packageType)
                    gemCount = GemManager.gemCount // Update local count
                    showGemDialog = false
                }
            )
        }
    }
}

@Composable
fun SwipeableCatCard(
    cat: CatData,
    offsetX: Float,
    rotation: Float,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(cat.name) {
                detectHorizontalDragGestures(
                    onDragEnd = { onDragEnd() }
                ) { _, dragAmount ->
                    onDrag(dragAmount)
                }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cat Image
            Image(
                painter = painterResource(id = R.drawable.cat_selection),
                contentDescription = cat.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Swipe indicators
            if (offsetX > 50f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color.Green.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "LIKE",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (offsetX < -50f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color.Red.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PASS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Cat Info at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = cat.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = cat.age,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = cat.description,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = cat.breed,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GemPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (GemPackage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "💎 Out of Gems!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown
            )
        },
        text = {
            Column {
                Text(
                    text = "You need gems to like pets!",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Choose a gem package:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                GemPackage.entries.forEach { packageType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${packageType.gemAmount} gems",
                            fontSize = 14.sp
                        )
                        Text(
                            text = packageType.price,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPurchase(GemPackage.SMALL) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)
            ) {
                Text("Buy 5 Gems")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun CatSwipeScreenPreview() {
    val mockNavController = rememberNavController()
    CatSwipeScreen(navController = mockNavController)
}