package com.example.pawmate_ils.chatScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.firebase_models.Message
import androidx.compose.material3.TextFieldDefaults
import com.example.pawmate_ils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    channelId: String,
) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)


    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)





    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val currentUserId = authViewModel.currentUser?.uid ?: ""

    // 🔥 Fetch channel info from Firebase
    var chatPartnerName by remember { mutableStateOf("") }
    var adopterId by remember { mutableStateOf("") }
    var shelterId by remember { mutableStateOf("") }

    LaunchedEffect(channelId) {
        authViewModel.fetchUserRole()
        chatViewModel.listenForMessages(channelId)
        chatViewModel.getChannelInfo(channelId) { channel ->
            adopterId = channel.adopterId
            shelterId = channel.shelterId
            val userRole = authViewModel.currentUserRole.value
            println("🔥 ChatScreen Debug → userRole: $userRole")
            println("🔥 adopterName: ${channel.adopterName}, shelterName: ${channel.shelterName}")

            if (channel.adopterName == "Unknown" || channel.shelterName == "Unknown") {
                chatViewModel.updateChannelNamesFromFirestore(channelId)
            }

            chatPartnerName = when (userRole) {
                "shelter" -> channel.adopterName.ifEmpty { "Adopter" }
                "adopter" -> channel.shelterName.ifEmpty { "Shelter" }
                else -> "Unknown User"
            }
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.background(backgroundColor),
                title = {
                    Text(
                        text = chatPartnerName  ,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD95C5C)
                )
            )
        },
        containerColor = Color.Transparent,
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.text.isNotBlank()) {
                        // ✅ Determine receiver based on who is logged in
                        val receiverId = if (currentUserId == adopterId) shelterId else adopterId
                        chatViewModel.sendMessage(channelId, messageText.text, receiverId)
                        messageText = TextFieldValue("")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF1F1), Color(0xFFFFE4E1))
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    ChatBubble(
                        message = message,
                        isUserMe = message.senderId == currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isUserMe: Boolean) {
    val bubbleColor = if (isUserMe) Color(0xFFD95C5C) else Color.White
    val textColor = if (isUserMe) Color.White else Color.Black
    val shape = if (isUserMe) {
        RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
        RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
    } else {
        RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(
                text = message.messageText,
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    val textFieldBg = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFFFF1F1)
    val textFieldTextColor = if (isDarkMode) Color.White else Color.Black
    val textFieldPlaceholder = if (isDarkMode) Color.LightGray else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(textFieldBg),
            placeholder = { Text(text = "Type a message...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = textFieldBg,
                unfocusedContainerColor = textFieldBg,
                disabledContainerColor = textFieldBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFD95C5C),
                focusedTextColor = textFieldTextColor,
                unfocusedTextColor = textFieldTextColor,
                focusedPlaceholderColor = textFieldPlaceholder,
                unfocusedPlaceholderColor = textFieldPlaceholder
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSendClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFD95C5C))
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}
