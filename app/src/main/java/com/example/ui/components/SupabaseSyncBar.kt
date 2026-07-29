package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SyncStatus
import com.example.ui.theme.ReturnGreen

@Composable
fun SupabaseSyncBar(
    syncStatus: SyncStatus,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Indicator Icon & Dot
                when (syncStatus) {
                    is SyncStatus.Syncing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is SyncStatus.Success -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ReturnGreen)
                        )
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Firebase Sync Active",
                            tint = ReturnGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    is SyncStatus.Error -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Firebase Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    is SyncStatus.Idle -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ReturnGreen)
                        )
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Firebase Live",
                            tint = ReturnGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Firebase Live Sync",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(kkysilversalem)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    val subtitleText = when (syncStatus) {
                        is SyncStatus.Syncing -> "Syncing live across devices..."
                        is SyncStatus.Success -> "${syncStatus.message} • ${syncStatus.lastSyncedAt}"
                        is SyncStatus.Error -> "Offline: ${syncStatus.errorMessage}"
                        is SyncStatus.Idle -> "Real-time sync active (2+ devices)"
                    }

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (syncStatus is SyncStatus.Error) MaterialTheme.colorScheme.error else Color.Gray,
                        maxLines = 1
                    )
                }
            }

            // Sync Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = syncStatus !is SyncStatus.Syncing) {
                        onSyncNow()
                    },
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Now",
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (syncStatus is SyncStatus.Syncing) "Syncing..." else "Sync Now",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
