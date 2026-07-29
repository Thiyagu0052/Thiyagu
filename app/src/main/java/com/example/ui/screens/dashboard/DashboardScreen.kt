package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HoldSummary
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.SummaryCardsSection
import com.example.ui.components.TransactionCardItem
import com.example.ui.components.TransactionDialog
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.HoldAmber
import com.example.ui.theme.ReturnGreen
import com.example.ui.theme.SilverPrimary

import com.example.data.model.Product

@Composable
fun DashboardScreen(
    summary: HoldSummary,
    shops: List<Shop>,
    products: List<Product> = emptyList(),
    transactions: List<Transaction>,
    onNavigateToTransactions: () -> Unit,
    onNavigateToShops: () -> Unit,
    onNavigateToShopDetail: (Long) -> Unit,
    onAddTransaction: (
        date: String,
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
    modifier: Modifier = Modifier
) {
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val recentTransactions = remember(transactions) {
        transactions.take(5)
    }

    // Calculate per-shop hold breakdown for summary carousel
    val shopHoldSummaries = remember(shops, transactions) {
        shops.map { shop ->
            val shopTxs = transactions.filter { it.shopId == shop.id }
            var del = 0.0
            var ret = 0.0
            shopTxs.forEach { tx ->
                val type = TransactionType.fromLabel(tx.type)
                if (type.isDeliveryType) del += tx.pureWeight
                else if (type.isReturnType) ret += tx.pureWeight
            }
            val hold = del - ret
            Triple(
                shop,
                Triple(
                    Math.round(del * 10.0) / 10.0,
                    Math.round(ret * 10.0) / 10.0,
                    Math.round(hold * 10.0) / 10.0
                ),
                shopTxs.size
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SilverPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Silver ERP Wholesale",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Business Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { showAddTransactionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Entry")
                        }
                    }
                }
            }

            // Summary Cards Grid
            item {
                SummaryCardsSection(
                    summary = summary,
                    onShopClick = onNavigateToShops,
                    onDeliveryClick = onNavigateToTransactions,
                    onReturnClick = onNavigateToTransactions
                )
            }

            // Shop Hold Summaries Carousel
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shop Hold Summaries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToShops) {
                            Text("View All")
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (shopHoldSummaries.isEmpty()) {
                        Text(
                            text = "No shops registered yet. Add a shop to view hold summary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(shopHoldSummaries) { (shop, metrics, txCount) ->
                                val (del, ret, hold) = metrics
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { onNavigateToShopDetail(shop.id) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = shop.shopName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = shop.ownerName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Delivery", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text("$del g", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DeliveryBlue)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Return", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text("$ret g", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ReturnGreen)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HoldAmber.copy(alpha = 0.15f))
                                                .padding(vertical = 4.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Hold: $hold g",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = HoldAmber
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("Full Ledger")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No transactions recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showAddTransactionDialog = true }) {
                                Text("Add First Transaction")
                            }
                        }
                    }
                }
            } else {
                items(recentTransactions) { tx ->
                    TransactionCardItem(
                        tx = tx,
                        onEdit = { editingTransaction = tx },
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddTransactionDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (showAddTransactionDialog) {
        TransactionDialog(
            shops = shops,
            products = products,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { date, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                onAddTransaction(date, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
            }
        )
    }

    if (editingTransaction != null) {
        TransactionDialog(
            shops = shops,
            products = products,
            existingTransaction = editingTransaction,
            onDismiss = { editingTransaction = null },
            onSave = { date, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                val txId = editingTransaction!!.id
                onUpdateTransaction(txId, date, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                editingTransaction = null
            }
        )
    }
}
