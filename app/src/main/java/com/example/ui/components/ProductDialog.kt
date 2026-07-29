package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.example.data.model.Product
import com.example.util.ImageUtils

@Composable
fun ProductDialog(
    onDismiss: () -> Unit,
    onSave: (productName: String, category: String, defaultWeight: Double, imageUri: String) -> Unit,
    existingProduct: Product? = null
) {
    var productName by remember(existingProduct) { mutableStateOf(existingProduct?.productName ?: "") }
    var category by remember(existingProduct) { mutableStateOf(existingProduct?.category?.ifBlank { "Anklets" } ?: "Anklets") }
    var defaultWeightText by remember(existingProduct) {
        mutableStateOf(if (existingProduct != null && existingProduct.defaultWeight > 0) existingProduct.defaultWeight.toString() else "")
    }
    var imageUriText by remember(existingProduct) { mutableStateOf(existingProduct?.imageUri ?: "") }

    val presetProducts = remember {
        listOf("Payal", "Anklets", "Leg Chain", "Chains", "Rings", "Utensils", "Coins", "Bars", "Kaddiyalu", "Waist Chain")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUriText = it.toString() }
    }

    val categories = remember {
        listOf("Anklets", "Chains", "Rings", "Utensils", "Coins", "Bars", "Custom Ornaments")
    }
    var categoryExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Fixed Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingProduct == null) "Add Silver Product" else "Edit Silver Product",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name(s) *") },
                    placeholder = { Text("e.g. Payal, Leg Chain, Anklet, Rings") },
                    supportingText = { Text("Tip: Separate multiple product names with commas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Product Chips for Quick Selection
                Text(
                    text = "Quick Map Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetProducts) { preset ->
                        val isSelected = productName.split(",").map { it.trim() }.contains(preset)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val currentList = productName.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                if (isSelected) {
                                    currentList.remove(preset)
                                } else {
                                    currentList.add(preset)
                                }
                                productName = currentList.joinToString(", ")
                            },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = defaultWeightText,
                    onValueChange = { defaultWeightText = it },
                    label = { Text("Default Weight (g)") },
                    placeholder = { Text("e.g. 250") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Image Upload
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
                        Text(if (imageUriText.isEmpty()) "Upload Image" else "Change Image")
                    }

                    if (imageUriText.isNotEmpty()) {
                        val imageModel: Any = remember(imageUriText) {
                            ImageUtils.getImageModel(imageUriText)
                        }
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Product preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUriText = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Red.copy(alpha = 0.85f), CircleShape)
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

                Spacer(modifier = Modifier.height(16.dp))
                } // End of scrollable body

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Sticky Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val w = defaultWeightText.toDoubleOrNull() ?: 0.0
                            if (productName.isNotBlank()) {
                                onSave(productName, category, w, imageUriText)
                                onDismiss()
                            }
                        },
                        enabled = productName.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}
