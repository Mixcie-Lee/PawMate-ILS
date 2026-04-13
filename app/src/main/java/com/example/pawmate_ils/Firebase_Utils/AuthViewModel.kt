    package com.example.pawmate_ils.Firebase_Utils

    import android.content.Context
    import android.net.Uri
    import android.util.Log
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.cloudinary.android.MediaManager
    import com.cloudinary.android.callback.ErrorInfo
    import com.cloudinary.android.callback.UploadCallback
    import com.example.pawmate_ils.CloudinaryHelper
    import com.example.pawmate_ils.GemManager
    import com.example.pawmate_ils.SettingsManager
    import com.example.pawmate_ils.firebase_models.Channel
    import com.example.pawmate_ils.firebase_models.User
    import com.google.firebase.Timestamp
    import com.google.firebase.auth.AuthCredential
    import com.google.firebase.auth.EmailAuthProvider
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.GoogleAuthProvider
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.SetOptions
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.channels.awaitClose
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.callbackFlow
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await

    open class AuthViewModel : ViewModel() {

        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        open val currentUser get() = auth.currentUser

        //keeping track user data for profile image and stuff
        private val _userData = MutableStateFlow<User?>(null)
        val userData: StateFlow<User?> = _userData


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


        private var userListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        //THIS HANDLES THE ACTIVE STATUS
        private val rtdb = FirebaseDatabase.getInstance().reference


        init {
            checkAuthStatus()
        }
        // 🔹 HELPER: Centralize _newUser update to sync with Firestore
        private fun updateNewUserState(userDoc: User?) {
            _newUser.value = userDoc?.isNewUser ?: true
        }





        private fun checkAuthStatus() {
            if (auth.currentUser != null) {
                _authState.value = AuthState.Authenticated
                startUserProfileListener()
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

                        GemManager.clearData()

                        if (user != null) {
                            // 1. Send verification email
                            user.sendEmailVerification()

                            // 2. Report success to the UI
                            // We stay "Logged In" so SignUpScreen has permission to save to Firestore
                            onResult(true, "Account created! Please verify your email.")
                        }
                    } else {
                        val errorMsg = task.exception?.message ?: "Sign-up failed"
                        _authState.value = AuthState.Error(errorMsg)
                        onResult(false, errorMsg)
                    }
                }
        }



        fun signIn(context: Context, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.reload()?.addOnCompleteListener {
                            if (user != null && !user.isEmailVerified) {
                                _authState.value =
                                    AuthState.Error("Email not verified. Please check your inbox.")
                                signOut(context)
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
                                            _userData.value = userDoc
                                            GemManager.setGemCount(userDoc.gems)

                                            val settings = SettingsManager(context)
                                            settings.setUsername(userDoc.name)
                                            settings.setProfilePhotoUri(userDoc.photoUri)


                                            updateNewUserState(userDoc)
                                            startUserProfileListener()

                                            if (userDoc.isNewUser == true) {
                                                val updateData = mapOf("isNewUser" to false)
                                                db.collection("users").document(user.uid)
                                                    .set(
                                                        updateData,
                                                        SetOptions.merge()
                                                    ) // <--- CHANGE HERE
                                                    .await()
                                            }

                                            _authState.value = AuthState.Authenticated
                                            onResult(true, null)
                                        } else {
                                            _authState.value =
                                                AuthState.Error("User not registered in Firestore")
                                            signOut(context)
                                            onResult(false, "User not registered")
                                        }
                                    } catch (e: Exception) {
                                        _authState.value =
                                            AuthState.Error(e.message ?: "Firestore check failed")
                                        signOut(context)
                                        onResult(false, e.message)
                                    }
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

        fun signUpWithGoogle(context: Context, idToken: String, onResult: (Boolean, String?) -> Unit) {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        val isNewUser = result?.additionalUserInfo?.isNewUser == true
                        _newUser.value = isNewUser

                        viewModelScope.launch {
                            try {
                                val user = auth.currentUser!!
                                val repo = FirestoreRepository()
                                val userDoc = repo.getUserById(user.uid)
                                val settings = SettingsManager(context)

                                if (userDoc != null) {
                                    // EXISTING USER: Sync data and go to Home
                                    _userGems.value = userDoc.gems
                                    _currentUserRole.value = userDoc.role
                                    _userData.value = userDoc
                                    settings.setUsername(userDoc.name)
                                    _authState.value = AuthState.Authenticated
                                    onResult(true, "existing") // Mark as existing
                                } else {
                                    // NEW USER: Do NOT create the document here.
                                    // Let the SignUpScreen handle it in Step 2.
                                    _authState.value = AuthState.Authenticated
                                    onResult(true, "new") // Mark as new
                                }
                            } catch (e: Exception) {
                                _authState.value = AuthState.Error(e.message ?: "Google Login Failed")
                                onResult(false, e.message)
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Google sign-in failed")
                        onResult(false, task.exception?.message)
                    }
                }
        }
        fun signOut(context: Context, onComplete: (() -> Unit)? = null) {
            val uid = auth.currentUser?.uid

            if (uid != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // 🔴 PHASE 1: Tell the server we are offline while we still have permissions
                        // Update RTDB Watchdog
                        rtdb.child("status/$uid").setValue(false).await()

                        // Trigger the cascading offline status in Firestore
                        updateOnlineStatus(false)

                        // 🔴 PHASE 2: Local cleanup on the Main Thread
                        launch(Dispatchers.Main) {
                            performLocalSignOut(context, onComplete)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Sign-out database sync failed: ${e.message}")
                        // Fallback: Proceed with local signout so the user isn't stuck
                        launch(Dispatchers.Main) { performLocalSignOut(context, onComplete) }
                    }
                }
            } else {
                performLocalSignOut(context, onComplete)
            }
        }

        private fun performLocalSignOut(context: Context, onComplete: (() -> Unit)?) {
            // Stop listening to Firestore profile updates
            userListenerRegistration?.remove()
            userListenerRegistration = null

            // Wipe LiveData and StateFlows
            _userData.value = null
            _currentUserRole.value = null
            _profilePhotoUrl.value = null

            // Revoke the Firebase Auth Token
            auth.signOut()

            // Reset App State to Unauthenticated
            _authState.value = AuthState.Unauthenticated
            clearLocalUserData(context)

            Log.d("AuthViewModel", "Local session terminated successfully.")
            onComplete?.invoke()
        }



        fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
            val user = auth.currentUser
            if (user != null) {
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, "Verification email resent! Please check your inbox.")
                        } else {
                            onResult(false, task.exception?.message ?: "Failed to resend email.")
                        }
                    }
            } else {
                // This handles cases where the user session timed out
                onResult(false, "No active session. Please try signing in again first.")
            }
        }




        fun clearLocalUserData(context: Context){
            _userGems.value = 0
            _likedPetsCount.value = 0
            _currentUserRole.value = null
            _userData.value = null

            val settings = SettingsManager(context)
            settings.setUsername("")
            settings.setProfilePhotoUri(null)

            Log.d("AuthViewModel", "Physical local storage wiped.")

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

        //handler of profile images
        fun uploadToCloudinary(context: Context, uri: Uri, onComplete: (String) -> Unit) {
            com.cloudinary.android.MediaManager.get().upload(uri)
                .unsigned("profile_pic") // ✅ Your verified preset
                .callback(object : com.cloudinary.android.callback.UploadCallback {

                    override fun onStart(requestId: String?) {
                        android.util.Log.d("Cloudinary", "Upload started")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        // Keep this empty or add a progress log
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<out Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String ?: ""
                        android.util.Log.d("Cloudinary", "Upload success: $url")
                        onComplete(url)
                    }

                    override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        android.util.Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    }

                    override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        android.util.Log.d("Cloudinary", "Upload rescheduled")
                    }
                }).dispatch()
        }
    //for profile settings making it lively
    fun updateProfile(
        newName: String? = null,
        newPhotoUri: String? = null,
        newShelterHours: String? = null,
        aboutMe: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "No user logged in")

        viewModelScope.launch {
            try {
                val batch = db.batch()
                val userRef = db.collection("users").document(uid)
                val updates = mutableMapOf<String, Any>()

                // 🎯 1. PREPARE USER DOCUMENT UPDATES
                newName?.let {
                    updates["name"] = it
                    updates["shelterName"] = it
                }
                newPhotoUri?.let { updates["photoUri"] = it }
                newShelterHours?.let { updates["shelterHours"] = it }
                aboutMe?.let { updates["aboutMe"] = it }

                val timestamp = System.currentTimeMillis()
                updates["lastActive"] = timestamp

                batch.update(userRef, updates)

                // 🎯 2. BROADCAST TO CHANNELS (The Chat List Fix)
                if (newName != null || newPhotoUri != null) {
                    // Update channels where you are the SHELTER
                    val shelterChannels = db.collection("channels")
                        .whereEqualTo("shelterId", uid).get().await()

                    for (doc in shelterChannels.documents) {
                        val chUpdate = mutableMapOf<String, Any>()
                        newName?.let { chUpdate["shelterName"] = it }
                        newPhotoUri?.let { chUpdate["shelterPhotoUri"] = it }
                        batch.update(doc.reference, chUpdate)
                    }

                    // Update channels where you are the ADOPTER
                    val adopterChannels = db.collection("channels")
                        .whereEqualTo("adopterId", uid).get().await()

                    for (doc in adopterChannels.documents) {
                        val chUpdate = mutableMapOf<String, Any>()
                        newName?.let { chUpdate["adopterName"] = it }
                        newPhotoUri?.let { chUpdate["adopterPhotoUri"] = it }
                        batch.update(doc.reference, chUpdate)
                    }
                }

                // 🎯 3. EXECUTE ALL UPDATES AT ONCE
                batch.commit().await()

                // 🎯 4. UPDATE LOCAL STATE (Lively UI)
                _userData.value = _userData.value?.copy(
                    name = newName ?: _userData.value?.name ?: "",
                    shelterName = newName ?: _userData.value?.shelterName ?: "",
                    photoUri = newPhotoUri ?: _userData.value?.photoUri ?: "",
                    shelterHours = newShelterHours ?: _userData.value?.shelterHours ?: "",
                    aboutMe = aboutMe ?: _userData.value?.aboutMe ?: "",
                    lastActive = timestamp
                )

                onResult(true, "Profile and conversations updated! 🐾")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Update failed: ${e.message}")
                onResult(false, e.message)
            }
        }
    }
        fun startUserProfileListener() {
            val uid = auth.currentUser?.uid ?: return

            // Use a SnapshotListener for real-time "lively" updates
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AuthViewModel", "Profile listen failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)

                        val rawTier = snapshot.get("tier")

                        val fixedTier = when (rawTier) {
                            is Long -> rawTier.toString()
                            is String -> rawTier // Already a string
                            else -> "0"
                        }

                        // 3. 🎯 Pass the String to .copy()
                        val finalUser = user?.copy(tier = fixedTier)
                        _userData.value = finalUser

                        // Keep other states in sync
                        finalUser?.let {
                            _currentUserRole.value = user?.role
                            _userGems.value = user?.gems ?: 0
                            _likedPetsCount.value = user?.likedPetsCount ?: 0

                            GemManager.setGemCount(it.gems ?: 10)

                            val tierInt = fixedTier.toIntOrNull() ?: 0
                            GemManager.updateLocalTier(tierInt)
                        }




                    }
                }

            // In your AuthViewModel.kt
        }
        fun getUserByIdFlow(uid: String): Flow<User?> = callbackFlow {
            val docRef = db.collection("users").document(uid)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AuthViewModel", "Error fetching target user: ${error.message}")
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    Log.d("AuthViewModel", "Successfully caught user: ${user.name}")
                }
                trySend(user)
            }

            // 🛡️ Safety: Stop listening when the screen is closed
            awaitClose {
                Log.d("AuthViewModel", "Closing listener for $uid")
                listener.remove()
            }
        }
        fun updateOnlineStatus(isOnline: Boolean) {
            val uid = auth.currentUser?.uid ?: return

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val repo = FirestoreRepository()
                    val timestamp = System.currentTimeMillis()

                    val userStatusRef = rtdb.child("status/$uid")
                    if (isOnline) {
                        userStatusRef.setValue(true)
                        // This tells the server: "If this user vanishes, set RTDB status to false"
                        userStatusRef.onDisconnect().setValue(false)
                    } else {
                        userStatusRef.setValue(false)
                    }




                    // 1️⃣ FORCE FETCH THE ROLE: Don't rely on the StateFlow yet
                    val userDoc = db.collection("users").document(uid).get().await()
                    val role = userDoc.getString("role")

                    Log.d("AUTH_PRESENCE", "Verified Role from DB: $role")

                    // 2️⃣ Update User Profile
                    val userUpdates = mapOf(
                        "online" to isOnline,
                        "lastActive" to timestamp
                    )
                    db.collection("users").document(uid).update(userUpdates).await()

                    // 3️⃣ Sync based on the role we JUST fetched
                    when (role) {
                        "shelter" -> {
                            repo.updateShelterPresenceInPets(uid, isOnline)
                            repo.updateShelterPresenceInChannels(uid, isOnline, timestamp)
                        }
                        "adopter" -> {
                            repo.updateAdopterPresenceInChannels(uid, isOnline, timestamp)
                        }
                    }

                    Log.d("AUTH_PRESENCE", "✅ Heartbeat fully synced for $role")
                } catch (e: Exception) {
                    Log.e("AUTH_PRESENCE", "❌ Failed to sync: ${e.message}")
                }
            }
        }


        //HELPER FUNCTION  TO UPDATE ONLINE STATUS
        fun isUserActuallyOnline(user: User?): Boolean {
            // 1. If the record is old/null, it's NOT online.
            if (user == null) return false

            // 2. If the 'online' flag is missing or false, it's NOT online.
            if (!user.isOnline) return false

            // 3. LEGACY CHECK: If lastActive is null or 0, it's an old account.
            // Do not show the green dot.
            val lastActive = user.lastActive ?: return false
            if (lastActive == 0L) return false

            val currentTime = System.currentTimeMillis()
            val fiveMinutesInMs = 5 * 60 * 1000

            // 4. Only show green if the heartbeat is within the last 5 minutes.
            return (currentTime - lastActive) < fiveMinutesInMs
        }

        suspend fun uploadToCloudinarySync(context: Context, uri: Uri): String? {
            // We call the Helper we just updated
            return CloudinaryHelper.uploadImageSync(uri)
        }







        sealed class AuthState {
            object Authenticated : AuthState()
            object Unauthenticated : AuthState()
            object Loading : AuthState()
            data class Error(val message: String) : AuthState()
        }

