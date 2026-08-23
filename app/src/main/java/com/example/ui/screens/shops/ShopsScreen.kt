package com.example.ui.screens.shops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.ShopDialog
import com.example.ui.components.TransactionCardItem
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.HoldAmber
import com.example.ui.theme.ReturnGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(
    shops: List<Shop>,
    transactions: List<Transaction>,
    onAddShop: (shopName: String, ownerName: String, phone: String, address: String, gstNumber: String, notes: String) -> Unit,
    onUpdateShop: (Shop) -> Unit,
    onDeleteShop: (Shop) -> Unit,
    onShopClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddShopDialog by remember { mutableStateOf(false) }
    var editingShop by remember { mutableStateOf<Shop?>(null) }

    val filteredShops = remember(shops, searchQuery) {
        if (searchQuery.isBlank()) shops
        else shops.filter {
            it.shopName.contains(searchQuery, ignoreCase = true) ||
                    it.ownerName.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("கடை பெயர், உரிமையாளர் அல்லது தொலைபேசி மூலம் தேடுங்கள்...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredShops.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "கடைகள் இன்னும் சேர்க்கப்படவில்லை." else "'$searchQuery' பொருந்தும் கடைகள் எதுவும் இல்லை",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filteredShops) { index, shop ->
                        // Compute shop metrics
                        val shopTxs = transactions.filter { it.shopId == shop.id }
                        var del = 0.0
                        var ret = 0.0
                        shopTxs.forEach { tx ->
                            val type = TransactionType.fromLabel(tx.type)
                            if (type.isDeliveryType) del += tx.pureWeight
                            else if (type.isReturnType) ret += tx.pureWeight
                        }
                        val hold = Math.round((del - ret) * 10.0) / 10.0
                        val roundedDel = Math.round(del * 10.0) / 10.0
                        val roundedRet = Math.round(ret * 10.0) / 10.0

                        ShopCardItem(
                            index = index + 1,
                            shop = shop,
                            delivery = roundedDel,
                            returnWeight = roundedRet,
                            hold = hold,
                            txCount = shopTxs.size,
                            onClick = { onShopClick(shop.id) },
                            onEdit = { editingShop = shop },
                            onDelete = { onDeleteShop(shop) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddShopDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
        }
    }

    if (showAddShopDialog) {
        ShopDialog(
            onDismiss = { showAddShopDialog = false },
            onSave = { name, owner, phone, address, gst, notes ->
                onAddShop(name, owner, phone, address, gst, notes)
            }
        )
    }

    if (editingShop != null) {
        ShopDialog(
            existingShop = editingShop,
            onDismiss = { editingShop = null },
            onSave = { name, owner, phone, address, gst, notes ->
                val updated = editingShop!!.copy(
                    shopName = name,
                    ownerName = owner,
                    phone = phone,
                    address = address,
                    gstNumber = gst,
                    notes = notes
                )
                onUpdateShop(updated)
                editingShop = null
            }
        )
    }
}

@Composable
fun ShopCardItem(
    index: Int,
    shop: Shop,
    delivery: Double,
    returnWeight: Double,
    hold: Double,
    txCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$index.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shop.shopName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Shop",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Shop",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "உரிமையாளர்: ${shop.ownerName}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shop.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            if (shop.address.isNotBlank()) {
                Text(
                    text = shop.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Pure Weight Balance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("கொடுத்தல்", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$delivery g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                }

                Column {
                    Text("வரவு", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$returnWeight g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ReturnGreen)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("தற்போதைய இருப்பு", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$hold g", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = HoldAmber)
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("கடையை நீக்கலாமா?") },
            text = { Text("${shop.shopName}-ஐ நிச்சயமாக நீக்க விரும்புகிறீர்களா? தொடர்புடைய பரிவர்த்தனைகள் வரலாற்றில் இருக்கும்.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showConfirmDelete = false
                }) {
                    Text("நீக்கு", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("ரத்து செய்")
                }
            }
        )
    }
}

@Composable
fun ShopDetailScreen(
    shopId: Long,
    shops: List<Shop>,
    products: List<Product> = emptyList(),
    transactions: List<Transaction>,
    onUpdateShop: (Shop) -> Unit,
    onAddTransaction: (
        date: String,
        time: String,
        shopId: Long,
        shopName: String,
        type: String,
        weight: Double,
        touch: Double,
        touchAdjustment: Double,
        remarks: String,
        imageUri: String
    ) -> Unit,
    onUpdateTransaction: (
        id: Long,
        date: String,
        time: String,
        shopId: Long,
        shopName: String,
        type: String,
        weight: Double,
        touch: Double,
        touchAdjustment: Double,
        remarks: String,
        imageUri: String
    ) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shop = remember(shops, shopId) { shops.find { it.id == shopId } }
    val shopTxs = remember(transactions, shopId) { 
        transactions.filter { it.shopId == shopId }
            .sortedWith(compareBy<Transaction> { it.date }.thenBy { it.time })
    }

    var showEditShopDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    var del = 0.0
    var ret = 0.0
    shopTxs.forEach { tx ->
        val type = TransactionType.fromLabel(tx.type)
        if (type.isDeliveryType) del += tx.pureWeight
        else if (type.isReturnType) ret += tx.pureWeight
    }
    val hold = Math.round((del - ret) * 10.0) / 10.0
    val roundedDel = Math.round(del * 10.0) / 10.0
    val roundedRet = Math.round(ret * 10.0) / 10.0

    if (shop == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("கடை கிடைக்கவில்லை")
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shop.shopName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { showEditShopDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Shop Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Shop Detail Summary Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("Owner: ${shop.ownerName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Phone: ${shop.phone}", style = MaterialTheme.typography.bodyMedium)
                    if (shop.address.isNotBlank()) Text("Address: ${shop.address}", style = MaterialTheme.typography.bodySmall)
                    if (shop.gstNumber.isNotBlank()) Text("GST: ${shop.gstNumber}", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("கொடுத்தல்", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$roundedDel g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("வரவு", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$roundedRet g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ReturnGreen)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("இருப்பு", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$hold g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = HoldAmber)
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "கடை பரிவர்த்தனை பட்டியல்",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (shopTxs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("இந்த கடைக்கு இன்னும் பரிவர்த்தனைகள் இல்லை.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(shopTxs) { index, tx ->
                        TransactionCardItem(
                            index = index + 1,
                            tx = tx,
                            onEdit = { editingTransaction = tx },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddTxDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (showEditShopDialog) {
        ShopDialog(
            existingShop = shop,
            onDismiss = { showEditShopDialog = false },
            onSave = { name, owner, phone, address, gst, notes ->
                val updated = shop.copy(
                    shopName = name,
                    ownerName = owner,
                    phone = phone,
                    address = address,
                    gstNumber = gst,
                    notes = notes
                )
                onUpdateShop(updated)
                showEditShopDialog = false
            }
        )
    }

    if (showAddTxDialog) {
        com.example.ui.components.TransactionDialog(
            shops = shops,
            products = products,
            onDismiss = { showAddTxDialog = false },
            onSave = { date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri ->
                onAddTransaction(date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri)
            }
        )
    }

    if (editingTransaction != null) {
        com.example.ui.components.TransactionDialog(
            shops = shops,
            products = products,
            existingTransaction = editingTransaction,
            onDismiss = { editingTransaction = null },
            onSave = { date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri ->
                val txId = editingTransaction!!.id
                onUpdateTransaction(txId, date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri)
                editingTransaction = null
            }
        )
    }
}
