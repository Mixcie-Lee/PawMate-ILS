package com.example.pawmate_ils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



enum class UserTier(val level: Int, val cost: Int) {
    TIER_0(0, 0),    // No access (Locked)
    TIER_1(1, 10),   // Swipe & Chat(can swipe and chat)
    TIER_2(2, 25),   // Detailed Info(adopter can see the detailed info)
    TIER_3(3, 50)    // Priority Inbox(chat priority on top of channels)
}
class GemManager {
    companion object {

        private const val KEY_USER_TIER = "user_tier"
        private const val PREFS_NAME = "gem_prefs"
        private const val KEY_GEM_COUNT = "gem_count"

        private var _pendingPackage by mutableStateOf<GemPackage?>(null)
        val pendingPackage: GemPackage? get() = _pendingPackage

        private val _gemCount = MutableStateFlow(10)
        val gemCount: StateFlow<Int> get() = _gemCount

        private var _isPurchaseDialogOpen by mutableStateOf(false)
        val isPurchaseDialogOpen: Boolean get() = _isPurchaseDialogOpen

        private val _currentTier = MutableStateFlow(UserTier.TIER_0)
        val currentTier: StateFlow<UserTier> get() = _currentTier


        private lateinit var context: Context

        fun init(appContext: Context) {
            context = appContext
            loadGemCount()
        }

        private fun loadGemCount() {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _gemCount.value = prefs.getInt(KEY_GEM_COUNT, 10)

            val tierLevel = prefs.getInt(KEY_USER_TIER, 0)
            _currentTier.value = UserTier.entries.find { it.level == tierLevel } ?: UserTier.TIER_0

        }

        // Inside GemManager's companion object
        // Replace your old saveData and saveGemCount with this:
        private fun saveData() {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // We use the same edit() block to save both pieces of data at once
            prefs.edit().apply {
                // ✅ Using your specific code for Gems
                putInt(KEY_GEM_COUNT, _gemCount.value)

                // ✅ Also saving the Permanent Tier status in the same "stamp"
                putInt(KEY_USER_TIER, _currentTier.value.level)

                apply() // Commit both to the phone's memory
            }

            Log.d(
                "GEM_SYSTEM",
                "💾 Sync Complete | Gems: ${_gemCount.value} | Tier: ${_currentTier.value.level}"
            )
        }

        fun consumeGem(): Boolean {                                  // ✅ MODIFIED: remove Context param
            return if (_gemCount.value > 0) {
                _gemCount.value--                                    // ✅ MODIFIED: update StateFlow value
                saveData()                                       // ✅ MODIFIED: persist change
                true
            } else {
                false
            }
        }

        fun consumeGems(amount: Int): Boolean {
            return if (_gemCount.value >= amount) {
                _gemCount.value -= amount
                saveData()
                true
            } else {
                false
            }
        }

        fun addGems(amount: Int) {                                   // ✅ MODIFIED: update StateFlow value
            _gemCount.value += amount
            saveData()
        }

        // 🔹 Directly set gem count
        fun setGemCount(amount: Int) {                               // ✅ MODIFIED: update StateFlow value
            _gemCount.value = amount
            if (::context.isInitialized) {
                saveData()
            }
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

        fun initiatePurchase(packageType: GemPackage) {
            _pendingPackage = packageType
        }

        fun cancelPurchase() {
            _pendingPackage = null
        }
        // Inside GemManager.kt

        fun upgradeTier(newTier: UserTier, context: android.content.Context): Boolean {
            val current = _currentTier.value

            // 🛑 LOGIC CHECK 1: Is the user trying to buy something they already have?
            if (newTier.level <= current.level) {
                android.util.Log.d(
                    "GEM_SYSTEM",
                    "User tried to buy Tier ${newTier.level} but already has Tier ${current.level}"
                )
                android.widget.Toast.makeText(
                    context,
                    "You already have these perks unlocked!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return false
            }

            // 💰 LOGIC CHECK 2: Can they afford it?
            if (_gemCount.value >= newTier.cost) {
                _gemCount.value -= newTier.cost
                _currentTier.value = newTier // This is permanent
                saveData()

                android.widget.Toast.makeText(
                    context,
                    "Successfully upgraded to ${newTier.name}!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return true
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Insufficient Gems for this Upgrade",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        fun confirmPurchase(userId: String, onTier3Unlocked: () -> Unit = {}) {
            _pendingPackage?.let { pkg ->
                // 1. 💎 ALWAYS add the gems (Stackable logic)
                _gemCount.value += pkg.gemAmount

                // 2. 👑 PERMANENT Tier Logic
                // Determine what tier this package represents
                val packageTierLevel = when (pkg) {
                    GemPackage.SMALL -> 1
                    GemPackage.MEDIUM -> 2
                    GemPackage.LARGE -> 3
                }

                val currentLevel = _currentTier.value.level

                // Only update if the purchased package is a HIGHER tier than current
                if (packageTierLevel > currentLevel) {
                    _currentTier.value = UserTier.entries.find { it.level == packageTierLevel } ?: _currentTier.value
                    Log.d("GEM_SYSTEM", "Permanent Tier Upgraded to: ${packageTierLevel}")
                } else {
                    Log.d("GEM_SYSTEM", "Tier ${currentLevel} maintained. Added gems to existing perks.")
                }

                if (packageTierLevel == 3) {
                    onTier3Unlocked()
                }


                // 3. 💾 Sync both Disk and Cloud in one go
                saveData()
                syncTierToFirestore(userId, _currentTier.value, _gemCount.value)

                // 4. 🧹 Cleanup
                _pendingPackage = null
                closePurchaseDialog()

                Log.d("GEM_SYSTEM", "Purchase Complete: +${pkg.gemAmount} Gems. Current Tier: ${_currentTier.value.level}")
            }
        }

        fun clearData() {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply() // Erases everything in gem_prefs
            _gemCount.value = 10         // Reset to default
            _currentTier.value = UserTier.TIER_0
            Log.d("GEM_SYSTEM", "🧹 Local data cleared for logout")
        }

        fun checkTierEligibility(
            selectedPackage: GemPackage,
            onShowDialog: (String, String) -> Unit, // 🟢 Added Title and Message
            onDirectPurchase: () -> Unit
        ) {
            val packageTierLevel = when (selectedPackage) {
                GemPackage.SMALL -> 1
                GemPackage.MEDIUM -> 2
                GemPackage.LARGE -> 3
            }

            val currentLevel = _currentTier.value.level

            if (currentLevel > packageTierLevel) {
                // Higher Tier Case
                onShowDialog(
                    "Premium Status Active",
                    "You are a Tier $currentLevel member. Purchasing this will add ${selectedPackage.gemAmount} Gems to your account. Your higher-tier perks will remain unchanged."
                )
            } else if (currentLevel == packageTierLevel) {
                // Same Tier Case
                onShowDialog(
                    "Tier Already Unlocked",
                    "You already have the permanent perks for this tier. This purchase will only add ${selectedPackage.gemAmount} Gems to your balance."
                )
            } else {
                // New Upgrade
                onDirectPurchase()
            }
        }



        fun syncTierToFirestore(userId: String, newTier: UserTier, gems: Int) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            val updates = mapOf(
                "tier" to newTier.level.toString(),
                "gems" to gems // This ensures the cloud keeps the correct gem count!
            )

            db.collection("users").document(userId)
                .update(updates) // Save the level (1, 2, or 3)
                .addOnSuccessListener {
                    Log.d("GEM_SYSTEM", "🚀 Cloud Sync Success! Tier ${newTier.level} saved.")
                }

        }

        fun updateLocalTier(newTierLevel: Int) {
            if (!::context.isInitialized) return // Safety check

            val tierEnum = UserTier.entries.find { it.level == newTierLevel } ?: UserTier.TIER_0

            // Only update if it's different to save on memory/disk writes
            if (_currentTier.value != tierEnum) {
                _currentTier.value = tierEnum
                saveData() // Persist to SharedPreferences so it's there next time they open the app
                Log.d("GEM_SYSTEM", "🔄 Local Tier synced from Cloud: ${tierEnum.level}")
            }
        }




    }
}


/*
private fun loadGemCount() {
    // Check if initialized first to prevent lateinit crash
    if (!::context.isInitialized) return

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    _gemCount.value = prefs.getInt(KEY_GEM_COUNT, 10)
}

private fun saveGemCount() {
    if (!::context.isInitialized) return // 🔹 Prevents the crash!

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(KEY_GEM_COUNT, _gemCount.value).apply()
}
*/


/**
 * Gem packages available for purchase
 */
enum class GemPackage(val gemAmount: Int, val price: String) {
    SMALL(10, "₱49 | offers swiping pets and chat with adopters(default)"),
    MEDIUM(25, "₱149 | offers a more detailed view of pet info"),
    LARGE(50, "₱249 | offers a priority inbox for chat messages and offers the same feature of tier 2 "),
}
