package com.example.pawmate_ils.Firebase_Utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.firebase_models.Channel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
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

    // 🔹 Track results for both roles separately to allow proper merging/deletion
    private var adopterChannels = emptyList<Channel>()
    private var shelterChannels = emptyList<Channel>()

    private val _newNotificationAlert = MutableStateFlow<String?>(null)
    val newNotificationAlert = _newNotificationAlert.asStateFlow()

    init {
        viewModelScope.launch {
            delay(500) // Now it's inside a coroutine, so it works!
            listenToChannels()
            startNotificationListener()
        }
        // 🔔 ADD THIS LINE HERE
    }

    // 🔹 LISTEN TO CHANNELS
    fun listenToChannels() {
        val currentUserId = authViewModel.currentUser?.uid ?: return

        // Listener for Adopter role
        db.collection("channels")
            .whereEqualTo("adopterId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HOME_VM", "Adopter Listen Failed", error)
                    return@addSnapshotListener
                }
                adopterChannels = snapshot?.documents?.mapNotNull { it.toObject(Channel::class.java) } ?: emptyList()
                combineAndSort()
            }

        // Listener for Shelter role
        db.collection("channels")
            .whereEqualTo("shelterId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HOME_VM", "Shelter Listen Failed", error)
                    return@addSnapshotListener
                }
                shelterChannels = snapshot?.documents?.mapNotNull { it.toObject(Channel::class.java) } ?: emptyList()
                combineAndSort()
            }
    }

    // 🔹 MERGE AND SORT (Handles Deletions and Priority Real-time)
    private fun combineAndSort() {
        val combinedList = (adopterChannels + shelterChannels)
            .distinctBy { it.channelId }
            .sortedWith(
                compareByDescending<Channel> { it.isPriority } // 👑 VIPs at the top
                    .thenByDescending { it.timestamp }         // 🕒 Newest messages next
            )

        _channels.value = combinedList
        Log.d("HOME_VM", "🔄 UI Synced. Total: ${combinedList.size}")
    }

    // 🔹 ADD CHANNEL
    fun addChannel(channel: Channel) {
        viewModelScope.launch {
            try {
                val finalAdopterName = channel.adopterName.ifBlank {
                    db.collection("users").document(channel.adopterId).get().await().getString("name") ?: "Adopter"
                }
                if (channel.shelterId.isBlank() || channel.adopterId.isBlank()) {
                    Log.e("HOME_VM", "❌ Rejecting channel: Missing IDs (Shelter: ${channel.shelterId})")
                    return@launch
                }

                val finalShelterName = channel.shelterName.ifBlank {
                    db.collection("users").document(channel.shelterId).get().await().getString("name") ?: "Shelter"
                }
                val newChannel = channel.copy(
                    adopterName = finalAdopterName,
                    shelterName = finalShelterName,
                    adopterTier = channel.adopterTier,
                    isPriority = channel.isPriority,
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
        // Deleting from Firestore triggers the addSnapshotListener automatically
        db.collection("channels").document(channel.channelId).delete()
    }

    fun clearChannels() {
        _channels.value = emptyList()
    }
    // 🏷️ THIS IS FOR AD0PTERS WHO SWIPED FIRST BEFORE AVAILING TIER 3, a crown icon appears TO INDICATE USER IS OFFICIALLY TIER 3
    fun syncExistingChannelsToTier3() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                // 🔍 1. Find all channels this user has already created
                val userChannels = db.collection("channels")
                    .whereEqualTo("adopterId", uid)
                    .get()
                    .await()

                // ⚡ 2. Use a Batch to update them all at once
                db.runBatch { batch ->
                    for (document in userChannels.documents) {
                        val channelRef = db.collection("channels").document(document.id)
                        batch.update(channelRef, "adopterTier", 3)
                        batch.update(channelRef, "isPriority", true)
                    }
                }.await()

                Log.d("TIER_SYNC", "Successfully upgraded ${userChannels.size()} channels to VIP")
                listenToChannels()
            } catch (e: Exception) {
                Log.e("TIER_SYNC", "Failed to sync old channels: ${e.message}")
            }
        }
    }

    fun startNotificationListener() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(currentUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1) // We only care about the latest one
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val latestDoc = snapshot?.documents?.firstOrNull()
                if (latestDoc != null) {
                    val isRead = latestDoc.getBoolean("isRead") ?: true
                    if (!isRead) {
                        // 🔔 TRIGGER: This is where the magic happens!
                        val title = latestDoc.getString("title") ?: "New Update"
                        val message = latestDoc.getString("message") ?: ""

                        _newNotificationAlert.value = "$title: $message"

                        // Optional: Mark as read so it doesn't pop up again on next app start
                        latestDoc.reference.update("isRead", true)
                    }
                }
            }
    }

    fun clearNotificationAlert() {
        _newNotificationAlert.value = null
    }

}