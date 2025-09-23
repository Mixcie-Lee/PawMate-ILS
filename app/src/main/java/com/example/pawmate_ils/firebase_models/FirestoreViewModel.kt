package com.example.pawmate_ils.firebase_models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import kotlinx.coroutines.launch

class FirestoreViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    // Holds the list of users
    var users by mutableStateOf<List<User>>(emptyList())
        private set

    // Holds loading state
    var isLoading by mutableStateOf(false)
        private set

    // Holds error messages
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Fetch all users from Firestore
    fun fetchUsers() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                users = firestoreRepository.getAllUsers()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Fetch single user
    fun fetchUserById(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = firestoreRepository.getUserById(userId)
                onResult(user)
            } catch (e: Exception) {
                errorMessage = e.message
                onResult(null)
            }
        }
    }

    // Add a new user
    fun addUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreRepository.addUser(user)
                fetchUsers() // refresh list
                onComplete(true)
            } catch (e: Exception) {
                errorMessage = e.message
                onComplete(false)
            }
        }
    }

    // Update existing user
    fun updateUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreRepository.updateUser(user)
                fetchUsers()
                onComplete(true)
            } catch (e: Exception) {
                errorMessage = e.message
                onComplete(false)
            }
        }
    }

    // Delete user
    fun deleteUser(userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreRepository.deleteUser(userId)
                fetchUsers()
                onComplete(true)
            } catch (e: Exception) {
                errorMessage = e.message
                onComplete(false)
            }
        }
    }
}
