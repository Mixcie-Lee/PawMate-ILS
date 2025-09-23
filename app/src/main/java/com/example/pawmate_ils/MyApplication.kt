package com.example.pawmate_ils

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) //THIS JUST  INITIALIZE THE FIREBASE THROUGHOUT THE APP, SO THAT WE DON'T NEED TO CALL ITS INSTANCE TO EVERY COMPOSABLE
    }
}
