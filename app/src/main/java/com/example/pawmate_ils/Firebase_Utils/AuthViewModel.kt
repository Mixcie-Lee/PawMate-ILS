    package com.example.pawmate_ils.Firebase_Utils

    import android.content.Context
    import android.net.Uri
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.pawmate_ils.SettingsManager
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.GoogleAuthProvider
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
        val currentUserRole : StateFlow<String?> = _currentUserRole

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
                        _authState.value = AuthState.Authenticated
                        onResult(true, null)
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
                            _authState.value = AuthState.Error("Email not verified. Please check your inbox.")
                            signOut()
                            onResult(false, "Email not verified")
                        } else {
                            // ✅ Check Firestore for registered user
                            viewModelScope.launch {
                                try {
                                    val repo = FirestoreRepository()
                                    val userDoc = repo.getUserById(user!!.uid)
                                    if (userDoc != null) {
                                        // ✅ User is registered — proceed normally
                                        _currentUserRole.value = userDoc.role
                                        _authState.value = AuthState.Authenticated
                                        onResult(true, null)
                                    } else {
                                        _authState.value = AuthState.Error("User not registered in Firestore")
                                        signOut()
                                        onResult(false, "User not registered")
                                    }
                                } catch (e: Exception) {
                                    _authState.value = AuthState.Error(e.message ?: "Firestore check failed")
                                    signOut()
                                    onResult(false, e.message)
                                }
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign-in failed")
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
                        viewModelScope.launch {
                            try {
                                val user = auth.currentUser!!
                                val repo = FirestoreRepository()
                                val userDoc = repo.getUserById(user.uid)
                                if (userDoc != null) {
                                    // Already registered — proceed normally
                                    _authState.value = AuthState.Authenticated
                                    onResult(true, null)
                                } else {
                                    // New Google user — go to Step 2 (About You)
                                    _authState.value = AuthState.Unauthenticated
                                    onResult(true, "NEW_USER")
                                }
                            } catch (e: Exception) {
                                _authState.value = AuthState.Error(e.message ?: "Firestore check failed")
                                onResult(false, e.message)
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Google sign-in failed")
                        onResult(false, task.exception?.message)
                    }
                }
        }


        fun uploadProfilePhoto(imageUri: Uri, context: Context, onComplete: (() -> Unit)? = null) {
            val uid = auth.currentUser?.uid ?: return
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val photoRef = storageRef.child("profile_photos/$uid.jpg")

            viewModelScope.launch {
                try {
                    // ✅ Upload the image to Firebase Storage
                    photoRef.putFile(imageUri).await()

                    // ✅ Get the download URL
                    val downloadUrl = photoRef.downloadUrl.await().toString()

                    // ✅ Update Firestore user document
                    db.collection("users").document(uid)
                        .update("photoUri", downloadUrl)
                        .await()

                    // ✅ Save locally so photo persists
                    val settings = SettingsManager(context)
                    settings.setProfilePhotoUri(downloadUrl)

                    // ✅ Update LiveData for UI
                    _profilePhotoUrl.postValue(downloadUrl)

                    onComplete?.invoke()
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "Photo upload failed", e)
                    e.printStackTrace()
                    onComplete?.invoke()
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
                    // Fetch user data from Firestore
                    val snapshot = db.collection("users").document(user.uid).get().await()

                    if (snapshot.exists()) {
                        val name = snapshot.getString("name") ?: user.displayName ?: "User"
                        val photoUri = snapshot.getString("photoUri")

                        // ✅ Save locally using your exact SettingsManager methods
                        settings.setUsername(name)
                        settings.setProfilePhotoUri(photoUri)

                        android.util.Log.d("AuthViewModel", "✅ User data synced locally for ${user.uid}")
                    } else {
                        android.util.Log.w("AuthViewModel", "⚠️ No user document found for ${user.uid}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "❌ Failed to sync user data: ${e.message}", e)
                }
            }
        }

        fun fetchUserRole() {
            val uid = auth.currentUser?.uid ?: return
            viewModelScope.launch {
                try {
                    val userDoc = db.collection("users").document(uid).get().await()
                    val role = userDoc.getString("role") ?: "unknown" // fallback value
                    _currentUserRole.value = role
                    println("✅ User role fetched: $role")
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "❌ Failed to fetch user role: ${e.message}")
                    _currentUserRole.value = "unknown" // prevent staying null
                }
            }
        }







    }








    sealed class AuthState {
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }
