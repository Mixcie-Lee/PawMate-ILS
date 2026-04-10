package com.example.pawmate_ils

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterHomeScreen(
    navController: NavController,
    defImage: Int = R.drawable.avatar,
    sharedViewModel: SharedViewModel,
    likedPetsCount : Int
) {
   val custFont = FontFamily(
       Font(R.font.opensans)
   )
    val custFont2 = FontFamily(
        Font(R.font.custom_font)
    )

    //Avatar Section
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
    //END AVATAR SECTION

    //Icons Section
    val navItemList = listOf(
        NavItem("Home",Icons.Default.Home),
        NavItem("Adopt",Icons.Default.Pets),
        NavItem("Message", Icons.Default.Message)
    )
    var count = remember { mutableIntStateOf(0) }
    var selectedIndex : Int = count.value
    //End Icons section
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontFamily = custFont2,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFB6C1)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label,
                                tint = Color(0xFFFFB6C1)
                            )
                        },
                        label = {
                            Text(
                                text = navItem.label,
                                color = Color.Black,
                                fontFamily = custFont
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(defImage),
                contentDescription = "User profile logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, color = Color.Black, CircleShape)
                    .clickable { galleryLauncher.launch("image") },
                contentScale = ContentScale.Crop,
            )

            TextField(
                modifier = Modifier.fillMaxWidth(0.65f),
                value = sharedViewModel.username.value,
                onValueChange = { sharedViewModel.username.value = it },
                label = {
                    Text(
                        text = "Username",
                        fontSize = 15.sp
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB6C1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Liked Pets",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$likedPetsCount",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.cat_icon),
                contentDescription = "image",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}












// Preview version without NavController requirement
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, apiLevel = 34)
@Composable
fun AdopterHomeScreenPreview() {

    val previewViewModel = SharedViewModel().apply {
        username.value = "Raym Fowell"
    }

   AdopterHomeScreen(
        navController = rememberNavController(),
        sharedViewModel = previewViewModel,
        likedPetsCount = 0
    )
}
