package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Shop

@Composable
fun ShopDialog(
    existingShop: Shop? = null,
    onDismiss: () -> Unit,
    onSave: (shopName: String, ownerName: String, phone: String, address: String, gstNumber: String, notes: String) -> Unit
) {
    var shopName by remember { mutableStateOf(existingShop?.shopName ?: "") }
    var ownerName by remember { mutableStateOf(existingShop?.ownerName ?: "") }
    var phone by remember { mutableStateOf(existingShop?.phone ?: "") }
    var address by remember { mutableStateOf(existingShop?.address ?: "") }
    var gstNumber by remember { mutableStateOf(existingShop?.gstNumber ?: "") }
    var notes by remember { mutableStateOf(existingShop?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingShop == null) "புதிய கடையைச் சேர்க்கவும்" else "கடை விவரத்தைத் திருத்து",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "மூடு")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("கடை பெயர் *") },
                    placeholder = { Text("எ.கா. ஸ்ரீ ராஜா ஜூவல்லர்ஸ்") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("உரிமையாளர் பெயர் *") },
                    placeholder = { Text("எ.கா. சுப்பா ராவ்") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("தொலைபேசி எண் *") },
                    placeholder = { Text("எ.கா. +91 98480 22338") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("முகவரி") },
                    placeholder = { Text("எ.கா. மெயின் ரோடு, சேலம்") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gstNumber,
                    onValueChange = { gstNumber = it },
                    label = { Text("ஜிஎஸ்டி எண் (விருப்பத்தேர்வு)") },
                    placeholder = { Text("எ.கா. 37AAAAA0000A1Z5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("குறிப்புகள் (விருப்பத்தேர்வு)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("ரத்து செய்")
                    }
                    Button(
                        onClick = {
                            if (shopName.isNotBlank() && ownerName.isNotBlank() && phone.isNotBlank()) {
                                onSave(shopName, ownerName, phone, address, gstNumber, notes)
                                onDismiss()
                            }
                        },
                        enabled = shopName.isNotBlank() && ownerName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("கடையைச் சேமி")
                    }
                }
            }
        }
    }
}
