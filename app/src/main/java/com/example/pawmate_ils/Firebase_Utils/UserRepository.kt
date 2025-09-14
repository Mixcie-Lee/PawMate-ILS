package com.example.pawmate_ils.Firebase_Utils
import com.example.pawmate_ils.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val usersRef = FirebaseFirestore.getInstance().collection("users")

    suspend fun getUser(userId: String): UserProfile? {
        return try {
            val snapshot = usersRef.document(userId).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun createUser(profile: UserProfile) {
        usersRef.document(profile.id).set(profile).await()
    }
}
