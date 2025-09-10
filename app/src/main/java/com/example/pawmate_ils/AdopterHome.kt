package com.example.pawmate_ils

import android.R.attr.shape
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.pawmate_ils.ui.theme.DarkBrown
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextAlign


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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = DarkBrown),
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 35.sp,
                        fontFamily = custFont2,
                    )
                        


                        },
                
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown
                )
            )

        },

        bottomBar = {
            NavigationBar(
                modifier = Modifier.background(DarkBrown)
                    .fillMaxWidth(),
                )
            {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label,
                                tint = DarkBrown

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


    ){innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .offset(y = 50.dp)
        ,

        horizontalAlignment =  Alignment.CenterHorizontally

    ) {


        // Circular Image with Click Handler
        Image(
            painter = painterResource(defImage),
            contentDescription = "User  profile logo",
            modifier = Modifier
                .offset(y = 85.dp)
                .size(150.dp) // Set size for the image
                .clip(CircleShape)
                .border(2.dp, color = Color.Black , CircleShape)
                .clickable { galleryLauncher.launch("image") } ,
            contentScale = ContentScale.Crop,


        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
           modifier =  Modifier
               .width(180.dp)
               .offset(y = 30.dp)
               .scale(1.3f)
            ,
            contentAlignment = Alignment.Center,

        ){
            TextField(
                modifier = Modifier.offset(y = 70.dp),
                value = sharedViewModel.username.value,
                onValueChange = { sharedViewModel.username.value = it },
                label = {
                    Text(
                        text = "Username",
                        modifier = Modifier.offset(x = 40.dp) ,
                        fontSize = 15.sp

                    ) } )
        }


        Spacer(modifier = Modifier.height(70.dp))

//CARD SECTION FOR LIKED PETS
        Card(
            modifier = Modifier
                .padding(10.dp)
                .width(200.dp)
                .offset(y = 90.dp)
                .height(170.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBrown),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),

            ) {

            Text(
                modifier = Modifier
                    .offset(x = 13.dp)
                ,
                text = "Liked Pets ",
                 style = MaterialTheme.typography.titleMedium,
                fontSize = 35.sp,
                color = Color.White,

                fontWeight = FontWeight.Bold
            )
            Text(
                text = " ${likedPetsCount}",
                modifier = Modifier.offset(y = 15.dp, x = 65.dp),
                fontSize = 55.sp
            )
           //END CARD SECTION


            Spacer(modifier = Modifier.height(10.dp))
        }
        Image(
            painter = painterResource(id = R.drawable.cat_icon),
            contentDescription = "image",
            modifier = Modifier.offset(x = 15.dp, y = -2.dp)
                .size(80.dp)

        )
        /*Image(
            painter = painterResource(id = R.drawable.paw_print2),
            contentDescription = "image",
            modifier = Modifier
                .offset(x = -90.dp, y = 18.dp)
        ) */


    }

}



@Composable
fun ContentScreen(modifier: Modifier ){


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
