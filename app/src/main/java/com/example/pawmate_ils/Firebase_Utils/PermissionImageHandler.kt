package com.example.pawmate_ils.Firebase_Utils

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable
fun GalleryPermissionHandler(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    if (!showDialog) return

    val context = LocalContext.current

    // 🎯 Step 1: Tukuyin ang tamang permission base sa Android Version
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // 🎯 Step 2: Launcher para sa System Prompt
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
        onDismiss()
    }

    // 🎯 Step 3: Custom "Privacy First" Dialog
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Allow Gallery Access?") },
        text = {
            Text(text = "PawMate needs access to your gallery to upload photos for your profile, pet listings, or chat messages. 🐾")
        },
        confirmButton = {
            Button(onClick = { launcher.launch(galleryPermission) }) {
                Text(text = "Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}