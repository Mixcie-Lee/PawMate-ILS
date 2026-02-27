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

    // ✅ FIXED: Functions are now properly separated
    fun sendMessage(channelId: String, messageText: String, receiverId: String) {
        val currentUser = authViewModel.currentUser ?: return

        val message = Message(
            messageId = "",
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "User",
            receiverId = receiverId,
            messageText = messageText,
            imageUrl = null,
            createdAt = System.currentTimeMillis()
        )

        db.collection("channels")
            .document(channelId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // ✅ UPDATE CHANNEL: Update last message for Home Screen preview
                val updates = mapOf(
                    "lastMessage" to messageText,
                    "timestamp" to System.currentTimeMillis(),
                    "unreadCount" to com.google.firebase.firestore.FieldValue.increment(1),
                    "lastSenderId" to currentUser.uid
                )
                db.collection("channels").document(channelId).update(updates)
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
                doc.toObject(Channel::class.java)?.let { onResult(it) }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "❌ Error fetching channel info: ${e.message}")
            }
    }

    // ✅ FIXED: Now correctly updates FIRESTORE instead of Realtime Database
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
                    mapOf(
                        "adopterName" to adopterName,
                        "shelterName" to shelterName
                    )
                ).await()

                Log.d("ChatViewModel", "✅ Firestore Channel names synced")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "🔥 Error syncing names: ${e.message}")
            }
        }
    }
    fun sendImageMessage(channelId: String, imageUri: Uri, receiverId: String) {
        CloudinaryHelper.uploadImage(imageUri) { url ->
            if (url != null) {
                val message = Message(
                    senderId = authViewModel.currentUser?.uid ?: "",
                    receiverId = receiverId,
                    messageText = "", // Empty text for photo-only messages
                    imageUrl = url,
                    createdAt = System.currentTimeMillis()
                )
                // Add to your Firestore collection
                db.collection("channels").document(channelId)
                    .collection("messages").add(message)

                // Optional: Update lastMessage in channel to "Sent a photo"
            }
        }
    }

}