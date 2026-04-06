package com.example.pawmate_ils.Firebase_Utils

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.CloudinaryHelper
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.firebase_models.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    private val db = FirebaseFirestore.getInstance()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel = _currentChannel.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping = _isPartnerTyping.asStateFlow()

    // 🟢 Debounce Logic: Tracks the timer for "Stop Typing"
    private var typingJob: Job? = null
    private var lastTypingState = false

    fun sendMessage(
        channelId: String,
        messageText: String,
        receiverId: String,
        replyMessage: com.example.pawmate_ils.firebase_models.Message? = null,
        replyName: String? = null
    ) {
        val currentUser = authViewModel.currentUser ?: return

        val message = Message(
            messageId = "",
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "User",
            receiverId = receiverId,
            messageText = messageText,
            imageUrl = null,
            createdAt = System.currentTimeMillis(),


            replyToText = replyMessage?.messageText,
            replyToMessageId = replyMessage?.messageId,
            replyToSenderName = replyName ?: replyMessage?.senderName,
            replyToSenderImage = replyMessage?.senderImage
        )

        db.collection("channels")
            .document(channelId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                val updates = mapOf(
                    "lastMessage" to messageText,
                    "timestamp" to System.currentTimeMillis(),
                    "unreadCount" to com.google.firebase.firestore.FieldValue.increment(1),
                    "lastSenderId" to currentUser.uid
                )
                db.collection("channels").document(channelId).update(updates)
                // 🟢 Stop typing immediately after sending
                setTypingStatus(channelId, false, immediate = true)
                Log.d("ChatViewModel", "✅ Message sent and Channel updated")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "❌ Firestore send failed: ${e.message}")
            }
    }

    fun listenForMessages(channelId: String) {
        db.collection("channels")
            .document(channelId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "❌ Firestore Listen Error: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()
                _messages.value = list
            }
    }

    fun getChannelInfo(channelId: String, onResult: (Channel) -> Unit) {
        db.collection("channels").document(channelId).get()
            .addOnSuccessListener { doc ->
                doc.toObject(Channel::class.java)?.let { channel ->
                    _currentChannel.value = channel
                    onResult(channel)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "❌ Error fetching channel info: ${e.message}")
            }
    }

    fun updateChannelNamesFromFirestore(channelId: String) {
        viewModelScope.launch {
            try {
                val channelDoc = db.collection("channels").document(channelId).get().await()
                val channel = channelDoc.toObject(Channel::class.java) ?: return@launch
                val adopterDoc = db.collection("users").document(channel.adopterId).get().await()
                val shelterDoc = db.collection("users").document(channel.shelterId).get().await()
                val adopterName = adopterDoc.getString("name") ?: "Unknown"
                val shelterName = shelterDoc.getString("name") ?: "Unknown"

                db.collection("channels").document(channelId).update(
                    mapOf("adopterName" to adopterName, "shelterName" to shelterName)
                ).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "🔥 Error syncing names: ${e.message}")
            }
        }
    }

    fun sendImageMessage(channelId: String, imageUri: Uri, receiverId: String) {
        val currentUserId = authViewModel.currentUser?.uid ?: return
        val currentUserName = authViewModel.currentUser?.displayName ?: "User"

        CloudinaryHelper.uploadImage(imageUri) { url ->
            if (url != null) {
                val message = Message(
                    messageId = "",
                    senderId = currentUserId,
                    senderName = currentUserName,
                    receiverId = receiverId,
                    messageText = "",
                    imageUrl = url,
                    createdAt = System.currentTimeMillis()
                )
                db.collection("channels").document(channelId)
                    .collection("messages").add(message)
                    .addOnSuccessListener {
                        val updates = mapOf(
                            "lastMessage" to "Sent a photo 📷",
                            "timestamp" to System.currentTimeMillis(),
                            "lastSenderId" to currentUserId,
                            "unreadCount" to com.google.firebase.firestore.FieldValue.increment(1)
                        )
                        db.collection("channels").document(channelId).update(updates)
                    }
            }
        }
    }

    // 🟢 OPTIMIZED: Debounced Typing Status
    fun setTypingStatus(channelId: String, isTyping: Boolean, immediate: Boolean = false) {
        val currentUserId = authViewModel.currentUser?.uid ?: return
        val typingField = if (authViewModel.currentUserRole.value == "shelter") "shelterTyping" else "adopterTyping"

        if (immediate) {
            typingJob?.cancel()
            db.collection("channels").document(channelId).update(typingField, isTyping)
            lastTypingState = isTyping
            return
        }

        // Only update if the state is actually changing to avoid spam
        if (isTyping && !lastTypingState) {
            db.collection("channels").document(channelId).update(typingField, true)
            lastTypingState = true
        }

        // Reset the timer every time the user types
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2500) // Wait for 2.5 seconds of silence
            db.collection("channels").document(channelId).update(typingField, false)
            lastTypingState = false
        }
    }

    fun listenForTyping(channelId: String) {
        db.collection("channels").document(channelId)
            .addSnapshotListener { snapshot, _ ->
                val userRole = authViewModel.currentUserRole.value
                val partnerTypingField = if (userRole == "shelter") "adopterTyping" else "shelterTyping"
                _isPartnerTyping.value = snapshot?.getBoolean(partnerTypingField) ?: false
            }
    }
}