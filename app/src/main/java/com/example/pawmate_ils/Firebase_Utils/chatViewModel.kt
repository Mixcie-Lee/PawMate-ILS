// ChatViewModel.kt
package com.example.pawmate_ils.Firebase_Utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.firebase_models.Message
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.firebase_models.Channel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import java.util.UUID


class ChatViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    private val db = Firebase.database

    fun sendMessage(channelId: String, messageText: String, receiverId: String) {
        val currentUser = authViewModel.currentUser ?: return

        val messageId = db.reference.push().key ?: UUID.randomUUID().toString()
        val message = Message(
            messageId = messageId,
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "",
            receiverId = receiverId,
            messageText = messageText,
            createdAt = System.currentTimeMillis()
        )

        Log.d("ChatViewModel", "Sending message: $message to channelId: $channelId")

        db.getReference("message")
            .child(channelId)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "✅ Message sent successfully: $messageId")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "❌ Failed to send message: ${e.message}")
            }
    }

    fun listenForMessages(channelId: String) {
        Log.d("ChatViewModel", "Listening for messages on channel: $channelId")
        db.getReference("message")
            .child(channelId)
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach { data ->
                        data.getValue(Message::class.java)?.let { list.add(it) }
                    }
                    _messages.value = list
                    Log.d("ChatViewModel", "Messages updated for channel $channelId, total: ${list.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "❌ Error listening messages: ${error.message}")
                }
            })
    }

    fun getChannelInfo(channelId: String, onResult: (Channel) -> Unit) {
        Log.d("ChatViewModel", "Fetching channel info for: $channelId")
        FirebaseDatabase.getInstance().getReference("channel").child(channelId)
            .get()
            .addOnSuccessListener {
                val channel = it.getValue(Channel::class.java)
                Log.d("ChatViewModel", "Fetched channel info: $channel")
                if (channel != null) onResult(channel)
            }
    }

    fun updateChannelNamesFromFirestore(channelId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Updating channel names from Firestore for: $channelId")
                val firestore = FirebaseFirestore.getInstance()
                val channelRef = FirebaseDatabase.getInstance().getReference("channel/$channelId")
                val snapshot = channelRef.get().await()
                val channel = snapshot.getValue(Channel::class.java)
                if (channel == null) {
                    Log.w("ChatViewModel", "⚠️ Channel is null for id: $channelId")
                    return@launch
                }

                val shelterId = channel.shelterId.ifEmpty {
                    Log.w("ChatViewModel", "⚠️ shelterId missing, using current user as fallback")
                    authViewModel.currentUser?.uid ?: return@launch
                }

                Log.d("ChatViewModel", "Channel current state: $channel")

                val adopterDoc = firestore.collection("users").document(channel.adopterId).get().await()
                val shelterDoc = firestore.collection("users").document(shelterId).get().await()

                val adopterName = adopterDoc.getString("name") ?: "Unknown"
                val shelterName = shelterDoc.getString("name") ?: "Unknown"

                Log.d(
                    "ChatViewModel",
                    "Resolved names -> adopter: $adopterName, shelter: $shelterName for channelId: $channelId"
                )

                channelRef.child("adopterName").setValue(adopterName)
                channelRef.child("shelterName").setValue(shelterName)
                    .addOnSuccessListener {
                        Log.d("ChatViewModel", "✅ Channel names updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "❌ Failed to update channel names: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "🔥 Error updating names: ${e.message}")
            }
        }
    }

}


