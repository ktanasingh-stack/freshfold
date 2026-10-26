package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaundryOrder
import com.example.model.OrderStatus
import com.example.model.ServiceType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    activeOrder: LaundryOrder?,
    unreadNotificationCount: Int,
    onNavigateToBooking: (ServiceType?) -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onAdvanceOrderStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(TealDark, TealPrimary)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalLaundryService,
                                    contentDescription = "FreshFold Icon",
                                    tint = PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                "FreshFold",
                                color = PureWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Doorstep Laundry & Dry Cleaning",
                            color = TealLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Notification Bell Button with Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PureWhite.copy(alpha = 0.15f))
                            .clickable { onNavigateToNotifications() }
                            .testTag("home_notification_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PureWhite
                        )
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(RoseAlert)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    color = PureWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Order Live Tracker Card
        if (activeOrder != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-14).dp)
                        .testTag("active_order_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "ACTIVE ORDER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "#${activeOrder.orderId}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            StatusBadge(status = activeOrder.status)
                        }

                        // Progress Indicator
                        val progressFraction = (activeOrder.status.stepIndex + 1).toFloat() / OrderStatus.entries.size.toFloat()
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = TealPrimary,
                                trackColor = Slate100
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = activeOrder.status.detailedStatus,
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Step ${activeOrder.status.stepIndex + 1}/${OrderStatus.entries.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TealPrimary
                                )
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToTracking,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("track_order_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Tracker", fontWeight = FontWeight.SemiBold)
                            }

                            if (!activeOrder.status.isCompleted) {
                                OutlinedButton(
                                    onClick = onAdvanceOrderStatus,
                                    modifier = Modifier.testTag("advance_status_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealDark)
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Step +", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Promo Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AmberWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Get 20% Off First Wash",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            "Use code FRESH20 at checkout for instant savings.",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Button(
                        onClick = { onNavigateToBooking(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Title: Laundry Services
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Our Laundry Services",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    "Transparent Pricing",
                    fontSize = 12.sp,
                    color = TealDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Services List
        items(ServiceType.entries) { service ->
            ServiceCard(
                service = service,
                onSelect = { onNavigateToBooking(service) }
            )
        }

        // Why Choose FreshFold Highlights
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "The FreshFold Guarantee",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturePill(
                        icon = Icons.Default.Eco,
                        title = "100% Eco Wash",
                        subtitle = "Organic detergents",
                        modifier = Modifier.weight(1f)
                    )
                    FeaturePill(
                        icon = Icons.Default.Shield,
                        title = "Fabric Safe",
                        subtitle = "Garment guarantee",
                        modifier = Modifier.weight(1f)
                    )
                    FeaturePill(
                        icon = Icons.Default.FlashOn,
                        title = "Express 6h",
                        subtitle = "Same day turnaround",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceType,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onSelect() }
            .testTag("service_card_${service.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (service) {
                        ServiceType.WASH_AND_FOLD -> Icons.Default.LocalLaundryService
                        ServiceType.WASH_AND_PRESS -> Icons.Default.Iron
                        ServiceType.DRY_CLEANING -> Icons.Default.DryCleaning
                        ServiceType.BEDDING_LINENS -> Icons.Default.Bed
                        ServiceType.SNEAKER_CARE -> Icons.Default.Checkroom
                    },
                    contentDescription = null,
                    tint = TealDark,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Slate900
                )
                Text(
                    text = service.subtitle,
                    fontSize = 12.sp,
                    color = Slate500,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format("%.2f", service.basePrice)} ",
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        fontSize = 14.sp
                    )
                    Text(
                        text = service.unitLabel,
                        color = Slate500,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${service.turnaroundHours}h turn",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to booking",
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Slate100
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate900)
            Text(subtitle, fontSize = 9.sp, color = Slate500)
        }
    }
}
