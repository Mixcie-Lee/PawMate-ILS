package com.example.pawmate_ils.Firebase_Utils
import com.example.pawmate_ils.firebase_models.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {
    private val chatsRef = FirebaseFirestore.getInstance().collection("chats")

    fun sendMessage(message: Message) {
        val chatId = listOf(message.senderId, message.receiverId).sorted().joinToString("_")
        chatsRef.document(chatId).collection("messages").add(message)
    }

    fun getMessages(senderId: String, receiverId: String): Flow<List<Message>> = callbackFlow {
        val chatId = listOf(senderId, receiverId).sorted().joinToString("_")
        val subscription = chatsRef.document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }
}

