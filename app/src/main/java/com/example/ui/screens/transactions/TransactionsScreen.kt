package com.example.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Shop
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionCardItem
import com.example.ui.components.TransactionDialog

import com.example.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    shops: List<Shop>,
    products: List<Product> = emptyList(),
    transactions: List<Transaction>,
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }
    var selectedShopFilterId by remember { mutableStateOf<Long?>(null) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedShopFilterId) {
        transactions.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                    tx.shopName.contains(searchQuery, ignoreCase = true) ||
                    tx.remarks.contains(searchQuery, ignoreCase = true) ||
                    tx.type.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || tx.type.equals(selectedTypeFilter, ignoreCase = true)
            val matchesShop = selectedShopFilterId == null || tx.shopId == selectedShopFilterId
            matchesQuery && matchesType && matchesShop
        }.sortedWith(compareBy({ it.date }, { it.time }))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("கடை பெயர், வகை, குறிப்புகள் மூலம் தேடுங்கள்...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("அனைத்து வகைகள்") }
                    )
                }

                items(TransactionType.entries) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type.label,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == type.label) null else type.label
                        },
                        label = { Text(type.displayLabel) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Summary Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "பரிவர்த்தனை லெட்ஜர் (${filteredTransactions.size} பதிவுகள்)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "பரிவர்த்தனை விவரங்கள் இல்லை.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filteredTransactions) { index, tx ->
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
            onClick = { showAddTransactionDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Transaction")
        }
    }

    if (showAddTransactionDialog) {
        TransactionDialog(
            shops = shops,
            products = products,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                onAddTransaction(date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
            }
        )
    }

    if (editingTransaction != null) {
        TransactionDialog(
            shops = shops,
            products = products,
            existingTransaction = editingTransaction,
            onDismiss = { editingTransaction = null },
            onSave = { date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                val txId = editingTransaction!!.id
                onUpdateTransaction(txId, date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                editingTransaction = null
            }
        )
    }
}
