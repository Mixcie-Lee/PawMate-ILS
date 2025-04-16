package com.example.pawmate_ils.onboard

import android.content.Context

class OnboardingUtil(private val context: Context){

    fun isOnboardingCompleted(): Boolean{
        return context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .getBoolean("completed", false)
    }

    fun setOnboardingCompleted(){
        context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("completed", true)
            .apply()
    }
}