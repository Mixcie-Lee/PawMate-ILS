package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import com.example.pawmate_ils.ui.components.PawMateSectionTitle

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
        containerColor = backgroundColor,
        bottomBar = {
            AdopterBottomBar(navController = navController, selectedTab = "Education")
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
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        PawMateSectionTitle(
                            title = "Education",
                            subtitle = "Pet care tips and guides",
                            color = Color(0xFFFF9999)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Learn something pawsome today \u2728",
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.65f)
                        )
                    }
                    IconButton(
                        onClick = { showTutorial = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFFFF9999).copy(alpha = if (isDarkMode) 0.18f else 0.14f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Show Tutorial",
                            tint = Color(0xFFFF9999),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            item {
                EducationSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isDarkMode = isDarkMode,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            label = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            if (filteredArticles.isEmpty()) {
                item {
                    EducationEmptyState(
                        textColor = textColor,
                        onClearFilters = {
                            searchQuery = ""
                            selectedCategory = "All"
                        }
                    )
                }
            } else {
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
    val accent = Color(0xFFFFB6C1)
    val borderColor = Color(0xFFFF9999).copy(alpha = if (isDarkMode) 0.35f else 0.25f)
    val containerColor = when {
        isSelected -> accent
        isDarkMode -> Color(0xFF2F2F33)
        else -> Color.White
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) null else BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = when {
                    isSelected -> Color.White
                    isDarkMode -> Color.White.copy(alpha = 0.78f)
                    else -> Color(0xFF6B6B6B)
                },
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
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
    val tileColor = categoryTileColor(article.category, isDarkMode)
    val badgeColor = categoryBadgeColor(article.category)
    val emoji = categoryEmoji(article.category)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(tileColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            badgeColor.copy(alpha = if (isDarkMode) 0.28f else 0.18f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Text(
                    text = article.description,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.LightGray else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (isDarkMode) Color.LightGray else Color.Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = article.readTime,
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color.LightGray else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFFF9999),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(22.dp)
            )
        }
    }
}

@Composable
private fun EducationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isDarkMode: Boolean,
    cardColor: Color,
    textColor: Color
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search articles, tips, breeds\u2026",
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFFFF9999)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = if (isDarkMode) Color.LightGray else Color.Gray
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFB6C1),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            cursorColor = Color(0xFFFF9999),
            focusedContainerColor = cardColor,
            unfocusedContainerColor = cardColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}

@Composable
private fun EducationEmptyState(
    textColor: Color,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "\uD83D\uDC3E", fontSize = 56.sp)
        Text(
            text = "No articles match your search",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        Text(
            text = "Try a different keyword or category.",
            fontSize = 13.sp,
            color = textColor.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
            onClick = onClearFilters,
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF9999))
        ) {
            Text("Clear filters", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun categoryEmoji(category: String): String = when (category) {
    "Dog Care" -> "\uD83D\uDC15"
    "Cat Care" -> "\uD83D\uDC08"
    "Health" -> "\uD83E\uDE7A"
    "Training" -> "\uD83C\uDF93"
    "Nutrition" -> "\uD83E\uDD63"
    else -> "\uD83E\uDDB4"
}

private fun categoryTileColor(category: String, isDarkMode: Boolean): Color {
    val base = when (category) {
        "Dog Care" -> Color(0xFFFFE2D1)
        "Cat Care" -> Color(0xFFE9DCFB)
        "Health" -> Color(0xFFD6F1E0)
        "Training" -> Color(0xFFD7ECFB)
        "Nutrition" -> Color(0xFFFFF1C9)
        else -> Color(0xFFFFD6E0)
    }
    return if (isDarkMode) base.copy(alpha = 0.18f) else base
}

private fun categoryBadgeColor(category: String): Color = when (category) {
    "Dog Care" -> Color(0xFFE08A4D)
    "Cat Care" -> Color(0xFF8A6FC7)
    "Health" -> Color(0xFF3FA46A)
    "Training" -> Color(0xFF4A90D9)
    "Nutrition" -> Color(0xFFC79A2E)
    else -> Color(0xFFFF7A95)
}

