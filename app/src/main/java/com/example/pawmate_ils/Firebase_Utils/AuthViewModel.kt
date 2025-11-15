package com.example.pawmate_ils.Firebase_Utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.GemManager
import com.example.pawmate_ils.SettingsManager
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

open class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    open val currentUser get() = auth.currentUser


    private val _profilePhotoUrl = MutableLiveData<String?>()
    val profilePhotoUrl: LiveData<String?> = _profilePhotoUrl

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole

    private val _newUser = MutableLiveData<Boolean>()
    val newUser: LiveData<Boolean> = _newUser



    private val _userGems = MutableStateFlow(0)
    val userGems: StateFlow<Int> = _userGems

    private val _likedPetsCount = MutableStateFlow(0)
    val likedPetsCount: StateFlow<Int> = _likedPetsCount


    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
            //GET ROLE AFTER A SUCCESFUL LOGIN
            viewModelScope.launch {
                try {
                    val uid = auth.currentUser!!.uid
                    val snapshot = db.collection("users").document(uid).get().await()
                    _currentUserRole.value = snapshot.getString("role")
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "Failed to fetch role: ${e.message}")
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val result = task.result
                    _newUser.value = result?.additionalUserInfo?.isNewUser == true
                    if (user != null) {
                        viewModelScope.launch {
                            try {
                                val repo = FirestoreRepository()
                                val userDoc = repo.getUserById(user.uid)

                                if (userDoc == null) {
                                    // 🔹 NEW: Gem Distribution Logic for new email sign-up
                                    val userData = hashMapOf(
                                        "email" to email,
                                        "role" to "adopter",
                                        "gems" to 10,
                                        "likedPetsCount" to 0,
                                        "isNewUser" to true,
                                        "createdAt" to System.currentTimeMillis(),
                                        "lastActive" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(user.uid).set(userData).await()
                                    _userGems.value = 10
                                    _likedPetsCount.value = 0
                                } else {
                                    _userGems.value = userDoc.gems
                                    _likedPetsCount.value = userDoc.likedPetsCount
                                }

                                _authState.value = AuthState.Authenticated
                                onResult(true, null)
                            } catch (e: Exception) {
                                _authState.value =
                                    AuthState.Error(e.message ?: "Firestore setup failed")
                                onResult(false, e.message)
                            }
                        }
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Sign-up failed"
                    _authState.value = AuthState.Error(errorMsg)
                    onResult(false, errorMsg)
                }
            }
    }


    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        _authState.value =
                            AuthState.Error("Email not verified. Please check your inbox.")
                        signOut()
                        onResult(false, "Email not verified")
                    } else {
                        viewModelScope.launch {
                            try {
                                val repo = FirestoreRepository()
                                val userDoc = repo.getUserById(user!!.uid)
                                if (userDoc != null) {
                                    _userGems.value = userDoc.gems
                                    _likedPetsCount.value = userDoc.likedPetsCount
                                    _currentUserRole.value = userDoc.role

                                    if (userDoc.isNewUser == true) {
                                        db.collection("users").document(user.uid)
                                            .update("isNewUser", false).await()
                                    }

                                    _authState.value = AuthState.Authenticated
                                    onResult(true, null)
                                } else {
                                    _authState.value =
                                        AuthState.Error("User not registered in Firestore")
                                    signOut()
                                    onResult(false, "User not registered")
                                }
                            } catch (e: Exception) {
                                _authState.value =
                                    AuthState.Error(e.message ?: "Firestore check failed")
                                signOut()
                                onResult(false, e.message)
                            }
                        }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Sign-in failed")
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signUpWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    _newUser.value = result?.additionalUserInfo?.isNewUser == true
                    viewModelScope.launch {
                        try {
                            val user = auth.currentUser!!
                            val repo = FirestoreRepository()
                            val userDoc = repo.getUserById(user.uid)

                            if (userDoc != null) {
                                // Existing Google user
                                _userGems.value = userDoc.gems
                                _likedPetsCount.value = userDoc.likedPetsCount
                                _currentUserRole.value = userDoc.role
                                _authState.value = AuthState.Authenticated
                                onResult(true, null)
                            } else {
                                // 🔹 NEW: Gem Distribution Logic for new Google sign-in
                                val newUser = User(
                                    id = user.uid,
                                    name = user.displayName ?: "",
                                    email = user.email ?: "",
                                    role = "adopter",
                                    MobileNumber = "",
                                    Address = "",
                                    Age = "",
                                    gems = 10,
                                    likedPetsCount = 0,
                                    createdAt = System.currentTimeMillis(),  // <-- NOW valid
                                    lastActive = System.currentTimeMillis(), // 🔹 FIXED: lastActive as Timestamp
                                    isNewUser = true
                                )
                                db.collection("users").document(user.uid).set(newUser).await()

                                _userGems.value = 10
                                _likedPetsCount.value = 0
                                _currentUserRole.value = "adopter"

                                _authState.value = AuthState.Authenticated
                                onResult(true, null)
                            }
                        } catch (e: Exception) {
                            _authState.value =
                                AuthState.Error(e.message ?: "Firestore check failed")
                            onResult(false, e.message)
                        }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Google sign-in failed")
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun syncUserDataLocal(context: Context) {
        val user = auth.currentUser ?: return
        val settings = SettingsManager(context)

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(user.uid).get().await()

                if (snapshot.exists()) {
                    val name = snapshot.getString("name") ?: user.displayName ?: "User"
                    val photoUri = snapshot.getString("photoUri")

                    settings.setUsername(name)
                    settings.setProfilePhotoUri(photoUri)

                    Log.d("AuthViewModel", "✅ User data synced locally for ${user.uid}")
                } else {
                    Log.w("AuthViewModel", "⚠️ No user document found for ${user.uid}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Failed to sync user data: ${e.message}", e)
            }
        }
    }

    fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val role = userDoc.getString("role") ?: "unknown"
                _currentUserRole.value = role
                println("✅ User role fetched: $role")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Failed to fetch user role: ${e.message}")
                _currentUserRole.value = "unknown"
            }
        }
    }

    // ✅ Your deleteAccountAndData() remains unchanged
    fun deleteAccountAndData(
        context: Context,
        currentPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) return onResult(false, "No user logged in")

        viewModelScope.launch {
            try {
                // 1️⃣ Re-authenticate user
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                // 2️⃣ Delete Firestore user document
                val userDocRef = db.collection("users").document(user.uid)
                userDocRef.delete().await()

                // 3️⃣ Delete likedPets subcollection
                val likedPetsSnapshot = userDocRef.collection("likedPets").get().await()
                val batch = db.batch()
                likedPetsSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()

                // 4️⃣ Delete channels and messages in Realtime DB
                val rtdb = FirebaseDatabase.getInstance().reference
                val channelRef = rtdb.child("channel")
                val messageRef = rtdb.child("message")

                val channelsSnapshot = channelRef.get().await()
                for (child in channelsSnapshot.children) {
                    val channel = child.getValue(Channel::class.java)
                    if (channel != null &&
                        (channel.adopterId == user.uid || channel.shelterId == user.uid)
                    ) {
                        channelRef.child(child.key!!).removeValue().await()
                        messageRef.child(child.key!!).removeValue().await()
                    }
                }

                // 5️⃣ Clear local SettingsManager prefs
                val settings = SettingsManager(context)
                settings.setUsername("")
                settings.setProfilePhotoUri(null)

                // 6️⃣ Reset gems
                GemManager.setGemCount(0)

                // 7️⃣ Delete Firebase Auth user
                user.delete().await()

                onResult(true, "Account and all data deleted successfully")
            } catch (e: Exception) {
                Log.e("DELETE_USER", "Error deleting user data", e)
                onResult(false, e.message)
            }
        }
    }

    // ✅ Password-related functions remain intact
    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email.")
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent to $email.")
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun changePassword(newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password updated successfully.")
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } else {
            onResult(false, "No user is currently signed in.")
        }
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    onResult(true, "Password updated successfully.")
                                } else {
                                    onResult(false, updateTask.exception?.message)
                                }
                            }
                    } else {
                        onResult(false, "Current password is incorrect.")
                    }
                }
        } else {
            onResult(false, "User not logged in.")
        }
    }

    fun updateGems(newGems: Int) {
        val uid = auth.currentUser?.uid ?: return
        _userGems.value = newGems

        viewModelScope.launch {
            try {
                FirestoreRepository().updateGems(uid, newGems)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update gems: ${e.message}")
            }
        }
    }

    fun updateLikedPetsCount(newCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        _likedPetsCount.value = newCount

        viewModelScope.launch {
            try {
                FirestoreRepository().updateLikedPetsCount(uid, newCount)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update liked pets count: ${e.message}")
            }
        }
    }

    fun handleUserLogin(userId: String) {
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val existingGems = document.getLong("gems")?.toInt() ?: 0
                GemManager.setGemCount(existingGems)
            } else {
                // 🔹 NEW: Gem Distribution Logic fallback
                val newUserData = hashMapOf(
                    "gems" to 10,
                    "createdAt" to System.currentTimeMillis()
                )
                userRef.set(newUserData)
                GemManager.setGemCount(10)
            }
        }.addOnFailureListener { e -> e.printStackTrace() }
    }
    fun migrateOldTimestamps() {
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").get().await()

                for (doc in snapshot.documents) {
                    val updates = mutableMapOf<String, Any>()

                    val lastActiveLong = doc.getLong("lastActive")
                    val createdAtLong = doc.getLong("createdAt")

                    if (lastActiveLong != null) {
                        updates["lastActive"] = Timestamp(lastActiveLong, 0)
                    }

                    if (createdAtLong != null) {
                        updates["createdAt"] = Timestamp(createdAtLong, 0)
                    }

                    if (updates.isNotEmpty()) {
                        doc.reference.update(updates).await()
                        Log.d("Migration", "Updated document ${doc.id}")
                    }
                }

                Log.d("Migration", "✅ Migration completed successfully!")

            } catch (e: Exception) {
                Log.e("Migration", "Migration failed: ${e.message}", e)
            }
        }
    }
    fun updateEmailInFirestore(newEmail: String, currentPassword: String, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "No user logged in")
        viewModelScope.launch {
            try {
                FirestoreRepository().updateEmail(uid, newEmail)
                onResult(true, "Email updated successfully in Firestore.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update email.")
            }
        }
    }

    fun updatePhoneNumberInFirestore(newPhone: String, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "No user logged in")
        viewModelScope.launch {
            try {
                FirestoreRepository().updatePhoneNumber(uid, newPhone)
                onResult(true, "Phone number updated successfully in Firestore.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update phone number.")
            }
        }
    }





    sealed class AuthState {
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
