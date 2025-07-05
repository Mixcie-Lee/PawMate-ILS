package com.example.pawmate_ils

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.painterResource
import com.example.pawmate_ils.SharedViewModel

@Composable
fun AdopterHomeScreen(
    navController: NavController,
    defImage: Int = R.drawable.adopterlogo,
    sharedViewModel: SharedViewModel
) {
    var selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    // Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { Uri -> selectedImageUri = Uri as MutableState<Uri?> }
    )

    // Determine which image to show
    val painter = rememberAsyncImagePainter(
        model = selectedImageUri ?: defImage
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(x = 90.dp)
    ) {
        // Circular Image with Click Handler
        Image(
            painter = painterResource(defImage),
            contentDescription = "User  profile logo",
            modifier = Modifier
                .size(120.dp) // Set size for the image
                .clip(CircleShape) // Makes it circular
                .clickable { galleryLauncher.launch("image/*") } // Launch image picker
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = sharedViewModel.username.value)
    }
}

// Preview version without NavController requirement
@Preview(showBackground = true)
@Composable
fun AdopterHomeScreenPreview() {


    val previewViewModel = SharedViewModel().apply {
        username.value = "Raym Fowell"
    }

    AdopterHomeScreen(
        navController = rememberNavController(),
        sharedViewModel = previewViewModel
    )
}