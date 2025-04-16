package com.example.pawmate_ils.onboard

import android.content.Context

class UtilOnboarding(private val context: Context){

    fun isOnboardCompleted(): Boolean{
        return context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .getBoolean("completed", false)
    }

    fun setOnboardingCompleted(){
        context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("completed", true)
            .apply()
    }

    companion object
}