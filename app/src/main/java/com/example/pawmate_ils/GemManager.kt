package com.example.pawmate_ils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GemManager {
    companion object {
        private const val PREFS_NAME = "gem_prefs"
        private const val KEY_GEM_COUNT = "gem_count"

        private val _gemCount = MutableStateFlow(10)
        val gemCount: StateFlow<Int> get() = _gemCount

        private var _isPurchaseDialogOpen by mutableStateOf(false)
        val isPurchaseDialogOpen: Boolean get() = _isPurchaseDialogOpen

        private lateinit var context: Context

        fun init(appContext: Context) {
            context = appContext
            loadGemCount()
        }

        private fun loadGemCount() {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _gemCount.value = prefs.getInt(KEY_GEM_COUNT, 10)
        }
        private fun saveGemCount() {                                 // ✅ MODIFIED
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(KEY_GEM_COUNT, _gemCount.value).apply()  // ✅ FIXED: save .value
        }

        fun consumeGem(): Boolean {                                  // ✅ MODIFIED: remove Context param
            return if (_gemCount.value > 0) {
                _gemCount.value--                                    // ✅ MODIFIED: update StateFlow value
                saveGemCount()                                       // ✅ MODIFIED: persist change
                true
            } else {
                false
            }
        }

        fun consumeGems(amount: Int): Boolean {
            return if (_gemCount.value >= amount) {
                _gemCount.value -= amount
                saveGemCount()
                true
            } else {
                false
            }
        }

        fun addGems(amount: Int) {                                   // ✅ MODIFIED: update StateFlow value
            _gemCount.value += amount
            saveGemCount()
        }

        // 🔹 Directly set gem count
        fun setGemCount(amount: Int) {                               // ✅ MODIFIED: update StateFlow value
            _gemCount.value = amount
            saveGemCount()
        }

        // 🔹 Open gem purchase dialog
        fun openPurchaseDialog() {                                   // ✅ NEW
            _isPurchaseDialogOpen = true
        }

        // 🔹 Close gem purchase dialog
        fun closePurchaseDialog() {                                  // ✅ NEW
            _isPurchaseDialogOpen = false
        }

        // 🔹 Purchase gems via package
        fun purchaseGems(packageType: GemPackage) {                  // ✅ NEW
            addGems(packageType.gemAmount)
            closePurchaseDialog()
        }
    }
}

/**
 * Gem packages available for purchase
 */
enum class GemPackage(val gemAmount: Int, val price: String) {
    SMALL(5, "₱49"),
    MEDIUM(15, "₱149"),
    LARGE(30, "₱249"),
    MEGA(60, "₱449")
}
