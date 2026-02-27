package com.example.pawmate_ils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryHelper {

    /**
     * ✅ Initialize Cloudinary
     * Call this in your MainActivity's onCreate: CloudinaryHelper.init(this)
     */
    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "dyd4vx59u", // Your Cloud Name
            "secure" to true
        )
        try {
            MediaManager.init(context, config)
            Log.d("Cloudinary", "MediaManager initialized successfully")
        } catch (e: IllegalStateException) {
            // This happens if init is called more than once (e.g., activity recreation)
            Log.d("Cloudinary", "MediaManager already initialized")
        } catch (e: Exception) {
            Log.e("Cloudinary", "Initialization error: ${e.message}")
        }
    }

    /**
     * ✅ Upload Image to Cloudinary
     * @param imageUri The local Uri of the image to upload
     * @param onResult Callback that returns the secure URL on success, or null on failure
     */
    fun uploadImage(imageUri: Uri, onResult: (String?) -> Unit) {
        try {
            MediaManager.get().upload(imageUri)
                // ⚠️ THESIS TIP: Use an 'unsigned' preset for mobile to avoid
                // exposing your API Secret in the source code.
                .unsigned("profile_pic")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("Cloudinary", "Upload started")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        // ✅ Use secure_url to avoid Android 'Cleartext' blocking
                        val secureUrl = resultData["secure_url"] as? String
                        Log.d("Cloudinary", "✅ URL Generated: $secureUrl")
                        onResult(secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("Cloudinary", "❌ Upload Error: ${error.description}")
                        onResult(null)
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } catch (e: Exception) {
            Log.e("Cloudinary", "Dispatch error: ${e.message}")
            onResult(null)
        }
    }
    }
