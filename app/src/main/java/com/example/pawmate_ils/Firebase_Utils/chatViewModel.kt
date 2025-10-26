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

        val message = Message(
            messageId = db.reference.push().key ?: UUID.randomUUID().toString(),
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "",
            receiverId = receiverId,
            messageText = messageText,
            createdAt = System.currentTimeMillis()
        )

        // Push to RTDB under /messages/{channelId}
        db.getReference("message")
            .child(channelId)
            .push()
            .setValue(message)
    }

    fun listenForMessages(channelId: String) {
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
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error: ${error.message}")
                }
            })
    }

    fun getChannelInfo(channelId: String, onResult: (Channel) -> Unit) {
        FirebaseDatabase.getInstance().getReference("channel").child(channelId)
            .get()
            .addOnSuccessListener {
                val channel = it.getValue(Channel::class.java)
                if (channel != null) onResult(channel)
            }
    }
    fun updateChannelNamesFromFirestore(channelId: String) {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val channelRef = FirebaseDatabase.getInstance().getReference("channel/$channelId")
                val snapshot = channelRef.get().await()
                val channel = snapshot.getValue(Channel::class.java) ?: return@launch

                val adopterDoc = firestore.collection("users").document(channel.adopterId).get().await()
                val shelterDoc = firestore.collection("users").document(channel.shelterId).get().await()

                val adopterName = adopterDoc.getString("name") ?: "Unknown"
                val shelterName = shelterDoc.getString("name") ?: "Unknown"

                channelRef.child("adopterName").setValue(adopterName)
                channelRef.child("shelterName").setValue(shelterName)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error updating names: ${e.message}")
            }
        }
    }

}

