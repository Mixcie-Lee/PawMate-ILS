package com.example.pawmate_ils.Firebase_Utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.firebase_models.Message
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val authViewModel: AuthViewModel = AuthViewModel()
): ViewModel() {

    private val firebaseDatabase = Firebase.database

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannels()
    }

    private fun getChannels() {
        firebaseDatabase.getReference("channel").get().addOnSuccessListener {
            val list = mutableListOf<Channel>()
            it.children.forEach { data ->
                val channel = data.getValue(Channel::class.java)
                if (channel != null) list.add(channel)
            }
            _channels.value = list
        }
    }

    fun addChannel(channel: Channel) {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()

                // Fetch adopter and shelter names from Firestore
                val adopterDoc = firestore.collection("users").document(channel.adopterId).get().await()
                val shelterDoc = firestore.collection("users").document(channel.shelterId).get().await()

                val adopterName = adopterDoc.getString("name") ?: "Unknown"
                val shelterName = shelterDoc.getString("name") ?: "Unknown"

                val ref = firebaseDatabase.getReference("channel")
                val key = ref.push().key ?: return@launch

                val newChannel = channel.copy(
                    channelId = key,
                    adopterName = adopterName,
                    shelterName = shelterName
                )

                ref.child(key)
                    .setValue(newChannel)
                    .addOnSuccessListener {
                        Log.d("HomeViewModel", "✅ Channel added for ${newChannel.petName}")
                        getChannels()
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "❌ Failed to add channel: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "🔥 Error fetching names: ${e.message}")
            }
        }
    }


    fun listenToChannels() {
        val currentUserId = authViewModel.currentUser?.uid ?: return

        val channelsRef = firebaseDatabase.getReference("channel")
        val messagesRef = firebaseDatabase.getReference("message")

        channelsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Channel>()

                snapshot.children.forEach { data ->
                    val channel: Channel? = try {
                        data.getValue(Channel::class.java)
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Malformed channel node: ${data.key}")
                        null
                    }

                    if (channel != null && (channel.adopterId == currentUserId || channel.shelterId == currentUserId)) {
                        val channelId = data.key ?: return@forEach

                        // Listen to only the latest message in each channel
                        messagesRef.child(channelId)
                            .limitToLast(1)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(msgSnapshot: DataSnapshot) {
                                    var lastMessageText = ""
                                    var lastMessageTime = 0L
                                    var unreadCount = 0

                                    msgSnapshot.children.forEach { msg ->
                                        val message = msg.getValue(Message::class.java)
                                        if (message != null) {
                                            lastMessageText = message.messageText
                                            lastMessageTime = message.createdAt
                                        }
                                    }

                                    // Count unread messages (sent by others only)
                                    messagesRef.child(channelId)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(allMsgs: DataSnapshot) {
                                                unreadCount = allMsgs.children.count { msg ->
                                                    val message = msg.getValue(Message::class.java)
                                                    message != null && message.senderId != currentUserId
                                                }

                                                // Update the channel with new info
                                                val updatedChannel = channel.copy(
                                                    lastMessage = lastMessageText,
                                                    timestamp = lastMessageTime,
                                                    unreadCount = unreadCount
                                                )

                                                val existingIndex =
                                                    list.indexOfFirst { it.channelId == channel.channelId }
                                                if (existingIndex >= 0) {
                                                    list[existingIndex] = updatedChannel
                                                } else {
                                                    list.add(updatedChannel)
                                                }

                                                // Sort by latest message
                                                _channels.value =
                                                    list.sortedByDescending { it.timestamp }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.e(
                                                    "HomeViewModel",
                                                    "Unread count error: ${error.message}"
                                                )
                                            }
                                        })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "HomeViewModel",
                                        "Message listener error: ${error.message}"
                                    )
                                }
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Failed to load channels: ${error.message}")
            }
        })
    }

}







