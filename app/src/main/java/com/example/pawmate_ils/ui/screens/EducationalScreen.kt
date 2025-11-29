package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.theme.DarkBrown

data class EducationalArticle(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val imageRes: Int,
    val readTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationalScreen(navController: NavController) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val tutorialPrefs = remember(context) {
        context.getSharedPreferences(
            "educational_tutorial",
            android.content.Context.MODE_PRIVATE
        )
    }
    val tutorialSeen = remember { tutorialPrefs.getBoolean("seen", false) }
    var showTutorial by remember { mutableStateOf(!tutorialSeen) }

    val categories = listOf("All", "Dog Care", "Cat Care", "Health", "Training", "Nutrition")

    val articles = listOf(
        EducationalArticle(
            id = 1,
            title = "Complete Guide to Dog Nutrition",
            description = "Learn about the essential nutrients your dog needs for a healthy life",
            category = "Dog Care",
            imageRes = R.drawable.dog1,
            readTime = "5 min read"
        ),
        EducationalArticle(
            id = 2,
            title = "Understanding Cat Behavior",
            description = "Decode your cat's body language and vocalizations",
            category = "Cat Care",
            imageRes = R.drawable.cat1,
            readTime = "4 min read"
        ),
        EducationalArticle(
            id = 3,
            title = "First Aid for Pets",
            description = "Essential emergency care tips every pet owner should know",
            category = "Health",
            imageRes = R.drawable.dog1,
            readTime = "8 min read"
        ),
        EducationalArticle(
            id = 4,
            title = "Puppy Training Basics",
            description = "Step-by-step guide to training your new puppy",
            category = "Training",
            imageRes = R.drawable.shitzu,
            readTime = "6 min read"
        ),
        EducationalArticle(
            id = 5,
            title = "Creating a Balanced Pet Diet",
            description = "How to ensure your pet gets all the nutrients they need",
            category = "Nutrition",
            imageRes = R.drawable.cat2,
            readTime = "5 min read"
        ),
        EducationalArticle(
            id = 6,
            title = "Common Pet Health Issues",
            description = "Recognizing and preventing common health problems",
            category = "Health",
            imageRes = R.drawable.chow,
            readTime = "7 min read"
        )
    )

    val filteredArticles = articles.filter { article ->
        (selectedCategory == "All" || article.category == selectedCategory) &&
                (searchQuery.isEmpty() || article.title.contains(searchQuery, ignoreCase = true) ||
                        article.description.contains(searchQuery, ignoreCase = true))
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
                            Icons.Filled.Pets,
                            contentDescription = "Swipe",
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    },
                    label = { Text("Swipe", color = Color.Gray.copy(alpha = 0.6f)) },
                    selected = false,
                    onClick = { navController.navigate("pet_swipe") },
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
                                Color(0xFFFF9999)
                            )
                        )
                    },
                    label = { Text("Learn", color = Color(0xFFFF9999), fontWeight = FontWeight.Bold) },
                    selected = true,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Education",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9999),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { showTutorial = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Show Tutorial",
                            tint = Color(0xFFFF9999),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }


            items(filteredArticles) { article ->
                ArticleCard(
                    article = article,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = {
                        navController.navigate("educational_detail/${article.id}")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showTutorial) {
            var tutorialStep by rememberSaveable { mutableStateOf(0) }

            AlertDialog(
                onDismissRequest = { 
                    tutorialPrefs.edit().putBoolean("seen", true).apply()
                    showTutorial = false 
                },
                containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
                title = {
                    Text(
                        text = when (tutorialStep) {
                            0 -> "Welcome to Education"
                            1 -> "Explore Articles"
                            else -> "Welcome to Education"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9999)
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
                                    0 -> R.drawable.educationaltuto1
                                    1 -> R.drawable.eductionaltuto2
                                    else -> R.drawable.educationaltuto1
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
                        if (tutorialStep < 1) {
                            TextButton(
                                onClick = { tutorialStep++ },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFFF9999)
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
                                Text("Get Started") 
                            }
                        }
                    }
                },
                dismissButton = {
                    if (tutorialStep > 0) {
                        TextButton(
                            onClick = { tutorialStep-- },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFFF9999)
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
                                contentColor = Color(0xFFFF9999)
                            )
                        ) { 
                            Text("Close") 
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFFFFB6C1) 
            else if (isDarkMode) 
                Color(0xFF3A3A3A) 
            else 
                Color.LightGray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        )
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else if (isDarkMode) Color.LightGray else Color.Gray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ArticleCard(
    article: EducationalArticle,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = article.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.description,
                    fontSize = 13.sp,
                    color = if (isDarkMode) Color.LightGray else Color.Gray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDarkMode) Color(0xFFFF9999).copy(alpha = 0.2f) else Color(0xFFFFD6E0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦴",
                    fontSize = 40.sp
                )
            }
        }
    }
}

