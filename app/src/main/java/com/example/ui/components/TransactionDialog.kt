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
import java.util.Calendar
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
        time: String,
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
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    var dateText by remember { mutableStateOf(existingTransaction?.date ?: todayDate) }
    var timeText by remember { mutableStateOf(existingTransaction?.time ?: currentTime) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateText)?.time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = try { timeText.split(":")[0].toInt() } catch (e: Exception) { 12 },
        initialMinute = try { timeText.split(":")[1].toInt() } catch (e: Exception) { 0 }
    )

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
                            text = if (existingTransaction == null) "புதிய பரிவர்த்தனை" else "பரிவர்த்தனையைத் திருத்து",
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

                // Date & Time Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = { },
                            label = { Text("தேதி (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        // Invisible click layer
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    Box(modifier = Modifier.weight(0.8f)) {
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = { },
                            label = { Text("நேரம் (HH:mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        // Invisible click layer
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selectedDate = datePickerState.selectedDateMillis
                                if (selectedDate != null) {
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = selectedDate
                                    dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                }
                                showDatePicker = false
                            }) { Text("சரி") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("ரத்து செய்") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showTimePicker) {
                    Dialog(onDismissRequest = { showTimePicker = false }) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "நேரத்தைத் தேர்ந்தெடுக்கவும்",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                                TimePicker(state = timePickerState)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showTimePicker = false }) { Text("ரத்து செய்") }
                                    TextButton(onClick = {
                                        timeText = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                                        showTimePicker = false
                                    }) { Text("சரி") }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select Shop Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedShopDropdown,
                    onExpandedChange = { expandedShopDropdown = !expandedShopDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedShop?.shopName ?: "கடையைத் தேர்ந்தெடுக்கவும்",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("கடையைத் தேர்ந்தெடுக்கவும்") },
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
                        value = selectedType.displayLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("பரிவர்த்தனை வகை") },
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
                                text = { Text(type.displayLabel, fontWeight = FontWeight.Medium) },
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
                        label = { Text("எடை (கி)") },
                        placeholder = { Text("எ.கா. 6369") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = touchText,
                        onValueChange = { touchText = it },
                        label = { Text("டச் (%)") },
                        placeholder = { Text("எ.கா. 76") },
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
                    label = { Text("டச் சரிசெய்தல் (%)") },
                    placeholder = { Text("எ.கா. 0 அல்லது 11") },
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
                            text = "கணக்கிடப்பட்ட நிகர எடை",
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
                            text = "சூத்திரம்: நிகர = எடை × (டச் + சரிசெய்தல்) / 100",
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
                    text = "தயாரிப்பு பெயர்(கள்):",
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
                    label = { Text("குறிப்புகள் / தயாரிப்பு விவரங்கள்") },
                    placeholder = { Text("தேர்ந்தெடுக்கப்பட்ட தயாரிப்புகள் இங்கே தோன்றும்") },
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
                            Text(if (imageUris.isEmpty()) "பில்கள் / புகைப்படங்களை இணைக்கவும்" else "கூடுதல் புகைப்படங்களைச் சேர்க்கவும்")
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
                        Text("ரத்து செய்")
                    }

                    Button(
                        onClick = {
                            val shop = selectedShop
                            if (shop != null && weightVal > 0 && touchVal > 0) {
                                onSave(
                                    dateText,
                                    timeText,
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
                        Text(if (existingTransaction == null) "சேமி" else "புதுப்பி")
                    }
                }
            }
        }
    }
}
