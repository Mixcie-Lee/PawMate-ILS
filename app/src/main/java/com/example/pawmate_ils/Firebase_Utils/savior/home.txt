package com.example.pawmate_ils.Firebase_Utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val firebaseDatabase = Firebase.database

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            getChannels()
        }
    }

     fun getChannels() {
        val currentUserId = authViewModel.currentUser?.uid ?: return

        Log.d("DEBUG_HOME", "Fetching all channels from Firebase...")
        firebaseDatabase.getReference("channel").get().addOnSuccessListener {
            val list = mutableListOf<Channel>()
            it.children.forEach { data ->
                val channel = data.getValue(Channel::class.java)
                Log.d("DEBUG_HOME", "Found channel node: ${data.key} → $channel")
                if (channel != null && (channel.adopterId == currentUserId || channel.shelterId == currentUserId)) {
                    list.add(channel)
                }
            }
            _channels.value = list.sortedByDescending { it.timestamp }
            Log.d("DEBUG_HOME", "Loaded ${list.size} channels into state.")
        }
    }

    // --------------------------------------------------------------------------------------
    //  ADD CHANNEL + HEAVY LOGGING
    // --------------------------------------------------------------------------------------
    fun addChannel(channel: Channel) {
        viewModelScope.launch {
            try {

                Log.d("DEBUG_ADD_CHANNEL", "Requested new channel: $channel")

                val firestore = FirebaseFirestore.getInstance()
                val currentUserId = authViewModel.currentUser?.uid ?: ""

                Log.d("DEBUG_ADD_CHANNEL", "Current logged user: $currentUserId")

                if (currentUserId.isEmpty()) {
                    Log.e("DEBUG_ADD_CHANNEL", "ERROR: No signed-in user, aborting.")
                    return@launch
                }

                // Correct shelterId source
                val correctShelterId = if (channel.shelterId.isEmpty()) {
                    Log.w("DEBUG_ADD_CHANNEL", "Pet shelterId EMPTY → Using currentUser as fallback!")
                    currentUserId
                } else {
                    channel.shelterId
                }

                Log.d("DEBUG_ADD_CHANNEL", "Using shelterId: $correctShelterId (original: ${channel.shelterId})")

                // Fetch both names
                Log.d("DEBUG_ADD_CHANNEL", "Fetching adopter name from Firestore: ${channel.adopterId}")
                val adopterDoc = firestore.collection("users").document(channel.adopterId).get().await()

                Log.d("DEBUG_ADD_CHANNEL", "Fetching shelter name from Firestore: $correctShelterId")
                val shelterDoc = firestore.collection("users").document(correctShelterId).get().await()

                val adopterName = adopterDoc.getString("name") ?: "Unknown"
                val shelterName = shelterDoc.getString("name") ?: "Unknown"

                Log.d("DEBUG_ADD_CHANNEL", "Fetched adopterName = $adopterName")
                Log.d("DEBUG_ADD_CHANNEL", "Fetched shelterName = $shelterName")

                val ref = firebaseDatabase.getReference("channel")

                val newChannel = channel.copy(
                    adopterName = adopterName,
                    shelterName = shelterName,
                    shelterId = correctShelterId
                )

                Log.d("DEBUG_ADD_CHANNEL", "Fully prepared NEW CHANNEL: $newChannel")

                // Check for duplicates
                ref.get().addOnSuccessListener { snapshot ->
                    val exists = snapshot.children.any { data ->
                        val existing = data.getValue(Channel::class.java)

                        val match = existing?.adopterId == newChannel.adopterId &&
                                existing.shelterId == newChannel.shelterId &&
                                existing.petName == newChannel.petName

                        if (match) {
                            Log.d("DEBUG_ADD_CHANNEL", "MATCHING EXISTING CHANNEL: $existing")
                        }
                        match
                    }

                    if (exists) {
                        Log.w("DEBUG_ADD_CHANNEL", "Channel already exists → Skipping creation.")
                        return@addOnSuccessListener
                    }

                    Log.d("DEBUG_ADD_CHANNEL", "Creating NEW channel node at: ${newChannel.channelId}")

                    ref.child(newChannel.channelId)
                        .setValue(newChannel)
                        .addOnSuccessListener {
                            Log.d("DEBUG_ADD_CHANNEL", "SUCCESS: Channel created for ${newChannel.petName}")
                            getChannels()
                        }
                        .addOnFailureListener { e ->
                            Log.e("DEBUG_ADD_CHANNEL", "ERROR writing new channel: ${e.message}")
                        }
                }

            } catch (e: Exception) {
                Log.e("DEBUG_ADD_CHANNEL", "EXCEPTION creating channel: ${e.message}")
            }
        }
    }

    // --------------------------------------------------------------------------------------
    // LISTENER FOR CHANNEL UPDATES
    // --------------------------------------------------------------------------------------
    fun listenToChannels() {
        val currentUserId = authViewModel.currentUser?.uid ?: return
        Log.d("DEBUG_LISTENER", "Start listening for channels belonging to: $currentUserId")

        val channelsRef = firebaseDatabase.getReference("channel")
        val messagesRef = firebaseDatabase.getReference("message")

        _channels.value = emptyList()

        channelsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DEBUG_LISTENER", "Received channels update...")

                val currentList = mutableListOf<Channel>()

                snapshot.children.forEach { data ->
                    try {
                        val map = data.value as? Map<String, Any>
                        val channel = map?.let {
                            Channel(
                                channelId = it["channelId"] as? String ?: "",
                                adopterId = it["adopterId"] as? String ?: "",
                                adopterName = it["adopterName"] as? String ?: "",
                                shelterId = it["shelterId"] as? String ?: "",
                                shelterName = it["shelterName"] as? String ?: "",
                                petName = it["petName"] as? String ?: "",
                                lastMessage = it["lastMessage"] as? String ?: "",
                                timestamp = (it["timestamp"] as? Long)
                                    ?: System.currentTimeMillis(),
                                unreadCount = (it["unreadCount"] as? Long)?.toInt() ?: 0,
                                createdAt = (it["createdAt"] as? Long)
                                    ?: System.currentTimeMillis()
                            )
                        }

                        Log.d("DEBUG_LISTENER", "Parsed channel: $channel")

                        if (channel != null &&
                            (channel.adopterId == currentUserId || channel.shelterId == currentUserId)
                        ) {
                            Log.d("DEBUG_LISTENER", "User is part of channel: ${channel.channelId}")

                            val channelId = data.key ?: return@forEach

                            // Fetch last message
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

                                        // Now count unread
                                        messagesRef.child(channelId)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(allMsgs: DataSnapshot) {

                                                    unreadCount = allMsgs.children.count { msg ->
                                                        val message =
                                                            msg.getValue(Message::class.java)
                                                        message != null &&
                                                                message.senderId != currentUserId
                                                    }

                                                    val updatedChannel = channel.copy(
                                                        lastMessage = lastMessageText,
                                                        timestamp = lastMessageTime,
                                                        unreadCount = unreadCount
                                                    )

                                                    Log.d("DEBUG_LISTENER", "Updated channel unread + lastmsg: $updatedChannel")

                                                    val index = currentList.indexOfFirst { it.channelId == channel.channelId }
                                                    if (index >= 0) {
                                                        currentList[index] = updatedChannel
                                                    } else {
                                                        currentList.add(updatedChannel)
                                                    }

                                                    _channels.value =
                                                        currentList.sortedByDescending { it.timestamp }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.e(
                                                        "DEBUG_LISTENER",
                                                        "Unread count error: ${error.message}"
                                                    )
                                                }
                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("DEBUG_LISTENER", "Message listener error: ${error.message}")
                                    }
                                })
                        } else {
                            Log.d("DEBUG_LISTENER", "Skipping channel not related to user: ${channel?.channelId}")
                        }
                    } catch (e: Exception) {
                        Log.e("DEBUG_LISTENER", "Malformed channel node: ${data.key}", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DEBUG_LISTENER", "Channel listener error: ${error.message}")
            }
        })
    }

    // --------------------------------------------------------------------------------------
    fun resetUnreadCount(channelId: String) {
        val currentUserId = authViewModel.currentUser?.uid ?: return
        Log.d("DEBUG_UNREAD", "Reset unread for channel: $channelId")

        val messagesRef = firebaseDatabase.getReference("message").child(channelId)

        messagesRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { msg ->
                val message = msg.getValue(Message::class.java)
                if (message != null && message.senderId != currentUserId) {
                    msg.ref.child("read").setValue(true)
                }
            }

            val updatedList = _channels.value.map {
                if (it.channelId == channelId) it.copy(unreadCount = 0)
                else it
            }

            _channels.value = updatedList
        }
    }

    fun deleteChannel(channel: Channel) {
        Log.d("DEBUG_DELETE", "Deleting channel: ${channel.channelId}")
        val ref = firebaseDatabase.getReference("channel").child(channel.channelId)
        ref.removeValue().addOnSuccessListener {
            Log.d("DEBUG_DELETE", "Deleted successfully")
            _channels.value =
                _channels.value.filter { it.channelId != channel.channelId }
        }.addOnFailureListener { e ->
            Log.e("DEBUG_DELETE", "Delete failed: ${e.message}")
        }
    }
    fun clearChannels(){
        _channels.value = emptyList()
        Log.d("DEBUG_HOME", "Cleared all channels for new user/login")

    }
}
