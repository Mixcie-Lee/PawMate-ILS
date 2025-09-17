package com.example.pawmate_ils.Firebase_Utils
import com.example.pawmate_ils.firebase_models.AdopterProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.pawmate_ils.firebase_models.ShelterProfile

class AuthRepository(
    private val  auth : FirebaseAuth = FirebaseAuth.getInstance()
){

        fun  getCurrentUserId(): String? = auth.currentUser?.uid


    suspend fun signUpWithEmail(email: String, password: String): String? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }
    suspend fun signInWithGoogle(idToken: String, userType: String): String? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return null

            when (userType.lowercase()) {
                "adopter" -> {
                    val AdopterRepo = AdopterRepository()
                    val existingProfile = AdopterRepo.getAdopter(uid)
                    if (existingProfile == null) {
                        val profile = AdopterProfile(
                            id = uid,
                            AdopterName = result.user?.displayName ?: "",
                            email = result.user?.email ?: "",
                            mobileNumber = "",
                            address = "",
                            role = "adopter",
                            password = ""
                        )
                        AdopterRepo.createUser(profile)
                    }
                }
                "shelter" -> {
                    val ShelterRepo = ShelterRepository()
                    val existingProfile = ShelterRepo.getShelter(uid)
                    if (existingProfile == null) {
                        val profile = ShelterProfile(
                            id = uid,
                            ShelterName  = result.user?.displayName ?: "",
                            email = result.user?.email ?: "",
                            mobileNumber = "",
                            address = "",
                            role = "shelter",
                            password = ""
                        )
                        ShelterRepo.createUser(profile)
                    }
                }
            }
            uid
        } catch (e: Exception) {
            null
        }
    }





    //not needed apple sign in for this project
    suspend fun signInWithApple(idToken: String, rawNonce: String): String? {
        val credential = OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.uid
        } catch (e: Exception) { null }
    }



}

