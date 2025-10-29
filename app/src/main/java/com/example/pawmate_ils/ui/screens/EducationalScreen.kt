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
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
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
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Pets, 
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
                    onClick = { navController.navigate("adopter_home") },
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
                            contentDescription = "Education",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFFF9999))
                        )
                    },
                    label = { Text("Education", color = Color(0xFFFF9999), fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { },
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
                    onClick = { navController.navigate("profile_settings") },
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
                
                Text(
                    text = "Education",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9999),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
            
            
            items(filteredArticles) { article ->
                ArticleCard(
                    article = article,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = {
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFB6C1) else if (ThemeManager.isDarkMode) Color(0xFF3A3A3A) else Color.LightGray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    color = Color.Black,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = article.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFD6E0)),
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

