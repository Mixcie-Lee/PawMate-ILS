package com.example.pawmate_ils.Firebase_Utils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val Id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
        )
class ChatRepository {
    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun sendMessage(chatId: String, senderId: String, text: String){
      val messageId = db.collection("chats").document(chatId).collection("messages").document().id

        val message = ChatMessage(
            Id = messageId,
            text = text,
            senderId = senderId,
        )
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .await()
    }
    fun listenForMessages(chatId: String, onMessagesChanged: (List<ChatMessage>) -> Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                onMessagesChanged(messages)
            }
    }

}

