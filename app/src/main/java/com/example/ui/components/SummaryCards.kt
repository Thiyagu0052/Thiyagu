package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HoldSummary
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.HoldAmber
import com.example.ui.theme.ReturnGreen
import com.example.ui.theme.SettlementPurple

@Composable
fun SummaryCardsSection(
    summary: HoldSummary,
    modifier: Modifier = Modifier,
    onShopClick: () -> Unit = {},
    onDeliveryClick: () -> Unit = {},
    onReturnClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "மொத்த கொடுத்தல்",
                value = "${summary.totalDelivery} g",
                subtitle = "வெள்ளி வழங்கப்பட்டது",
                icon = Icons.Default.CallMade,
                containerColor = Color(0xFFEFF6FF),
                contentColor = DeliveryBlue,
                modifier = Modifier.weight(1f),
                onClick = onDeliveryClick
            )

            SummaryCard(
                title = "மொத்த வரவு",
                value = "${summary.totalReturn} g",
                subtitle = "கச்சா & பைன்",
                icon = Icons.Default.CallReceived,
                containerColor = Color(0xFFECFDF5),
                contentColor = ReturnGreen,
                modifier = Modifier.weight(1f),
                onClick = onReturnClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "தற்போதைய இருப்பு",
                value = "${summary.currentHold} g",
                subtitle = "கொடுத்தல் - வரவு",
                icon = Icons.Default.Lock,
                containerColor = Color(0xFFFFFBEB),
                contentColor = HoldAmber,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "மொத்தக் கடைகள்",
                value = "${summary.totalShops}",
                subtitle = "செயலில் உள்ள கடைகள்",
                icon = Icons.Default.Store,
                containerColor = Color(0xFFF3E8FF),
                contentColor = SettlementPurple,
                modifier = Modifier.weight(1f),
                onClick = onShopClick
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                maxLines = 1,
                fontSize = 8.sp
            )
        }
    }
}