//SYSTEM LOG OUT IF INACTIVE FOR SEVEN DAYS
        fun checkSessionExpiry(context: android.content.Context, onLogoutRequired: () -> Unit) {
            val user = auth.currentUser ?: return

            viewModelScope.launch {
                try {
                    // 1. Get the latest 'lastActive' from Firestore
                    val snapshot = db.collection("users").document(user.uid).get().await()
                    val lastActive = snapshot.getLong("lastActive") ?: 0L

                    if (lastActive == 0L) return@launch // No timestamp yet, wait for next activity

                    val currentTime = System.currentTimeMillis()
                    val sevenDaysInMs = 7L * 24 * 60 * 60 * 1000 // Math for 1 week

                    // 2. The Check: Has it been more than 7 days?
                    if (currentTime - lastActive > sevenDaysInMs) {
                        Log.d("SECURITY", "Session expired (7 days). Force logout.")
                        signOut(context) {
                            onLogoutRequired()
                        }
                    } else {
                        Log.d("SECURITY", "Session is still valid.")
                    }
                } catch (e: Exception) {
                    Log.e("SECURITY", "Expiry check failed: ${e.message}")
                }
            }
        }
        //FOR GOOGLE ACCOUNT USERS
        // 1️⃣ Main Entry Point: Call this from the UI
        fun requestAccountDeletion(context: Context, idToken: String? = null, currentPassword: String? = null, onComplete: (Boolean, String?) -> Unit) {
            val user = auth.currentUser ?: return onComplete(false, "No user logged in")

            // Detect Provider
            val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

            val credential = if (isGoogleUser) {
                if (idToken == null) return onComplete(false, "Google re-authentication required")
                GoogleAuthProvider.getCredential(idToken, null)
            } else {
                if (currentPassword == null) return onComplete(false, "Password required")
                EmailAuthProvider.getCredential(user.email!!, currentPassword)
            }

            performFinalDeletion(credential, onComplete)
        }


        // 2️⃣ SHARED: The actual deletion process (Cleaned up)
        private fun performFinalDeletion(credential: AuthCredential, onComplete: (Boolean, String?) -> Unit) {
            val user = auth.currentUser ?: return

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        try {
                            // 1. Delete Firestore Document
                            db.collection("users").document(user.uid).delete().await()

                            // 2. Delete Auth Account
                            user.delete().await()

                            onComplete(true, "Account and data deleted successfully. 🐾")
                        } catch (e: Exception) {
                            onComplete(false, "Data cleanup failed: ${e.message}")
                        }
                    }
                } else {
                    onComplete(false, "Authentication failed. Incorrect password or session expired.")
                }
            }
        }






    }
