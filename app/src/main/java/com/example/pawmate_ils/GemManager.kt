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
        TIER_2(2, 25),   // Swipe & Chat additional gems but no features
        TIER_3(3, 50)    // Premium subscription. Priority Inbox(chat priority on top of channels)
    }
    class GemManager {


        companion object {

            private const val KEY_USER_TIER = "user_tier"
            private const val PREFS_NAME = "gem_prefs"
            private const val KEY_GEM_COUNT = "gem_count"

            private var _isAdjustmentLocked by mutableStateOf(false)
            val isAdjustmentLocked: Boolean get() = _isAdjustmentLocked

            private var _pendingPackage by mutableStateOf<GemPackage?>(null)
            val pendingPackage: GemPackage? get() = _pendingPackage

            private val _gemCount = MutableStateFlow(10)
            val gemCount: StateFlow<Int> get() = _gemCount

            private var _isPurchaseDialogOpen by mutableStateOf(false)
            val isPurchaseDialogOpen: Boolean get() = _isPurchaseDialogOpen

            private val _currentTier = MutableStateFlow(UserTier.TIER_0)
            val currentTier: StateFlow<UserTier> get() = _currentTier

            var showExpiryDialog by mutableStateOf(false)


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
                if (!::context.isInitialized) return // Safety check para 'di mag-crash

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()

                // 1. I-set ang values sa editor
                editor.putInt(KEY_GEM_COUNT, _gemCount.value)
                editor.putInt(KEY_USER_TIER, _currentTier.value.level)


                val isSaved = editor.commit()

                Log.d(
                    "GEM_SYSTEM",
                    "💾 HARD SAVE: $isSaved | Gems: ${_gemCount.value} | Tier: ${_currentTier.value.level}"
                )
            }



            fun consumeGems(amount: Int, userId: String): Boolean { // ✅ Dagdagan natin ng userId param
                return if (_gemCount.value >= amount) {
                    _gemCount.value -= amount
                    saveData()

                    // 🔥 BURNING TO CLOUD: Kahit mag-logout, bawas na talaga
                    syncTierToFirestore(userId, _currentTier.value, _gemCount.value)
                    true
                } else {
                    false
                }
            }

            fun addGems(amount: Int) {
                // ✅ MODIFIED: update StateFlow value
                if (isAdjustmentLocked) {
                    Log.d("GEM_SYSTEM", "🚫 REFUND BLOCKED: Gem adjustment is currently locked.")
                    return // 🔒 Hindi papasok ang dagdag na gems
                }
                _gemCount.value += amount
                saveData()
            }

            // 🔹 Directly set gem count
            fun setGemCount(amount: Int) {                               // ✅ MODIFIED: update StateFlow value
                if (isAdjustmentLocked) {
                    Log.d("GEM_SYSTEM", "🚫 SET_GEMS BLOCKED: State is locked.")
                    return
                }
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
                    // 1. 💎 ALWAYS add gems
                    _gemCount.value += pkg.gemAmount

                    val currentLevel = _currentTier.value.level
                    var expiryTimestamp = 0L

                    // 2. 👑 Tier logic based on package
                    when (pkg) {
                        GemPackage.SMALL, GemPackage.MEDIUM -> {
                            // Tiers 1 and 2 are PERMANENT
                            val targetLevel = if (pkg == GemPackage.SMALL) 1 else 2
                            if (targetLevel > currentLevel) {
                                _currentTier.value = UserTier.entries.find { it.level == targetLevel } ?: _currentTier.value
                                Log.d("GEM_SYSTEM", "Permanent Tier Upgraded to: $targetLevel")
                            }
                        }
                        GemPackage.LARGE -> {
                            // 🎯 Tier 3 is a 30-DAY SUBSCRIPTION
                            val calendar = java.util.Calendar.getInstance()
                            calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
                            expiryTimestamp = calendar.timeInMillis

                            _currentTier.value = UserTier.TIER_3
                            onTier3Unlocked()
                            Log.d("GEM_SYSTEM", "Tier 3 Subscription Active. Expires: $expiryTimestamp")
                        }
                    }

                    // 3. 💾 Save locally and sync to Cloud
                    saveData()
                    syncTierToFirestore(userId, _currentTier.value, _gemCount.value, expiryTimestamp)

                    // 4. 🧹 Cleanup
                    _pendingPackage = null
                    closePurchaseDialog()
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
                val packageLevel = when (selectedPackage) {
                    GemPackage.SMALL -> 1
                    GemPackage.MEDIUM -> 2
                    GemPackage.LARGE -> 3
                }

                val currentLevel = _currentTier.value.level

                if (currentLevel > packageLevel) {
                    // Higher Tier Case
                    onShowDialog(
                        "Premium Status Active", "You are Tier $currentLevel. This adds ${selectedPackage.gemAmount} Gems; your perks remain.")
                } else if (currentLevel == packageLevel && packageLevel != 3) {
                    // Same Tier Case
                    onShowDialog("Tier Already Unlocked", "You have these permanent perks. This adds ${selectedPackage.gemAmount} Gems.")
                } else {
                    // New Upgrade
                    onDirectPurchase()
                }
            }



            fun syncTierToFirestore(userId: String, newTier: UserTier, gems: Int, expiry: Long = 0L) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                val updates = mutableMapOf<String, Any>(
                    "tier" to newTier.level.toString(),
                    "gems" to gems
                )

                if (newTier == UserTier.TIER_3) {
                    updates["tierExpiry"] = expiry
                }



                db.collection("users").document(userId)
                    .update(updates) // Save the level (1, 2, or 3)
                    .addOnSuccessListener {
                        Log.d("GEM_SYSTEM", "🚀 Cloud Sync Success! Tier ${newTier.level} saved.")
                    }

            }
            fun triggerExpiryNotice() {
                showExpiryDialog = true
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

            fun lockGems() {
                _isAdjustmentLocked = true
            }
            fun unlockGems() {
                _isAdjustmentLocked = false
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
     *
     *
     */
    enum class GemPackage(val gemAmount: Int, val price: String) {
        SMALL(10, "₱49 | offers swiping pets and chat with shelters"),
        MEDIUM(25, "₱129 | top-up gems for more swipes and chat messages"),
        LARGE(50, "₱249 | Ultimate: Full access to priority chats and detailed pet info for 1 month"),
    }


