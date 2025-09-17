package com.example.pawmate_ils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode
    
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
}
