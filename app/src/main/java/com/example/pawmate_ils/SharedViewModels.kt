package com.example.pawmate_ils

// SharedViewModel.kt
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var username = mutableStateOf("")  // Holds the username
}
