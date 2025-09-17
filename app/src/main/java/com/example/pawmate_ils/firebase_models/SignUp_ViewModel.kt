/*package com.example.pawmate_ils.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.Firebase_Utils.AuthRepository
import com.example.pawmate_ils.Firebase_Utils.UserRepository
import com.example.pawmate_ils.firebase_models.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Result<Boolean>>(Result.success(false))
    val signUpState: StateFlow<Result<Boolean>> = _signUpState

    fun signUp(email: String, password: String, name: String, role: String, address : String, ) {
        viewModelScope.launch {
            try {
                val user: FirebaseUser? = authRepository.signInWithEmail(email, password)
                if (user != null) {
                    val UserProfile = UserProfile(
                        id = user.uid,
                        name = name,
                        email = email,
                        address = address
                    )
                    userRepository.getUser(UserProfile)
                    _signUpState.value = Result.success(true)
                } else {
                    _signUpState.value = Result.failure(Exception("User creation failed"))
                }
            } catch (e: Exception) {
                _signUpState.value = Result.failure(e)
            }
        }
    }
}
*/