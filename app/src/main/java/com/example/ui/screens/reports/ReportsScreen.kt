package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionCardItem
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.HoldAmber
import com.example.ui.theme.ReturnGreen
import com.example.util.ReportSharingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    shops: List<Shop>,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedShopId by remember { mutableStateOf<Long?>(null) }
    var selectedShopName by remember { mutableStateOf("அனைத்து கடைகள்") }
    var expandedShopDropdown by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Date Range State
    var showDateRangePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()
    
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateSdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val filteredTransactions = remember(transactions, selectedShopId, datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis) {
        transactions.filter { tx ->
            val matchesShop = selectedShopId == null || tx.shopId == selectedShopId
            
            val txTime = try { sdf.parse(tx.date)?.time ?: 0L } catch (e: Exception) { 0L }
            val start = datePickerState.selectedStartDateMillis
            val end = datePickerState.selectedEndDateMillis
            
            val matchesDate = if (start != null && end != null) {
                // Buffer to end of day for endDate
                txTime in start..end
            } else if (start != null) {
                txTime >= start
            } else if (end != null) {
                txTime <= end
            } else true
            
            matchesShop && matchesDate
        }.sortedWith(compareBy({ it.date }, { it.time }))
    }

    val formattedDateRange = remember(datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis) {
        val start = datePickerState.selectedStartDateMillis
        val end = datePickerState.selectedEndDateMillis
        if (start != null && end != null) {
            "${displayDateSdf.format(Date(start))} - ${displayDateSdf.format(Date(end))}"
        } else if (start != null) {
            "From ${displayDateSdf.format(Date(start))}"
        } else if (end != null) {
            "Until ${displayDateSdf.format(Date(end))}"
        } else "முழு விவரம்"
    }

    // Calculations
    var deliverySum = 0.0
    var returnSum = 0.0
    filteredTransactions.forEach { tx ->
        val type = TransactionType.fromLabel(tx.type)
        if (type.isDeliveryType) deliverySum += tx.pureWeight
        else if (type.isReturnType) returnSum += tx.pureWeight
    }
    val roundedDelivery = Math.round(deliverySum * 10.0) / 10.0
    val roundedReturn = Math.round(returnSum * 10.0) / 10.0
    val roundedHold = Math.round((deliverySum - returnSum) * 10.0) / 10.0

    if (showShareDialog) {
        ShareOptionsDialog(
            onDismiss = { showShareDialog = false },
            onSelect = { format ->
                showShareDialog = false
                when (format) {
                    "Text" -> ReportSharingUtils.shareTextReport(
                        context, selectedShopName, filteredTransactions,
                        roundedDelivery, roundedReturn, roundedHold,
                        formattedDateRange
                    )
                    "Image" -> ReportSharingUtils.shareImageReport(
                        context, selectedShopName, filteredTransactions,
                        roundedDelivery, roundedReturn, roundedHold,
                        formattedDateRange
                    )
                    "PDF" -> ReportSharingUtils.sharePdfReport(
                        context, selectedShopName, filteredTransactions,
                        roundedDelivery, roundedReturn, roundedHold,
                        formattedDateRange
                    )
                }
            }
        )
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("சரி") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    datePickerState.setSelection(null, null)
                    showDateRangePicker = false 
                }) { Text("அழி") }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                title = { Text("தேதி வரம்பைத் தேர்ந்தெடுக்கவும்", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "அறிக்கைகள்",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { showShareDialog = true },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("பகிரவும்")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shop Dropdown Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedShopDropdown,
                onExpandedChange = { expandedShopDropdown = !expandedShopDropdown },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedShopName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("கடை வாரியாக") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShopDropdown) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedShopDropdown,
                    onDismissRequest = { expandedShopDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("அனைத்து கடைகள்", fontWeight = FontWeight.Bold) },
                        onClick = {
                            selectedShopId = null
                            selectedShopName = "அனைத்து கடைகள்"
                            expandedShopDropdown = false
                        }
                    )
                    shops.forEach { shop ->
                        DropdownMenuItem(
                            text = { Text(shop.shopName) },
                            onClick = {
                                selectedShopId = shop.id
                                selectedShopName = shop.shopName
                                expandedShopDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = formattedDateRange,
                onValueChange = {},
                readOnly = true,
                label = { Text("தேதி வரம்பு") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDateRangePicker = true },
                enabled = false, // To make it clickable as a whole
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "இருப்பு விவரம் ($selectedShopName)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("மொத்த கொடுத்தல்", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedDelivery g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("மொத்த வரவு", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedReturn g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ReturnGreen)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("தற்போதைய இருப்பு", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedHold g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = HoldAmber)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "அறிக்கை விவரம் (${filteredTransactions.size} பதிவுகள்)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("தகவல் இல்லை.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                itemsIndexed(filteredTransactions) { index, tx ->
                    TransactionCardItem(index = index + 1, tx = tx)
                }
            }
        }
    }
}

@Composable
fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("அறிக்கையை பகிரவும்", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("எந்த வடிவில் பகிர வேண்டும்?", style = MaterialTheme.typography.bodyMedium)
                ShareOptionItem(icon = Icons.Default.Description, label = "Text (எளிமையானது)", onClick = { onSelect("Text") })
                ShareOptionItem(icon = Icons.Default.Image, label = "Image (புகைப்படம்)", onClick = { onSelect("Image") })
                ShareOptionItem(icon = Icons.Default.PictureAsPdf, label = "PDF (முழுமையான அறிக்கை)", onClick = { onSelect("PDF") })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("ரத்து செய்") }
        }
    )
}

@Composable
fun ShareOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontWeight = FontWeight.Medium)
        }
    }
}
