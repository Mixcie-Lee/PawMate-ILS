package com.example.pawmate_ils.chatScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("SimpleDateFormat")
private fun formatChatTimestamp(timestampMs: Long): Pair<String, Boolean> {
    if (timestampMs <= 0L) return "" to false
    val now = System.currentTimeMillis()
    val diff = now - timestampMs
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "Just now" to true
        diff < hour -> "${diff / minute}m ago" to true
        diff < day -> {
            val calNow = Calendar.getInstance()
            val calMsg = Calendar.getInstance().apply { timeInMillis = timestampMs }
            val sameDay = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
                    calNow.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)
            if (sameDay) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMs)) to false
            } else {
                "Yesterday" to false
            }
        }
        diff < 7 * day ->
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestampMs)) to false
        else ->
            SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestampMs)) to false
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    @Suppress("UNUSED_PARAMETER") chatViewModel: ChatViewModel
) {
    val homeViewModel: HomeViewModel = viewModel()
    val channels by homeViewModel.channels.collectAsState()
    val currentUserRole by authViewModel.currentUserRole.collectAsState()
    val userData by authViewModel.userData.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val isDarkMode = ThemeManager.isDarkMode
    val pageBg = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val pink = Color(0xFFE84D7A)
    val surface = if (isDarkMode) Color(0xFF262224) else Color.White
    val onSurface = if (isDarkMode) Color(0xFFF8F0F3) else Color(0xFF2C181C)
    val muted = if (isDarkMode) Color(0xFFB0A8AB) else Color(0xFF7A6F73)
    val searchFill = if (isDarkMode) Color(0xFF332A2D) else Color(0xFFF5E8EC)

    LaunchedEffect(Unit) {
        authViewModel.fetchUserRole()
        homeViewModel.listenToChannels()
    }

    val filteredChannels = remember(channels, searchQuery, currentUserRole) {
        val q = searchQuery.trim().lowercase(Locale.US)
        channels.filter { ch ->
            if (q.isEmpty()) return@filter true

            // Match the fallback logic used in the LazyColumn
            val currentShelterName = if (ch.shelterName.isNullOrBlank()) {
                "Shelter ${ch.shelterId.takeLast(4)}"
            } else {
                ch.shelterName
            }

            val title = if (currentUserRole == "shelter") ch.adopterName ?: "Adopter" else currentShelterName
            val petNamesString = ch.petNames.joinToString(" ").lowercase(Locale.US)

            title.lowercase().contains(q) ||
                    petNamesString.contains(q) ||
                    ch.lastMessage.lowercase(Locale.US).contains(q)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = pageBg,
        bottomBar = {
            AdopterBottomBar(
                navController = navController,
                selectedTab = "Message"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(paddingValues)
        ) {
            MessagesHeroHeader(
                navController = navController,
                userPhotoUri = userData?.photoUri?.takeIf { it.isNotBlank() }
                    ?: authViewModel.currentUser?.photoUrl?.toString(),
                title = "Messages",
                subtitle = "Your conversations",
                accent = pink
            )

            MessagesSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                fill = searchFill,
                accent = pink,
                onSurface = onSurface,
                muted = muted
            )

            when {
                currentUserRole.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = pink)
                    }
                }

                filteredChannels.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (channels.isEmpty()) {
                                "No conversations yet. Start swiping to match!"
                            } else {
                                "No chats match your search."
                            },
                            color = muted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredChannels, key = { it.channelId }) { channel ->
                            val isShelterView = currentUserRole == "shelter"
                            val currentUserId = authViewModel.currentUser?.uid ?: ""
                            val shouldShowBadge =
                                channel.unreadCount > 0 && channel.lastSenderId != currentUserId
                            val isLive = authViewModel.isUserActuallyOnline(
                                User(isOnline = channel.isOnline, lastActive = channel.lastActive)
                            )

                            // 🎯 NEWLY ADDED: Create display text from the list of pet names
                            val petsDisplay = channel.petNames.joinToString(", ")

                            val mainTitle = if (isShelterView) {
                                channel.adopterName ?: "Adopter"
                            } else {
                                // 🆕 FIX: Specifically check for empty strings ("") or null
                                if (channel.shelterName.isNullOrBlank()) {
                                    // If the name is missing, try to use a snippet of the ID so it's at least unique
                                    "Shelter ${channel.shelterId.takeLast(4)}"
                                } else {
                                    channel.shelterName
                                }
                            }
                            val displayPhoto =
                                if (isShelterView) channel.adopterPhotoUri else channel.shelterPhotoUri
                            val defaultSubtitle =
                                if (isShelterView) "Interested in $petsDisplay"
                                else "About $petsDisplay"
                            val preview =
                                if (channel.lastMessage.isNotEmpty()) channel.lastMessage else defaultSubtitle
                            val (timeLabel, timeRecent) = formatChatTimestamp(channel.timestamp)

                            ConversationRow(
                                displayName = mainTitle,
                                photoUri = displayPhoto,
                                preview = preview,
                                timeLabel = timeLabel,
                                timeRecentAccent = timeRecent,
                                unreadCount = if (shouldShowBadge) channel.unreadCount else 0,
                                isLive = isLive,
                                surface = surface,
                                onSurface = onSurface,
                                muted = muted,
                                accent = pink,
                                isDark = isDarkMode,
                                onAvatarClick = {
                                    val targetUserId =
                                        if (currentUserRole == "shelter") channel.adopterId else channel.shelterId
                                    if (!targetUserId.isNullOrEmpty()) {
                                        navController.navigate("profile_details/$targetUserId")
                                    }
                                },
                                onClick = {
                                    homeViewModel.resetUnreadCount(channel.channelId)
                                    navController.navigate("message/${channel.channelId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesHeroHeader(
    navController: NavController,
    userPhotoUri: String?,
    title: String,
    subtitle: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userPhotoUri ?: R.drawable.blackpawmateicon3,
            contentDescription = "Your profile",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f))
                .clickable { navController.navigate("profile_settings") },
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.blackpawmateicon3),
            error = painterResource(R.drawable.blackpawmateicon3)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (ThemeManager.isDarkMode) Color(0xFFF8F0F3) else Color(0xFF2C181C)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (ThemeManager.isDarkMode) Color(0xFFB0A8AB) else Color(0xFF8A7A80),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun MessagesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    fill: Color,
    accent: Color,
    onSurface: Color,
    muted: Color,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        placeholder = {
            Text("Search", color = muted, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = accent.copy(alpha = 0.85f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = fill,
            unfocusedContainerColor = fill,
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface,
            cursorColor = accent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* handled by filter */ })
    )
}

@Composable
private fun ConversationRow(
    displayName: String,
    photoUri: String?,
    preview: String,
    timeLabel: String,
    timeRecentAccent: Boolean,
    unreadCount: Int,
    isLive: Boolean,
    surface: Color,
    onSurface: Color,
    muted: Color,
    accent: Color,
    isDark: Boolean,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit,
) {
    val rowBg = surface.copy(alpha = if (isDark) 0.92f else 0.97f)
    val borderColor = accent.copy(alpha = if (isDark) 0.12f else 0.08f)
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clickable(
                    onClick = onAvatarClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            AsyncImage(
                model = photoUri ?: R.drawable.blackpawmateicon3,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(muted.copy(alpha = 0.12f)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.blackpawmateicon3),
                error = painterResource(R.drawable.blackpawmateicon3)
            )
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                fontSize = 13.sp,
                color = muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (timeLabel.isNotEmpty()) {
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (timeRecentAccent) accent else muted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.coerceAtMost(99).toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}