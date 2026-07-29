package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    shops: List<Shop>,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedShopId by remember { mutableStateOf<Long?>(null) }
    var selectedShopName by remember { mutableStateOf("All Shops") }
    var expandedShopDropdown by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, selectedShopId) {
        if (selectedShopId == null) transactions
        else transactions.filter { it.shopId == selectedShopId }
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

    fun shareReport(ctx: Context) {
        val reportBuilder = StringBuilder()
        reportBuilder.append("--- SILVER ERP REPORT ---\n")
        reportBuilder.append("Shop Filter: $selectedShopName\n")
        reportBuilder.append("Total Delivery Pure Weight: $roundedDelivery g\n")
        reportBuilder.append("Total Return Pure Weight: $roundedReturn g\n")
        reportBuilder.append("Current Hold Balance: $roundedHold g\n\n")
        reportBuilder.append("Transactions (${filteredTransactions.size} records):\n")

        filteredTransactions.forEach { tx ->
            reportBuilder.append("${tx.date} | ${tx.shopName} | ${tx.type} | Pure: ${tx.pureWeight}g (Gross: ${tx.weight}g @ ${tx.touch}%)\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportBuilder.toString())
            type = "text/plain"
        }
        ctx.startActivity(Intent.createChooser(sendIntent, "Share Silver ERP Report"))
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
                    text = "Reports & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { shareReport(context) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shop Dropdown Filter
        ExposedDropdownMenuBox(
            expanded = expandedShopDropdown,
            onExpandedChange = { expandedShopDropdown = !expandedShopDropdown }
        ) {
            OutlinedTextField(
                value = selectedShopName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Shop") },
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
                DropdownMenuItem(
                    text = { Text("All Shops", fontWeight = FontWeight.Bold) },
                    onClick = {
                        selectedShopId = null
                        selectedShopName = "All Shops"
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

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Holding Balance Summary ($selectedShopName)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Delivery", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedDelivery g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Return", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedReturn g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ReturnGreen)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Hold", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("$roundedHold g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = HoldAmber)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Report Statement (${filteredTransactions.size} Records)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transaction data available for this report.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredTransactions) { tx ->
                    TransactionCardItem(tx = tx)
                }
            }
        }
    }
}
