package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.AppNotification
import com.example.model.OrderStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSendTestPush: () -> Unit,
    onSelectNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pushPermissionGranted by remember { mutableStateOf(true) }

    // Runtime permission launcher for Android 13+ (TIRAMISU)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pushPermissionGranted = isGranted
    }

    var orderUpdatesToggle by remember { mutableStateOf(true) }
    var driverArrivalToggle by remember { mutableStateOf(true) }
    var promoAlertsToggle by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Push Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
                        Text("${notifications.count { !it.isRead }} unread alerts", fontSize = 12.sp, color = TealDark, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllRead) {
                            Text("Mark Read", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Slate50),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Android Push Permission & Test Trigger Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TealPrimary)
                                Text("Real Push Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text("ACTIVE", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text(
                            "FreshFold uses Android high-priority notification channels to alert you when your driver is nearby or when your garments are freshly cleaned.",
                            fontSize = 12.sp,
                            color = Slate500
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    onSendTestPush()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("trigger_test_push_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Test Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Notification Preferences
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notification Channels", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Order Status Milestones", fontSize = 13.sp, color = Slate700)
                            Switch(checked = orderUpdatesToggle, onCheckedChange = { orderUpdatesToggle = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Driver Proximity & Arrival (10 min warning)", fontSize = 13.sp, color = Slate700)
                            Switch(checked = driverArrivalToggle, onCheckedChange = { driverArrivalToggle = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Discounts & Weekly Deals", fontSize = 13.sp, color = Slate700)
                            Switch(checked = promoAlertsToggle, onCheckedChange = { promoAlertsToggle = it })
                        }
                    }
                }
            }

            // Section Header
            item {
                Text("Notification History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
            }

            // Notifications List
            items(notifications) { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNotification(notif.orderId) }
                        .testTag("notification_item_${notif.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!notif.isRead) TealLight.copy(alpha = 0.35f) else PureWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (notif.status) {
                                        OrderStatus.DELIVERED -> EmeraldSuccess
                                        OrderStatus.WASHING -> AmberWarning
                                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFEC4899)
                                        else -> TealPrimary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (notif.status) {
                                    OrderStatus.DELIVERED -> Icons.Default.Check
                                    OrderStatus.WASHING -> Icons.Default.LocalLaundryService
                                    OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.LocalShipping
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = notif.timeAgo,
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }

                        if (!notif.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}
