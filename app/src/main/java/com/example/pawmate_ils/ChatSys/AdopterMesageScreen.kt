package com.example.pawmate_ils.ChatSys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pawmate_ils.Firebase_Utils.ChatRepository
import com.example.pawmate_ils.firebase_models.Message

@Composable
fun AdopterMessageScreen(
    chatRepo: ChatRepository,
    userId: String,
    shelterId: String
) {
    val messages by chatRepo.getMessages(userId, shelterId).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }

    // Mock ChatRepository (for preview only)

    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg -> Text("${msg.senderId}: ${msg.content}") }
        }
        Row {
            TextField(value = text, onValueChange = { text = it })
            Button(onClick = {
                if (text.isNotBlank()) {
                    chatRepo.sendMessage(
                        Message(senderId = userId, receiverId = shelterId, content = text)
                    )
                    text = ""
                }
            }) { Text("Send") }
        }
    }
}
