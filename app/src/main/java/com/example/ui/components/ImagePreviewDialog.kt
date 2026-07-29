package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.ImageUtils

@Composable
fun ImagePreviewDialog(
    imageUris: List<String>,
    initialIndex: Int = 0,
    title: String = "Proof Image",
    onDismiss: () -> Unit
) {
    val validUris = remember(imageUris) { imageUris.filter { it.isNotBlank() } }
    if (validUris.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, validUris.size - 1)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.92f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (validUris.size > 1) {
                            Text(
                                text = "Photo ${currentIndex + 1} of ${validUris.size} (Slide view)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Main Image View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp, bottom = 70.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val currentUriStr = validUris[currentIndex]
                    val imageModel: Any = remember(currentUriStr) {
                        ImageUtils.getImageModel(currentUriStr)
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full Preview Slide ${currentIndex + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Previous Button
                    if (currentIndex > 0) {
                        IconButton(
                            onClick = { currentIndex-- },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                contentDescription = "Previous Image",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Next Button
                    if (currentIndex < validUris.size - 1) {
                        IconButton(
                            onClick = { currentIndex++ },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "Next Image",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Bottom Indicator Dots / Slide Selector
                if (validUris.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        validUris.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentIndex) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f))
                                    .clickable { currentIndex = index }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUri: String,
    title: String = "Proof Image",
    onDismiss: () -> Unit
) {
    ImagePreviewDialog(
        imageUris = listOf(imageUri),
        initialIndex = 0,
        title = title,
        onDismiss = onDismiss
    )
}
