package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.util.ImageUtils
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.ReturnGreen

@Composable
fun TransactionCardItem(
    tx: Transaction,
    modifier: Modifier = Modifier,
    index: Int? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    val typeEnum = TransactionType.fromLabel(tx.type)
    val badgeColor = when {
        typeEnum.isDeliveryType -> DeliveryBlue
        typeEnum.isReturnType -> ReturnGreen
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index != null) {
                        Text(
                            text = "$index.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val typeEnumDisplay = TransactionType.fromLabel(tx.type)
                        Text(
                            text = typeEnumDisplay.displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${tx.date} ${tx.time}".trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${tx.pureWeight} g Pure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )

                    if (onEdit != null || onDelete != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        if (onEdit != null) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Ledger Entry",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (onDelete != null) {
                            IconButton(
                                onClick = { showConfirmDelete = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Ledger Entry",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tx.shopName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "மொத்தம்: ${tx.weight} g | டச்: ${tx.touch}%" + if (tx.touchAdjustment != 0.0) " (+${tx.touchAdjustment}%)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            if (tx.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "குறிப்பு: ${tx.remarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            val imageList = remember(tx.imageUri) {
                ImageUtils.parseImageUris(tx.imageUri)
            }

            if (imageList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showImagePreview = true }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        imageList.take(3).forEach { imgUri ->
                            val imageModel: Any = remember(imgUri) {
                                ImageUtils.getImageModel(imgUri)
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageModel)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Bill Proof",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (imageList.size > 1) "${imageList.size} இணைப்பு படங்கள் 📷" else "இணைப்பு படங்கள் 🔍",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showConfirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("பரிவர்த்தனையை நீக்கலாமா?") },
            text = { Text("${tx.shopName}-க்கான இந்த ${TransactionType.fromLabel(tx.type).displayLabel} பதிவை (${tx.pureWeight}g) நீக்க விரும்புகிறீர்களா?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showConfirmDelete = false
                }) {
                    Text("நீக்கு", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("ரத்து செய்")
                }
            }
        )
    }

    if (showImagePreview) {
        val imageList = ImageUtils.parseImageUris(tx.imageUri)
        if (imageList.isNotEmpty()) {
            ImagePreviewDialog(
                imageUris = imageList,
                title = "Proof Images - ${tx.shopName} (${tx.date} ${tx.time})",
                onDismiss = { showImagePreview = false }
            )
        }
    }
}

