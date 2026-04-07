package com.example.pawmate_ils.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation.NavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.ui.components.AdopterBottomBar
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.util.Locale

data class MerchandiseItem(
    val imageRes: Int,
    val name: String,
    val category: String,
    val price: String,
    val badge: String,
    val description: String
)

private data class CartEntry(
    val item: MerchandiseItem,
    val quantity: Int
)

private fun resolveDrawableId(context: android.content.Context, name: String): Int {
    val packageName = context.packageName
    val candidates = listOf(name, "a_$name", "ic_$name", "img_$name")
    for (candidate in candidates) {
        val resId = context.resources.getIdentifier(candidate, "drawable", packageName)
        if (resId != 0) return resId
    }
    return 0
}

private fun buildShopItems(context: android.content.Context): List<MerchandiseItem> {
    val hiRefs = (1..8).mapNotNull { index ->
        val id = resolveDrawableId(context, "hi$index")
        if (id != 0) id else null
    }

    if (hiRefs.isNotEmpty()) {
        val names = listOf(
            "PawMate Graphic Tee", "PawMate Collectible Figure", "PawMate Insulated Mug",
            "PawMate Hoodie", "PawMate Keychain", "PawMate Tote Bag",
            "PawMate Sticker Pack", "PawMate Desk Figurine"
        )
        val categories = listOf(
            "Apparel", "Collectibles", "Drinkware", "Apparel",
            "Accessories", "Accessories", "Accessories", "Collectibles"
        )
        val prices = listOf(
            "PHP 250.00", "PHP 450.00", "PHP 250.00", "PHP 699.00",
            "PHP 149.00", "PHP 199.00", "PHP 99.00", "PHP 399.00"
        )
        return hiRefs.mapIndexed { i, image ->
            MerchandiseItem(
                imageRes = image,
                name = names.getOrElse(i) { "PawMate Item ${i + 1}" },
                category = categories.getOrElse(i) { "Accessories" },
                price = prices.getOrElse(i) { "PHP 199.00" },
                badge = "New",
                description = "Official PawMate merchandise item."
            )
        }
    }

    return listOf(
        MerchandiseItem(R.drawable.dog1, "PawMate Graphic Tee", "Apparel", "PHP 250.00", "New", "Soft cotton shirt with PawMate print."),
        MerchandiseItem(R.drawable.shitzu, "PawMate Insulated Mug", "Drinkware", "PHP 250.00", "New", "Daily-use mug with insulated lining."),
        MerchandiseItem(R.drawable.chow, "PawMate Figurine", "Collectibles", "PHP 450.00", "New", "Desk collectible for PawMate fans."),
        MerchandiseItem(R.drawable.dog1, "PawMate Hoodie", "Apparel", "PHP 699.00", "Popular", "Premium fleece hoodie for cool weather.")
    )
}

private fun discountedPriceLabel(priceLabel: String, promoApplied: Boolean): String {
    if (!promoApplied) return priceLabel
    val amount = Regex("""\d+(?:\.\d{1,2})?""").find(priceLabel)?.value?.toDoubleOrNull() ?: return priceLabel
    return String.format(Locale.US, "PHP %.2f", amount * 0.8)
}

private fun parsePrice(priceLabel: String): Double {
    return Regex("""\d+(?:\.\d{1,2})?""").find(priceLabel)?.value?.toDoubleOrNull() ?: 0.0
}

private fun formatPhp(amount: Double): String = String.format(Locale.US, "PHP %.2f", amount)

