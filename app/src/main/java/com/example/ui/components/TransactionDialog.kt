package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.PureWeightCalculator
import com.example.data.model.Shop
import com.example.data.model.TransactionType
import com.example.util.ImageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    shops: List<Shop>,
    products: List<Product> = emptyList(),
    existingTransaction: com.example.data.model.Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (
        date: String,
        shopId: Long,
        shopName: String,
        type: String,
        weight: Double,
        touch: Double,
        touchAdjustment: Double,
        remarks: String,
        imageUri: String,
    ) -> Unit
) {
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var dateText by remember { mutableStateOf(existingTransaction?.date ?: todayDate) }
    var selectedShop by remember {
        mutableStateOf(
            if (existingTransaction != null) {
                shops.find { it.id == existingTransaction.shopId } ?: shops.firstOrNull()
            } else shops.firstOrNull()
        )
    }
    var expandedShopDropdown by remember { mutableStateOf(false) }

    var selectedType by remember {
        mutableStateOf(
            if (existingTransaction != null) {
                TransactionType.fromLabel(existingTransaction.type)
            } else TransactionType.DELIVERY
        )
    }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    var weightText by remember { mutableStateOf(existingTransaction?.weight?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var touchText by remember { mutableStateOf(existingTransaction?.touch?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var touchAdjText by remember { mutableStateOf(existingTransaction?.touchAdjustment?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "0") }
    var remarksText by remember { mutableStateOf(existingTransaction?.remarks ?: "") }
    var imageUris by remember {
        mutableStateOf(
            ImageUtils.parseImageUris(existingTransaction?.imageUri)
        )
    }

    val weightVal = weightText.toDoubleOrNull() ?: 0.0
    val touchVal = touchText.toDoubleOrNull() ?: 0.0
    val touchAdjVal = touchAdjText.toDoubleOrNull() ?: 0.0

    val calculatedPureWeight = remember(weightVal, touchVal, touchAdjVal) {
        PureWeightCalculator.calculate(weightVal, touchVal, touchAdjVal)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = uris.map { it.toString() }
            imageUris = (imageUris + newUris).distinct()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Fixed Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingTransaction == null) "New Transaction" else "Edit Transaction",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                // Date Field
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Transaction Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select Shop Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedShopDropdown,
                    onExpandedChange = { expandedShopDropdown = !expandedShopDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedShop?.shopName ?: "Select Shop",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Shop") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShopDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedShopDropdown,
                        onDismissRequest = { expandedShopDropdown = false }
                    ) {
                        shops.forEach { shop ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(shop.shopName, fontWeight = FontWeight.Bold)
                                        Text(shop.phone, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedShop = shop
                                    expandedShopDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Transaction Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        TransactionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedType = type
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weight & Touch Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (g)") },
                        placeholder = { Text("e.g. 6369") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = touchText,
                        onValueChange = { touchText = it },
                        label = { Text("Touch (%)") },
                        placeholder = { Text("e.g. 76") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = touchAdjText,
                    onValueChange = { touchAdjText = it },
                    label = { Text("Touch Adjustment (%)") },
                    placeholder = { Text("e.g. 0 or 11") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Pure Weight Display Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CALCULATED PURE WEIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$calculatedPureWeight g",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Formula: Pure = Weight × (Touch + Adj) / 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product Mapping Selection (Multiple)
                val availableProductNames = remember(products) {
                    val dbNames = products.asSequence().map { it.productName.trim() }.filter { it.isNotEmpty() }.toList()
                    if (dbNames.isNotEmpty()) dbNames.distinct()
                    else listOf("Payal", "Anklets", "Leg Chain", "Chains", "Rings", "Utensils", "Coins", "Bars", "Kaddiyalu")
                }

                Text(
                    text = "Map Product Name(s):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableProductNames) { pName ->
                        val isSelected = remarksText.contains(pName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val currentItems = remarksText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                if (isSelected) {
                                    currentItems.removeAll { it.equals(pName, ignoreCase = true) }
                                } else {
                                    if (!currentItems.any { it.equals(pName, ignoreCase = true) }) {
                                        currentItems.add(pName)
                                    }
                                }
                                remarksText = currentItems.joinToString(", ")
                            },
                            label = { Text(pName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remarks Field
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Remarks / Product Notes") },
                    placeholder = { Text("Selected products & remarks appear here") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Image Attachment Button & Multi-Image Thumbnail Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (imageUris.isEmpty()) "Attach Bills / Photos" else "Add More Photos")
                        }

                        if (imageUris.isNotEmpty()) {
                            Text(
                                text = "${imageUris.size} attached",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (imageUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            imageUris.forEachIndexed { idx, uriStr ->
                                val imageModel: Any = remember(uriStr) {
                                    ImageUtils.getImageModel(uriStr)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Bill thumbnail ${idx + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            imageUris = imageUris.filterIndexed { i, _ -> i != idx }
                                        },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                } // End of scrollable form column

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Sticky Save & Cancel Buttons at Bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val shop = selectedShop
                            if (shop != null && weightVal > 0 && touchVal > 0) {
                                onSave(
                                    dateText,
                                    shop.id,
                                    shop.shopName,
                                    selectedType.label,
                                    weightVal,
                                    touchVal,
                                    touchAdjVal,
                                    remarksText,
                                    ImageUtils.joinImageUris(imageUris)
                                )
                                onDismiss()
                            }
                        },
                        enabled = selectedShop != null && weightVal > 0 && touchVal > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (existingTransaction == null) "Save Transaction" else "Update Transaction")
                    }
                }
            }
        }
    }
}
