package com.example.pawmate_ils.Firebase_Utils

import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Add a new user
    suspend fun addUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    // Get all users
    suspend fun getAllUsers(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.toObjects(User::class.java)
    }

    // Get single user by ID
    suspend fun getUserById(userId: String): User? {
        val snapshot = usersCollection.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    // Update user
    suspend fun updateUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    // Delete user
    suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }
}