private fun addOrMergeEntry(entries: MutableList<CartEntry>, product: MerchandiseItem, quantity: Int) {
    val existingIndex = entries.indexOfFirst { it.item.name == product.name }
    if (existingIndex >= 0) {
        val existing = entries[existingIndex]
        entries[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
    } else {
        entries.add(CartEntry(product, quantity))
    }
}

private fun saveCart(context: Context, cartItems: List<CartEntry>) {
    val sharedPrefs = context.getSharedPreferences("pawmate_cart", Context.MODE_PRIVATE)
    val json = Gson().toJson(cartItems)
    sharedPrefs.edit().putString("cart_data", json).apply()
}

private fun saveHistory(context: Context, items: List<CartEntry>) {
    val prefs = context.getSharedPreferences("pawmate_shop_prefs", Context.MODE_PRIVATE)
    val json = Gson().toJson(items)
    prefs.edit().putString("history_json", json).apply()
}
private fun loadCart(context: Context): List<CartEntry> {
    val sharedPrefs = context.getSharedPreferences("pawmate_cart", Context.MODE_PRIVATE)
    val json = sharedPrefs.getString("cart_data", null) ?: return emptyList()
    // Use the standard Gson TypeToken instead of the Firebase one
    val type = object : com.google.gson.reflect.TypeToken<List<CartEntry>>() {}.type
    return Gson().fromJson(json, type)
}

private fun loadHistory(context: Context): List<CartEntry> {
    val prefs = context.getSharedPreferences("pawmate_shop_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("history_json", null) ?: return emptyList()
    val type = object : com.google.gson.reflect.TypeToken<List<CartEntry>>() {}.type
    return Gson().fromJson(json, type)
}

@Composable
private fun ShopCartOverlay(
    isDarkMode: Boolean,
    cartItems: SnapshotStateList<CartEntry>,
    boughtItems: List<CartEntry>,
    promoApplied: Boolean,
    onDismiss: () -> Unit,
    onOrderNow: (totalFormatted: String, checkoutIndices: List<Int>) -> Unit,
) {
    val pink = if (isDarkMode) Color(0xFFFF7BA1) else Color(0xFFE84D7A)
    val pinkSoft = if (isDarkMode) Color(0xFFFFB3C9) else Color(0xFFFFE4EE)
    val pageBg = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val surface = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val barBg = if (isDarkMode) Color(0xFF252525) else Color.White
    val onSurface = if (isDarkMode) Color(0xFFF5F5F5) else Color(0xFF1A1A1A)
    val muted = if (isDarkMode) Color(0xFFAEAEAE) else Color(0xFF7A7377)
    val lineChecked = remember { mutableStateListOf<Boolean>() }
    val cartSignature = cartItems.joinToString("|") { "${it.item.name}@${it.quantity}" }

    LaunchedEffect(cartSignature) {
        lineChecked.clear()
        repeat(cartItems.size) { lineChecked.add(true) }
    }

    val selectedIndices = cartItems.indices.filter { i -> lineChecked.getOrElse(i) { true } }
    val selectedCount = selectedIndices.size
    val selectedTotal = selectedIndices.sumOf { i ->
        val e = cartItems[i]
        val unit = if (promoApplied) parsePrice(e.item.price) * 0.8 else parsePrice(e.item.price)
        unit * e.quantity
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = pageBg) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Surface(color = barBg, shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = onSurface
                            )
                        }
                        Text(
                            text = "My Cart",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = onSurface
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFE8E8E8),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = muted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (cartItems.isEmpty() && boughtItems.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = muted.copy(alpha = 0.45f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Your cart is empty", fontWeight = FontWeight.SemiBold, color = onSurface)
                                Text(
                                    "Add PawMate goodies from the shop.",
                                    fontSize = 13.sp,
                                    color = muted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    items(cartItems.size) { index ->
                        val entry = cartItems[index]
                        val unit = if (promoApplied) parsePrice(entry.item.price) * 0.8 else parsePrice(entry.item.price)
                        val subtitle = "${entry.item.category} · ${entry.item.badge}"
                        val checked = lineChecked.getOrElse(index) { true }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        while (lineChecked.size <= index) lineChecked.add(true)
                                        lineChecked[index] = it
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = pink,
                                        uncheckedColor = muted.copy(alpha = 0.6f),
                                        checkmarkColor = Color.White
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 40.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFF0F0F0)
                                        )
                                    ) {
                                        Image(
                                            painter = painterResource(id = entry.item.imageRes),
                                            contentDescription = entry.item.name,
                                            modifier = Modifier.size(72.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            entry.item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            subtitle,
                                            fontSize = 12.sp,
                                            color = muted,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            formatPhp(unit),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pink,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(top = 82.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = pinkSoft,
                                        onClick = {
                                            val e = cartItems[index]
                                            val n = e.quantity - 1
                                            if (n <= 0) cartItems.removeAt(index)
                                            else cartItems[index] = e.copy(quantity = n)
                                        }
                                    ) {
                                        Text(
                                            "−",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = pink,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Text(
                                        "${entry.quantity} pc",
                                        fontWeight = FontWeight.SemiBold,
                                        color = onSurface,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = pinkSoft,
                                        onClick = {
                                            val e = cartItems[index]
                                            cartItems[index] = e.copy(quantity = e.quantity + 1)
                                        }
                                    ) {
                                        Text(
                                            "+",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = pink,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (boughtItems.isNotEmpty()) {
                        val recentPurchases = boughtItems.takeLast(5)
                        item {
                            Text(
                                "Recent purchases",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = muted,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            count = recentPurchases.size,
                            key = { recentPurchases[it].item.name + "_" + it }
                        ) { i ->
                            val entry = recentPurchases[i]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = entry.item.imageRes),
                                        contentDescription = entry.item.name,
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            entry.item.name,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 13.sp,
                                            color = onSurface
                                        )
                                        Text(
                                            "× ${entry.quantity}",
                                            fontSize = 11.sp,
                                            color = muted
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                    color = surface,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(top = 16.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Select", color = muted, fontSize = 14.sp)
                            Text(
                                selectedCount.toString(),
                                color = muted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = pink
                            )
                            Text(
                                formatPhp(selectedTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = pink
                            )
                        }
                        Button(
                            onClick = {
                                if (selectedIndices.isNotEmpty()) {
                                    onOrderNow(formatPhp(selectedTotal), selectedIndices)
                                }
                            },
                            enabled = selectedIndices.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = pink,
                                disabledContainerColor = muted.copy(alpha = 0.35f),
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text("Order Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopProductDetailOverlay(
    product: MerchandiseItem,
    isDarkMode: Boolean,
    promoApplied: Boolean,
    promoInput: String,
    onPromoInputChange: (String) -> Unit,
    onApplyPromo: () -> Unit,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
) {
    val pink = Color(0xFFC16565)
    val pinkSoft = Color(0xFFFFE4EE)
    val pageBg = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val surface = if (isDarkMode) Color(0xFF262224) else Color(0xFFFFFBFC)
    val onSurface = if (isDarkMode) Color(0xFFF8F0F3) else Color(0xFF3D2C32)
    val muted = if (isDarkMode) Color(0xFFB0A8AB) else Color(0xFF8A7A80)
    val imageCardBg = if (isDarkMode) Color(0xFF2E2629) else Color(0xFFFFF8FA)
    val imageInnerBg = if (isDarkMode) Color(0xFF33292D) else pinkSoft
    var descriptionExpanded by remember(product.name) { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = pageBg) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = pink
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = imageCardBg),
                        border = BorderStroke(1.dp, pink.copy(alpha = if (isDarkMode) 0.22f else 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(imageInnerBg)
                        ) {
                            Image(
                                painter = painterResource(id = product.imageRes),
                                contentDescription = product.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(288.dp)
                                    .padding(12.dp),
                                contentScale = ContentScale.Fit
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 18.dp, top = 72.dp, bottom = 72.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                repeat(3) { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == 0) 8.dp else 7.dp)
                                            .background(
                                                color = if (i == 0) pink else muted.copy(alpha = 0.35f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = product.category.uppercase(Locale.US),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pink
                    )
                    Text(
                        text = product.name.uppercase(Locale.US),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        lineHeight = 25.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp)
                            .clickable { descriptionExpanded = !descriptionExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = pink
                        )
                        Icon(
                            imageVector = if (descriptionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = pink.copy(alpha = 0.85f)
                        )
                    }
                    if (descriptionExpanded) {
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = muted,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) pink.copy(alpha = 0.18f) else pinkSoft
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                product.category,
                                color = if (isDarkMode) Color(0xFFFFB3BA) else Color(0xFF9E3D52),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                product.badge,
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = onPromoInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("P", color = pink, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Promo code") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkMode) surface else Color(0xFFFFEEF2),
                            unfocusedContainerColor = if (isDarkMode) surface else Color(0xFFFFF5F8),
                            focusedBorderColor = pink,
                            unfocusedBorderColor = pink.copy(alpha = 0.4f),
                            cursorColor = pink,
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            focusedPlaceholderColor = muted,
                            unfocusedPlaceholderColor = muted
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onApplyPromo() })
                    )
                    TextButton(
                        onClick = onApplyPromo,
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Apply code", color = pink, fontWeight = FontWeight.SemiBold) }
                    Text(
                        if (promoApplied) "Promo applied — 20% off." else "Try PROMO1 for 20% off.",
                        fontSize = 12.sp,
                        color = if (promoApplied) Color(0xFF66BB6A) else muted
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Quantity", fontWeight = FontWeight.Medium, color = pink, fontSize = 14.sp)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) pink.copy(alpha = 0.14f) else pinkSoft.copy(alpha = 0.65f)
                            ),
                            border = BorderStroke(1.dp, pink.copy(alpha = 0.25f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                TextButton(
                                    onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = pink)
                                }
                                Text(
                                    quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                TextButton(
                                    onClick = { onQuantityChange(quantity + 1) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = pink)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(96.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) surface else Color(0xFFFFF7F9)
                    ),
                    border = BorderStroke(1.dp, pink.copy(alpha = if (isDarkMode) 0.2f else 0.14f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 14.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Price", fontSize = 12.sp, color = muted)
                                Text(
                                    discountedPriceLabel(product.price, promoApplied),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pink
                                )
                                if (promoApplied) {
                                    Text(
                                        product.price,
                                        fontSize = 12.sp,
                                        color = muted,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        if (isDarkMode) pink.copy(alpha = 0.15f) else pinkSoft,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = pink
                                )
                            }
                            Button(
                                onClick = onAddToCart,
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pink,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    "ADD TO CART",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onBuyNow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, pink),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = pink,
                                containerColor = if (isDarkMode) Color.Transparent else pinkSoft.copy(alpha = 0.35f)
                            )
                        ) {
                            Text("Buy now · GCash", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopScreen(navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val categories = listOf("All", "Apparel", "Drinkware", "Accessories", "Collectibles")
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedProduct by remember { mutableStateOf<MerchandiseItem?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var appliedPromoCode by remember { mutableStateOf("") }
    var promoStatusMessage by remember { mutableStateOf<String?>(null) }
    val promoApplied = appliedPromoCode.trim().equals("PROMO1", ignoreCase = true)
    val cartItems = remember {
        mutableStateListOf<CartEntry>().apply {
            addAll(loadCart(context))
        }
    }
    val boughtItems = remember {
        mutableStateListOf<CartEntry>().apply {
            addAll(loadHistory(context))
        }
    }
    val cartCount = cartItems.sumOf { it.quantity }

    // --- 🟢 NEW GCash Transaction States ---
    var showGCashFlow by remember { mutableStateOf(false) }
    var pendingTotalAmount by remember { mutableStateOf("PHP 0.00") }
    var gcashPurchaseAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showShopConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(cartItems.size, cartItems.sumOf { it.quantity }) {
        saveCart(context, cartItems.toList())
    }
    LaunchedEffect(boughtItems.size) { saveHistory(context, boughtItems.toList()) }


    fun applyPromoCode() {
        val normalized = promoInput.trim().uppercase(Locale.US)
        when {
            normalized.isEmpty() -> { appliedPromoCode = ""; promoStatusMessage = "Enter a promo code first." }
            normalized == "PROMO1" -> { appliedPromoCode = normalized; promoStatusMessage = "Promo applied: 20% off." }
            else -> { appliedPromoCode = ""; promoStatusMessage = "Invalid promo code." }
        }
    }

    val items = remember { buildShopItems(context) }
    val filtered = items.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (search.isBlank() || it.name.contains(search, ignoreCase = true))
    }



    Box(modifier = Modifier.fillMaxSize().background(backgroundColor).statusBarsPadding()) {
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { AdopterBottomBar(navController = navController, selectedTab = "Shop") }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(
                    start = if (isTablet) 24.dp else 10.dp,
                    end = if (isTablet) 24.dp else 10.dp,
                    top = if (isTablet) 16.dp else 8.dp,
                    bottom = 92.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text("Search..") },
                            shape = RoundedCornerShape(26.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cardColor,
                                unfocusedContainerColor = cardColor
                            ),
                            singleLine = true
                        )
                        IconButton(
                            onClick = { selectedCategory = "All" },
                            modifier = Modifier.size(44.dp).background(cardColor, CircleShape)
                        ) { Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF6A6A6A)) }
                    }
                }

                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().height(if (isTablet) 140.dp else 110.dp).background(Brush.horizontalGradient(listOf(Color(0xFFDCCBFF), Color(0xFFD8F2FF), Color(0xFFF7DDFD))), RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Column(modifier = Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "50% OFF", fontSize = if (isTablet) 46.sp else 42.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9A8AD8))
                                Text(text = "Limited PawMate merch deals", fontSize = if (isTablet) 14.sp else 12.sp, color = Color(0xFF7D77CF))
                            }
                            Text(text = "💎 GEMS", fontSize = if (isTablet) 30.sp else 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7D77CF), modifier = Modifier.align(Alignment.CenterEnd))
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { category ->
                            val selected = selectedCategory == category
                            AssistChip(onClick = { selectedCategory = category }, label = { Text(if (category == "All") "New" else category) }, colors = AssistChipDefaults.assistChipColors(containerColor = if (selected) Color(0xFFFFCDD9) else Color(0xFFF7E6EA), labelColor = Color(0xFFC56D7D)))
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = promoInput, onValueChange = { promoInput = it }, modifier = Modifier.weight(1f), singleLine = true, leadingIcon = { Text("P", color = Color(0xFFC16565), fontWeight = FontWeight.Bold) }, placeholder = { Text("Promo code (use PROMO1)") }, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = cardColor, unfocusedContainerColor = cardColor), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { applyPromoCode(); focusManager.clearFocus() }))
                            Button(onClick = { applyPromoCode(); focusManager.clearFocus() }, colors = ButtonDefaults.buttonColors(containerColor = if (promoApplied) Color(0xFF4CAF50) else Color(0xFFC16565)), shape = RoundedCornerShape(10.dp)) { Text(text = if (promoApplied) "APPLIED" else "ENTER", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                        promoStatusMessage?.let { msg -> Text(text = msg, color = if (promoApplied) Color(0xFF4CAF50) else Color(0xFFC16565), fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    }
                }

                items(filtered.chunked(if (isTablet) 3 else 2)) { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { product ->
                            Card(modifier = Modifier.weight(1f).clickable { selectedProduct = product }, colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC9BF)), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))) {
                                        Image(painter = painterResource(id = product.imageRes), contentDescription = product.name, modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentScale = ContentScale.Fit)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = product.name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (promoApplied) {
                                        Text(text = product.price, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, textDecoration = TextDecoration.LineThrough)
                                        Text(text = discountedPriceLabel(product.price, true), color = Color(0xFFDB3049), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else { Text(text = product.price, color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp) }
                                }
                            }
                        }
                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 102.dp)) {
            Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = Color(0xFFE68F8F)), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                IconButton(onClick = { showCartDialog = true }, modifier = Modifier.size(52.dp)) { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White) }
            }
            if (cartCount > 0) {
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(20.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = cartCount.coerceAtMost(99).toString(), color = Color(0xFFE68F8F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showCartDialog) {
            ShopCartOverlay(
                isDarkMode = isDarkMode,
                cartItems = cartItems,
                boughtItems = boughtItems.toList(),
                promoApplied = promoApplied,
                onDismiss = { showCartDialog = false },
                onOrderNow = { totalFormatted, checkoutIndices ->
                    pendingTotalAmount = totalFormatted
                    gcashPurchaseAction = {
                        checkoutIndices.sortedDescending().forEach { idx ->
                            val line = cartItems[idx]
                            addOrMergeEntry(boughtItems, line.item, line.quantity)
                            cartItems.removeAt(idx)
                        }
                        saveCart(context, cartItems.toList())
                        saveHistory(context, boughtItems.toList())
                    }
                    showCartDialog = false
                    showShopConfirmation = true
                }
            )
        }

        selectedProduct?.let { product ->
            var quantity by remember(product) { mutableStateOf(1) }
            ShopProductDetailOverlay(
                product = product,
                isDarkMode = isDarkMode,
                promoApplied = promoApplied,
                promoInput = promoInput,
                onPromoInputChange = { promoInput = it },
                onApplyPromo = { applyPromoCode(); focusManager.clearFocus() },
                quantity = quantity,
                onQuantityChange = { quantity = it },
                onDismiss = { selectedProduct = null },
                onAddToCart = {
                    addOrMergeEntry(cartItems, product, quantity)
                    selectedProduct = null
                },
                onBuyNow = {
                    val priceNum = parsePrice(product.price)
                    val finalPrice = if (promoApplied) priceNum * 0.8 else priceNum
                    pendingTotalAmount = formatPhp(finalPrice * quantity)
                    gcashPurchaseAction = { addOrMergeEntry(boughtItems, product, quantity) }
                    selectedProduct = null
                    showShopConfirmation = true
                }
            )
        }
        if (showShopConfirmation) {
            AlertDialog(
                onDismissRequest = { showShopConfirmation = false },
                containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
                title = { Text("Confirm Purchase", fontWeight = FontWeight.Bold, color = Color(0xFFC16565)) },
                text = {
                    Text(
                        "Are you sure you want to proceed with the payment of $pendingTotalAmount?",
                        color = if(isDarkMode) Color.White else Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showShopConfirmation = false
                            showGCashFlow = true // 🚀 Launch GCash ONLY after this confirmation
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC16565))
                    ) {
                        Text("Proceed to GCash", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShopConfirmation = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }





        if (showGCashFlow) {
            ShopGCashDialog(
                totalAmount = pendingTotalAmount,
                onDismiss = { showGCashFlow = false },
                onPaymentVerified = {
                    gcashPurchaseAction?.invoke()
                    showGCashFlow = false
                    android.widget.Toast.makeText(context, "Order Successful! Added to history.", android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun ShopGCashDialog(totalAmount: String, onDismiss: () -> Unit, onPaymentVerified: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    val isDarkMode = ThemeManager.isDarkMode
    val pawMatePink = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)

    AlertDialog(
        onDismissRequest = { if (currentStep != 2) onDismiss() },
        containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
        title = { Text(text = when(currentStep) { 1 -> "GCash Scan to Pay"; 2 -> "Verifying Payment..."; else -> "Success!" }, fontWeight = FontWeight.Bold, color = pawMatePink) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when (currentStep) {
                    1 -> {
                        Text("Total Amount: $totalAmount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if(isDarkMode) Color.White else Color.Black)
                        Spacer(Modifier.height(12.dp))
                        Icon(Icons.Default.QrCodeScanner, null, Modifier.size(160.dp), tint = pawMatePink.copy(alpha = 0.7f))
                        Text("Scan this QR with GCash App", fontSize = 12.sp, color = Color.Gray)
                        Text("Ref: PM-${System.currentTimeMillis().toString().takeLast(6)}", fontSize = 10.sp, color = Color.Gray)
                    }
                    2 -> { LaunchedEffect(Unit) { delay(3000); currentStep = 3 }; CircularProgressIndicator(color = pawMatePink); Spacer(Modifier.height(12.dp)); Text("Checking transaction status...", color = if(isDarkMode) Color.White else Color.Black) }
                    3 -> { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(60.dp)); Text("Payment Received!", fontWeight = FontWeight.Bold, color = if(isDarkMode) Color.White else Color.Black); Text("Processing your order.", fontSize = 12.sp, color = Color.Gray) }
                }
            }
        },
        confirmButton = { Button(onClick = { if (currentStep == 1) currentStep = 2 else if (currentStep == 3) onPaymentVerified() }, colors = ButtonDefaults.buttonColors(containerColor = pawMatePink), shape = RoundedCornerShape(20.dp)) { Text(if (currentStep == 3) "Done" else "I have paid") } },
        dismissButton = { if (currentStep == 1) { TextButton(onClick = onDismiss) { Text("Cancel", color = if(isDarkMode) Color.LightGray else Color.Gray) } } }
    )
}