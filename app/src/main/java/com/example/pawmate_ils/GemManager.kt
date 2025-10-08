package com.example.pawmate_ils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GemManager {
    companion object {
        private var _gemCount by mutableStateOf(10) // Start with 10 gems
        val gemCount: Int get() = _gemCount
        
        private var _isPurchaseDialogOpen by mutableStateOf(false)
        val isPurchaseDialogOpen: Boolean get() = _isPurchaseDialogOpen
        
        fun consumeGem(): Boolean {
            return if (_gemCount > 0) {
                _gemCount--
                true
            } else {
                false
            }
        }
        
        fun addGems(amount: Int) {
            _gemCount += amount
        }
        
        fun openPurchaseDialog() {
            _isPurchaseDialogOpen = true
        }
        
        fun closePurchaseDialog() {
            _isPurchaseDialogOpen = false
        }
        
        fun purchaseGems(packageType: GemPackage) {
            addGems(packageType.gemAmount)
            closePurchaseDialog()
        }
    }
}

enum class GemPackage(val gemAmount: Int, val price: String) {
    SMALL(5, "₱49"),
    MEDIUM(15, "₱149"),
    LARGE(30, "₱249"),
    MEGA(60, "₱449")
}
