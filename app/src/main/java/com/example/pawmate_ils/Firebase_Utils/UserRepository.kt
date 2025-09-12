package com.example.pawmate_ils.Firebase_Utils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//FOR SAVING USER CREDENTIAL
data class UserProfile(
        val UserId: String =  "",
        val text: String = "",
        val senderId: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val preferences: Map<String, String> = emptyMap()
        )

class UserRepository{
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: UserProfile){
          db.collection("users")
              .document(user.UserId)
              .set(user)
              .await()
    }
   //for updating user preferences ito GA :)
   suspend fun updatePreferences(userId: String, preferences: Map<String, Any>) {
       db.collection("users")
           .document(userId)
           .update("preferences", preferences)
           .await()
   }


    fun listenToUser(userId : String, onUserChanged: (UserProfile?) -> Unit){
        db.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(UserProfile::class.java)
                onUserChanged(user)
            }
    }
}

