package com.example.pawmate_ils

import android.content.Context

/**
 * Default adopter profile art: packaged drawables [R.drawable.boypfp], [R.drawable.girlpfp].
 * [photoUriForGender] is written to Firestore on sign-up so chat/shelter rows can load the same
 * URI via Coil ([android.resource] scheme) across devices running this app.
 *
 * (There is no separate default-PFP HTTP API in this project; Cloudinary is only used for uploads.)
 */
object ProfilePhotoDefaults {

    fun placeholderResForGender(gender: String?): Int = when (gender) {
        "Male" -> R.drawable.boypfp
        "Female" -> R.drawable.girlpfp
        else -> R.drawable.avatar
    }

    fun photoUriForGender(context: Context, gender: String?): String {
        val resId = placeholderResForGender(gender)
        return "android.resource://${context.packageName}/$resId"
    }
}
