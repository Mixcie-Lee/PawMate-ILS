package com.example.pawmate_ils

import android.annotation.SuppressLint
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.BottomAppBarDefaults.containerColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.pawmate_ils.AdopterHomeScreen
import com.example.pawmate_ils.SharedViewModel
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties.ContentDescription
import androidx.compose.ui.text.font.FontWeight
import com.example.pawmate_ils.ui.theme.PetPink



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterHomeScreen(
    navController: NavController,
    defImage: Int = R.drawable.adopterlogo,
    sharedViewModel: SharedViewModel,
    likedPetsCount : Int
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
            .offset(y = 50.dp),
        horizontalAlignment =  Alignment.CenterHorizontally

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
        Text(
            modifier =
            Modifier
                .offset(x = 5.dp)
                .size(100.dp),

            text = sharedViewModel.username.value
        )
        Spacer(modifier = Modifier.height(10.dp))

//CARD SECTION FOR LIKED PETS
        Card(
            modifier = Modifier
                .padding(16.dp)
                .width(180.dp)
                .offset(y = 10.dp)
                .height(100.dp),

            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),

            ) {
            Text(
                text = "Liked Pets :",
                 style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.height(10.dp))
        }
        /* BOTTOM NAV BAR SECTION HERE */

        val navItemList = listOf(
            NavItem("Home",Icons.Default.Home),
            NavItem("Adopt",Icons.Default.Pets),
            NavItem("Message", Icons.Default.Message)

        )
        var count = remember { mutableStateOf(0) }
        var selectedIndex : Int = count.value
        Scaffold(
            modifier = Modifier
                .offset(y = -35.dp)
                .clip(RoundedCornerShape(50.dp))

            ,


            bottomBar = {
                NavigationBar(
                    modifier = Modifier.background(PetPink)
                )
                {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                            },
                            icon = {
                                Icon(imageVector = navItem.icon, contentDescription = navItem.label )
                            },
                            label = {
                                Text(text = navItem.label)
                            }


                        )
                    }
                }
            }


        ){innerPadding ->
            ContentScreen(modifier = Modifier.padding(innerPadding))

        }



    }

}

@Composable
fun ContentScreen(modifier: Modifier){

}







// Preview version without NavController requirement
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun AdopterHomeScreenPreview() {

    val previewViewModel = SharedViewModel().apply {
        username.value = "Raym Fowell"
    }

   AdopterHomeScreen(
        navController = rememberNavController(),
        sharedViewModel = previewViewModel,
        likedPetsCount = 12
    )
}
