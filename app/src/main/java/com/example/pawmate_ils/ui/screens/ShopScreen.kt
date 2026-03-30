package com.example.pawmate_ils.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CheckCircle
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

    LaunchedEffect(cartItems.size) { saveCart(context, cartItems.toList()) }
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



    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
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
                                    Text(text = product.name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
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
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFC16565)); Text("My Cart", fontWeight = FontWeight.Bold); if (cartItems.isNotEmpty()) { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFC16565)), shape = RoundedCornerShape(12.dp)) { Text(text = "$cartCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) } } } },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (cartItems.isEmpty() && boughtItems.isEmpty()) { Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.4f)); Spacer(modifier = Modifier.height(8.dp)); Text("Your cart is empty", fontWeight = FontWeight.Medium, color = Color.Gray); Text("Add items from the product list.", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f)) } }
                        if (cartItems.isNotEmpty()) {
                            cartItems.forEach { entry ->
                                val discountedUnit = if (promoApplied) parsePrice(entry.item.price) * 0.8 else parsePrice(entry.item.price)
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F8)), shape = RoundedCornerShape(12.dp)) {
                                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))) { Image(painter = painterResource(id = entry.item.imageRes), contentDescription = entry.item.name, modifier = Modifier.size(56.dp), contentScale = ContentScale.Crop) }
                                        Column(modifier = Modifier.weight(1f)) { Text(entry.item.name, fontWeight = FontWeight.SemiBold, maxLines = 1, fontSize = 14.sp); Text(entry.item.category, color = Color.Gray, fontSize = 11.sp) }
                                        Column(horizontalAlignment = Alignment.End) { Text("x${entry.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(formatPhp(discountedUnit * entry.quantity), color = Color(0xFFC16565), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            val cartTotalValue = cartItems.sumOf { (if (promoApplied) parsePrice(it.item.price) * 0.8 else parsePrice(it.item.price)) * it.quantity }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", fontWeight = FontWeight.Medium); Text(formatPhp(cartTotalValue), fontWeight = FontWeight.ExtraBold, color = Color(0xFFC16565)) }
                        }
                        if (boughtItems.isNotEmpty()) { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)); Text("Recent purchases", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Gray); boughtItems.takeLast(5).forEach { entry -> Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), shape = RoundedCornerShape(10.dp)) { Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))) { Image(painter = painterResource(id = entry.item.imageRes), contentDescription = entry.item.name, modifier = Modifier.size(40.dp), contentScale = ContentScale.Crop) }; Column(modifier = Modifier.weight(1f)) { Text(entry.item.name, fontWeight = FontWeight.Medium, maxLines = 1, fontSize = 13.sp); Text("x${entry.quantity}", color = Color.Gray, fontSize = 11.sp) } } } } }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showCartDialog = false }) { Text("Close") }
                        if (cartItems.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val totalVal = cartItems.sumOf { (if (promoApplied) parsePrice(it.item.price) * 0.8 else parsePrice(it.item.price)) * it.quantity }
                                    pendingTotalAmount = formatPhp(totalVal)
                                    gcashPurchaseAction = { cartItems.forEach { addOrMergeEntry(boughtItems, it.item, it.quantity) }; cartItems.clear() }
                                    showCartDialog = false
                                    showShopConfirmation = true
                                    saveCart(context, emptyList()) // Force clear save
                                    saveHistory(context, boughtItems.toList())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC16565))
                            ) { Text("Checkout", color = Color.White) }
                        }
                    }
                }
            )
        }

        selectedProduct?.let { product ->
            var quantity by remember(product) { mutableStateOf(1) }
            AlertDialog(
                onDismissRequest = { selectedProduct = null },
                title = { Text(product.name, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F4F4))) { Image(painter = painterResource(id = product.imageRes), contentDescription = product.name, modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Fit) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD9)), shape = RoundedCornerShape(8.dp)) { Text(product.category, color = Color(0xFFC16565), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) }; Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(8.dp)) { Text(product.badge, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) } } }
                            Text(discountedPriceLabel(product.price, promoApplied), color = Color(0xFFC16565), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        if (promoApplied) { Text(product.price, color = Color.Gray, fontSize = 12.sp, textDecoration = TextDecoration.LineThrough) }
                        Text(product.description, color = Color.DarkGray, fontSize = 14.sp)
                        OutlinedTextField(value = promoInput, onValueChange = { promoInput = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Text("P", color = Color(0xFFC16565), fontWeight = FontWeight.Bold) }, placeholder = { Text("Enter promo code") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFFF5F8), unfocusedContainerColor = Color(0xFFFFF5F8)), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { applyPromoCode(); focusManager.clearFocus() }))
                        Text(if (promoApplied) "Promo applied: 20% discount enabled." else "Use PROMO1 for 20% off.", fontSize = 12.sp, color = if (promoApplied) Color(0xFF4CAF50) else Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Text("Qty", fontWeight = FontWeight.Medium); Button(onClick = { if (quantity > 1) quantity -= 1 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD9))) { Text("-", color = Color(0xFF9A4E5F)) }; Text(quantity.toString(), fontWeight = FontWeight.Bold); Button(onClick = { quantity += 1 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD9))) { Text("+", color = Color(0xFF9A4E5F)) } }
                    }
                },
                confirmButton = { Button(onClick = { addOrMergeEntry(cartItems, product, quantity); selectedProduct = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9999))) { Text("Add to Cart", color = Color.White) } },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedProduct = null }) { Text("Close") }
                        Button(
                            onClick = {
                                val priceNum = parsePrice(product.price)
                                val finalPrice = if (promoApplied) priceNum * 0.8 else priceNum
                                pendingTotalAmount = formatPhp(finalPrice * quantity)
                                gcashPurchaseAction = { addOrMergeEntry(boughtItems, product, quantity) }
                                selectedProduct = null
                                showShopConfirmation = true
                                },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC16565))
                        ) { Text("Buy Now", color = Color.White) }
                    }
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