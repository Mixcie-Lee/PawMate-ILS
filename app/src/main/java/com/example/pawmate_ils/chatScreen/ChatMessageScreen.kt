package com.example.pawmate_ils.chatScreen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.firebase_models.Message
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.AdopShelDataStruc.DateUtils
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    channelId: String,
) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)

    val messages by chatViewModel.messages.collectAsState()
    val isPartnerTyping by chatViewModel.isPartnerTyping.collectAsState() // 🟢 Collect typing state
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val currentUserId = authViewModel.currentUser?.uid ?: ""

    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Header & Avatar State
    var chatPartnerName by remember { mutableStateOf("Loading...") }
    var chatPartnerPhoto by remember { mutableStateOf<String?>(null) }
    var chatPartnerRole by remember { mutableStateOf("") }
    var adopterId by remember { mutableStateOf("") }
    var shelterId by remember { mutableStateOf("") }

    var replyingTo by remember { mutableStateOf<Message?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { selectedUri ->
                val currentChannel = chatViewModel.currentChannel.value
                if (currentChannel != null) {
                    val receiverId = if (currentUserId == currentChannel.adopterId) {
                        currentChannel.shelterId
                    } else {
                        currentChannel.adopterId
                    }
                    chatViewModel.sendImageMessage(channelId, selectedUri, receiverId)
                } else {
                    Log.e("ChatScreen", "Cannot send image: Channel info is null")
                }
            }
        }
    )

    LaunchedEffect(channelId) {
        authViewModel.fetchUserRole()
        chatViewModel.listenForMessages(channelId)
        chatViewModel.listenForTyping(channelId) // 🟢 Start listening for partner's typing

        chatViewModel.getChannelInfo(channelId) { channel ->
            adopterId = channel.adopterId
            shelterId = channel.shelterId

            val userRole = authViewModel.currentUserRole.value

            if (channel.adopterName == "Unknown" || channel.shelterName == "Unknown") {
                chatViewModel.updateChannelNamesFromFirestore(channelId)
            }

            when (userRole) {
                "shelter" -> {
                    chatPartnerName = channel.adopterName.ifEmpty { "Adopter" }
                    chatPartnerPhoto = channel.adopterPhotoUri
                    chatPartnerRole = "Adopter"
                }
                "adopter" -> {
                    chatPartnerName = channel.shelterName.ifEmpty { "Shelter" }
                    chatPartnerPhoto = channel.shelterPhotoUri
                    chatPartnerRole = "Shelter"
                }
                else -> {
                    chatPartnerName = "Chat"
                    chatPartnerPhoto = null
                    chatPartnerRole = ""
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = chatPartnerPhoto ?: "https://via.placeholder.com/150",
                            contentDescription = "Partner Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chatPartnerName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (chatPartnerRole.isNotEmpty()) {
                                Text(
                                    text = chatPartnerRole,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD95C5C))
            )
        },
        bottomBar = {
            // All bottom elements must be wrapped in a single Column
            Column {
                // 🆕 Reply Preview Box (Appears above the input bar)
                AnimatedVisibility(
                    visible = replyingTo != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFFDEEF1))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vertical accent line
                        Box(
                            modifier = Modifier.width(3.dp).height(35.dp)
                                .background(Color(0xFFD95C5C))
                        )

                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                text = "Replying to ${if (replyingTo?.senderId == currentUserId) "yourself" else chatPartnerName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD95C5C)
                            )
                            Text(
                                text = replyingTo?.messageText ?: "Shared Image",
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = if (isDarkMode) Color.LightGray else Color.Gray
                            )
                        }

                        IconButton(onClick = { replyingTo = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Reply",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // ChatInputBar is now INSIDE the Column
                ChatInputBar(
                    messageText = messageText,
                    onMessageChange = {
                        messageText = it
                        chatViewModel.setTypingStatus(channelId, it.text.isNotEmpty())
                    },
                    onSendClick = {
                        if (messageText.text.isNotBlank()) {
                            val receiverId = if (currentUserId == adopterId) shelterId else adopterId

                            // 🆕 Resolve the correct name for the reply bubble
                            val resolvedReplyName = if (replyingTo != null) {
                                if (replyingTo?.senderId == currentUserId) {
                                    // Use your own name if replying to yourself
                                    authViewModel.userData.value?.name ?: "Me"
                                } else {
                                    // Use the partner's name from the header state
                                    chatPartnerName
                                }
                            } else null

                            // 🚀 Pass the resolvedReplyName to the ViewModel
                            chatViewModel.sendMessage(
                                channelId = channelId,
                                messageText = messageText.text,
                                receiverId = receiverId,
                                replyMessage = replyingTo,
                                replyName = resolvedReplyName
                            )

                            chatViewModel.setTypingStatus(channelId, false)
                            messageText = TextFieldValue("")
                            replyingTo = null
                        }
                    },
                    onGalleryClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp)
                ) {
                    items(messages) { message ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                // 🆕 Long-press detection starts here
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            // This fills our 'replyingTo' state with the message data
                                            replyingTo = message
                                        }
                                    )
                                }
                        ) {
                            ChatBubble(
                                message = message,
                                isUserMe = message.senderId == currentUserId,
                                partnerPhotoUri = chatPartnerPhoto,
                                onImageClick = { url -> selectedImageUrl = url }
                            )
                        }
                    }
                }
                // 🟢 MESSENGER STYLE TYPING INDICATOR
                AnimatedVisibility(
                    visible = isPartnerTyping,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$chatPartnerName is typing...",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.LightGray else Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // 🟢 FULL SCREEN ZOOM OVERLAY
            AnimatedVisibility(
                visible = selectedImageUrl != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { selectedImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedImageUrl,
                        contentDescription = "Zoomed Image",
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: Message,
    isUserMe: Boolean,
    partnerPhotoUri: String?,
    onImageClick: (String) -> Unit
) {
    val bubbleColor = if (isUserMe) Color(0xFFD95C5C) else Color.White
    val textColor = if (isUserMe) Color.White else Color.Black
    val shape = if (isUserMe) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUserMe) {
            AsyncImage(
                model = partnerPhotoUri ?: "https://via.placeholder.com/150",
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 4.dp)
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            horizontalAlignment = if (isUserMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = DateUtils.formatToPST(message.createdAt),
                fontSize = 10.sp,
                color = if (ThemeManager.isDarkMode) Color.LightGray else Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
            )
            if (!message.replyToText.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUserMe) Color.Black.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = message.replyToSenderName ?: "User",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserMe) Color.White.copy(alpha = 0.9f) else Color(0xFFD95C5C)
                        )
                        Text(
                            text = message.replyToText!!,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = if (isUserMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                }
            }

            if (!message.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = "Shared Image",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .fillMaxWidth(0.65f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                        .clickable { onImageClick(message.imageUrl!!) },
                    contentScale = ContentScale.Crop
                )
            }

            if (message.messageText.isNotEmpty()) {
                Box(
                    modifier = Modifier.clip(shape).background(bubbleColor).padding(12.dp)
                ) {
                    Text(text = message.messageText, color = textColor, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    val textFieldBg = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFFFF1F1)
    val textFieldTextColor = if (isDarkMode) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) Color(0xFF1E1E1E) else Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onGalleryClick) {
            Icon(Icons.Default.AddAPhoto, "Gallery", tint = Color(0xFFD95C5C))
        }

        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = { Text(text = "Type a message...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = textFieldBg,
                unfocusedContainerColor = textFieldBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFD95C5C),
                focusedTextColor = textFieldTextColor,
                unfocusedTextColor = textFieldTextColor
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
            Icon(Icons.Default.Send, "Send", tint = Color.White)
        }
    }
}