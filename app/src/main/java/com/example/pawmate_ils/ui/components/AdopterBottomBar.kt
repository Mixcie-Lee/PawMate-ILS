package com.example.pawmate_ils.ui.components

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.ThemeManager

@Composable
fun AdopterBottomBar(
    navController: NavController,
    selectedTab: String,
    modifier: Modifier = Modifier
) {
    val isDarkMode = ThemeManager.isDarkMode
    val barColor = if (isDarkMode) Color(0xFF26262B).copy(alpha = 0.96f) else Color(0xFFF2EFF2).copy(alpha = 0.98f)
    val accent = Color(0xFFC16565)

    val selectedRoute = when (selectedTab) {
        "Home" -> "pet_swipe"
        "Favorites" -> "adopter_home"
        "Education" -> "educational"
        "Shop" -> "shop"
        "Message" -> "chat_home"
        "Profile" -> "profile_settings"
        else -> ""
    }
    val lastNavAt = remember { mutableLongStateOf(0L) }

    fun navigate(route: String) {
        if (route == selectedRoute) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavAt.longValue < 500L) return
        lastNavAt.longValue = now
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = barColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Education", selectedTab == "Education", accent, { navigate("educational") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Education",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
                            unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
                        )
                    }
                }
                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Favorites", selectedTab == "Favorites", accent, { navigate("adopter_home") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Favorites",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.Default.Favorite,
                            unselectedIcon = Icons.Outlined.FavoriteBorder
                        )
                    }
                }

                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Home", selectedTab == "Home", accent, { navigate("pet_swipe") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Home",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.Default.Home,
                            unselectedIcon = Icons.Outlined.Home
                        )
                    }
                }

                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Message", selectedTab == "Message", accent, { navigate("chat_home") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Message",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.Default.ChatBubble,
                            unselectedIcon = Icons.Outlined.ChatBubbleOutline
                        )
                    }
                }

                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Shop", selectedTab == "Shop", accent, { navigate("shop") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Shop",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.Default.ShoppingCart,
                            unselectedIcon = Icons.Outlined.ShoppingCart
                        )
                    }
                }
                Box(modifier = Modifier.width(58.dp), contentAlignment = Alignment.BottomCenter) {
                    AdopterBottomBarItem("Profile", selectedTab == "Profile", accent, { navigate("profile_settings") }) { tint, selected ->
                        BottomBarIcon(
                            contentDescription = "Profile",
                            tint = tint,
                            selected = selected,
                            selectedIcon = Icons.Default.Person,
                            unselectedIcon = Icons.Outlined.Person
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdopterBottomBarItem(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    icon: @Composable (Color, Boolean) -> Unit
) {
    val yOffset by animateDpAsState(
        targetValue = if (selected) (-4).dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "adopter_nav_lift"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) accent else Color.Gray.copy(alpha = 0.72f),
        animationSpec = tween(220),
        label = "adopter_nav_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = yOffset)
            .width(58.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (selected) accent else Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 8.dp else 0.dp)
        ) {
            IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                icon(if (selected) Color.White else tint, selected)
            }
        }
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(42.dp)
                .height(3.dp)
                .background(
                    color = if (selected) accent else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
private fun BottomBarIcon(
    contentDescription: String,
    tint: Color,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    modifier: Modifier = Modifier.size(22.dp)
) {
    // Always use Material icons so outlined vs filled states tint reliably on all devices.
    Icon(
        imageVector = if (selected) selectedIcon else unselectedIcon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
