import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.pawmate_ils.NavItem
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ui.theme.PetPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(

) {
    val custFont = FontFamily(
        Font(R.font.custom_font)
    )
    val custFont2 = FontFamily(
        Font(R.font.opensans)
    )



    //FOR ICONS NAVV :)
    val navItemList = listOf(
        NavItem("Home",Icons.Default.Home),
        NavItem("Adopt",Icons.Default.Pets),
        NavItem("Message", Icons.Default.Message)
    )
    var count = remember { mutableIntStateOf(0) }
    var selectedIndex : Int = count.value
    //END

   Scaffold(
       topBar = {
           CenterAlignedTopAppBar(
           modifier = Modifier
               .fillMaxWidth()
               .background(color = PetPink),
                title = {Text(
                    text = "MESSAGE",
                    color = Color.White,
                    fontSize = 35.sp,
                    fontFamily = custFont
                    ) },
               colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                   containerColor = PetPink
               )
           )

       },
       bottomBar = {
           NavigationBar(
                 modifier = Modifier.fillMaxWidth()
           ) {
               navItemList.forEachIndexed {index, navItem ->
                    NavigationBarItem(
                           selected = selectedIndex == index,
                           onClick = {
                               selectedIndex = index
                           },
                           icon = {
                               Icon(
                                   imageVector = navItem.icon,
                                   contentDescription = navItem.label,
                                   tint = PetPink
                               )
                           },
                            label = {
                                Text(
                                    text = navItem.label,
                                    color = Color.Black,
                                    fontFamily = custFont2
                                )
                            }

                    )
               }

           }
       }
   ){innerPadding ->
       ContentScreen(modifier = Modifier.padding(innerPadding))
   }
    LazyColumn(
             modifier = Modifier.fillMaxSize()
    ) {

    }
}

@Composable
fun ContentScreen(modifier: Modifier){

}

    @Preview(showBackground = true, apiLevel = 34)

    @Composable
    fun MessageScreenPreview() {
        MessageScreen()
    }

