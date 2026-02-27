package com.example.pawmate_ils.Firebase_Utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.firebase_models.Channel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val authViewModel: AuthViewModel = AuthViewModel()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        listenToChannels()
    }

    // 🔹 LISTEN TO CHANNELS (Using Firestore Queries)
    fun listenToChannels() {
        val currentUserId = authViewModel.currentUser?.uid ?: return

        // We use two listeners because Firestore doesn't support
        // "OR" queries across different fields (adopterId OR shelterId) easily
        // without a complex index. This is the safest way for a demo.

        val adopterQuery = db.collection("channels").whereEqualTo("adopterId", currentUserId)
        val shelterQuery = db.collection("channels").whereEqualTo("shelterId", currentUserId)

        val handleUpdate = {
            // This function merges results from both queries
            // and updates the StateFlow
        }

        adopterQuery.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { it.toObject(Channel::class.java) } ?: emptyList()
            updateChannelList(list)
        }

        shelterQuery.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { it.toObject(Channel::class.java) } ?: emptyList()
            updateChannelList(list)
        }
    }

    private fun updateChannelList(newList: List<Channel>) {
        val currentList = _channels.value.toMutableList()
        newList.forEach { channel ->
            val index = currentList.indexOfFirst { it.channelId == channel.channelId }
            if (index != -1) currentList[index] = channel else currentList.add(channel)
        }
        _channels.value = currentList.sortedByDescending { it.timestamp }
    }

    // 🔹 ADD CHANNEL (Using Firestore Collections)
    fun addChannel(channel: Channel) {
        viewModelScope.launch {
            try {
                val adopterDoc = db.collection("users").document(channel.adopterId).get().await()
                val shelterDoc = db.collection("users").document(channel.shelterId).get().await()

                val newChannel = channel.copy(
                    adopterName = adopterDoc.getString("name") ?: "Adopter",
                    shelterName = shelterDoc.getString("name") ?: "Shelter",
                    createdAt = System.currentTimeMillis(),
                    timestamp = System.currentTimeMillis()
                )

                db.collection("channels").document(newChannel.channelId).set(newChannel).await()
                Log.d("HOME_VM", "✅ Channel added to Firestore")
            } catch (e: Exception) {
                Log.e("HOME_VM", "❌ Add failed: ${e.message}")
            }
        }
    }
    fun resetUnreadCount(channelId: String) {
        db.collection("channels").document(channelId)
            .update("unreadCount", 0)
    }

    fun deleteChannel(channel: Channel) {
        db.collection("channels").document(channel.channelId).delete()
    }

    fun clearChannels() {
        _channels.value = emptyList()
    }
}