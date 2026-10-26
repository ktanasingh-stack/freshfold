package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaundryOrder
import com.example.model.OrderStatus
import com.example.ui.components.LiveTrackingMap
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    order: LaundryOrder?,
    onBack: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onSendNotification: () -> Unit,
    onNavigateToBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }
    var chatMessage by remember { mutableStateOf("") }
    var sentMessageNotice by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (order != null) "Track Order #${order.orderId}" else "Live Order Tracking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (order != null) order.status.displayTitle else "Real-time updates",
                            fontSize = 12.sp,
                            color = TealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tracking_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                },
                actions = {
                    IconButton(onClick = onSendNotification, modifier = Modifier.testTag("test_push_icon")) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Simulate Push", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        modifier = modifier
    ) { padding ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Slate50),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalLaundryService, contentDescription = null, tint = TealDark, modifier = Modifier.size(36.dp))
                    }
                    Text("No Active Order", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("You don't have any orders currently in progress. Book a pickup to experience live tracking!", fontSize = 13.sp, color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(
                        onClick = onNavigateToBooking,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Book Laundry Pickup")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Slate50),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Map Canvas
                item {
                    LiveTrackingMap(order = order)
                }

                // Interactive Simulator Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                    Text("Real-Time Simulation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                }
                                StatusBadge(status = order.status)
                            }

                            Text(
                                "Advance through the laundry workflow to test real-time timeline changes and system push notifications.",
                                fontSize = 12.sp,
                                color = Slate500
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onAdvanceStatus,
                                    enabled = !order.status.isCompleted,
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("advance_status_action")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (order.status.isCompleted) "Delivered!" else "Next Stage (${order.status.nextStatus()?.displayTitle ?: "Done"})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onSendNotification,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealDark),
                                    modifier = Modifier.testTag("send_push_notification_btn")
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Push Alert", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Driver Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Slate900),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    order.driver.avatarInitials,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    order.driver.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    "${order.driver.vehicleModel} • ${order.driver.licensePlate}",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                    Text("${order.driver.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    Text("(${order.driver.completedTrips} trips)", fontSize = 11.sp, color = Slate500)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${order.driver.phone}")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            // Handle emulator dialer intent
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(TealLight)
                                        .testTag("call_driver_btn")
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call Driver", tint = TealDark, modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = { showMessageDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SkyLight)
                                        .testTag("message_driver_btn")
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "Message Driver", tint = SkyBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Interactive 7-Stage Timeline
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Live Status Timeline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate900
                            )

                            OrderStatus.entries.forEachIndexed { index, stage ->
                                val isPast = stage.stepIndex < order.status.stepIndex
                                val isCurrent = stage.stepIndex == order.status.stepIndex
                                val isFuture = stage.stepIndex > order.status.stepIndex

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Step Indicator Column
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(28.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isPast -> EmeraldSuccess
                                                        isCurrent -> TealPrimary
                                                        else -> Slate100
                                                    }
                                                )
                                                .border(
                                                    width = if (isCurrent) 2.dp else 0.dp,
                                                    color = if (isCurrent) TealDark else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isPast) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                            } else if (isCurrent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(PureWhite)
                                                )
                                            } else {
                                                Text(
                                                    "${stage.stepIndex + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate500
                                                )
                                            }
                                        }

                                        if (index < OrderStatus.entries.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(28.dp)
                                                    .background(if (isPast) EmeraldSuccess else Slate300)
                                            )
                                        }
                                    }

                                    // Content Column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stage.displayTitle,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = when {
                                                isCurrent -> TealDark
                                                isPast -> Slate900
                                                else -> Slate500
                                            }
                                        )
                                        Text(
                                            text = stage.detailedStatus,
                                            fontSize = 12.sp,
                                            color = if (isCurrent) Slate700 else Slate500
                                        )
                                        if (isCurrent) {
                                            Text(
                                                text = stage.estimateText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberWarning,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Delivery & Slot Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Booking & Delivery Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Address", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                    Text(order.address, fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Medium)
                                    if (order.apartmentOrNotes.isNotBlank()) {
                                        Text(order.apartmentOrNotes, fontSize = 12.sp, color = Slate700)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Scheduled Pickup", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                    Text("${order.pickupDate} • ${order.pickupSlot}", fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Medium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Scheduled Delivery", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                    Text("${order.deliveryDate} • ${order.deliverySlot}", fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Medium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Payment", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                    Text("${order.paymentMethod.title} • Paid $${String.format("%.2f", order.totalAmount)}", fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Message Driver Dialog
    if (showMessageDialog && order != null) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Message Driver (${order.driver.name})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send a quick delivery instruction to driver ${order.driver.name}:", fontSize = 13.sp, color = Slate700)
                    OutlinedTextField(
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        placeholder = { Text("e.g., Leave basket on front porch") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    if (sentMessageNotice) {
                        Text("Message sent to driver via telemetry!", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sentMessageNotice = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
