package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LaundryOrder
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    orders: List<LaundryOrder>,
    activeOrder: LaundryOrder?,
    onTrackOrder: (LaundryOrder) -> Unit,
    onReorder: (LaundryOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInvoiceOrder by remember { mutableStateOf<LaundryOrder?>(null) }
    var ratingOrder by remember { mutableStateOf<LaundryOrder?>(null) }
    var starRating by remember { mutableIntStateOf(5) }
    var reviewSubmitted by remember { mutableStateOf(false) }

    val allOrders = remember(orders, activeOrder) {
        val list = mutableListOf<LaundryOrder>()
        if (activeOrder != null) list.add(activeOrder)
        list.addAll(orders)
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Orders & Receipts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Slate900
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        modifier = modifier
    ) { padding ->
        if (allOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Slate50),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Slate500, modifier = Modifier.size(56.dp))
                    Text("No Orders Yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("Your completed laundry orders and receipts will appear here.", fontSize = 13.sp, color = Slate500)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Slate50),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(allOrders) { order ->
                    OrderHistoryCard(
                        order = order,
                        onTrack = { onTrackOrder(order) },
                        onViewInvoice = { selectedInvoiceOrder = order },
                        onReorder = { onReorder(order) },
                        onRate = { ratingOrder = order }
                    )
                }
            }
        }
    }

    // Digital Invoice Dialog
    if (selectedInvoiceOrder != null) {
        val order = selectedInvoiceOrder!!
        Dialog(onDismissRequest = { selectedInvoiceOrder = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FreshFold Digital Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                            Text("Order #${order.orderId}", fontSize = 12.sp, color = Slate500)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text("PAID", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    HorizontalDivider(color = Slate100)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        order.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.quantity}x ${item.type.title}", fontSize = 13.sp, color = Slate700)
                                Text("$${String.format("%.2f", item.totalPrice)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }
                        if (order.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", fontSize = 13.sp, color = EmeraldSuccess)
                                Text("-$${String.format("%.2f", order.discount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service Fee", fontSize = 13.sp, color = Slate500)
                            Text("$${String.format("%.2f", order.serviceFee)}", fontSize = 13.sp, color = Slate700)
                        }
                        HorizontalDivider(color = Slate300, thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Text("$${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TealDark)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Transaction: ${order.paymentReference}", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                            Text("Paid via: ${order.paymentMethod.title}", fontSize = 11.sp, color = Slate500)
                            Text("Date: ${order.createdAt}", fontSize = 11.sp, color = Slate500)
                        }
                    }

                    Button(
                        onClick = { selectedInvoiceOrder = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Invoice")
                    }
                }
            }
        }
    }

    // Rate & Review Dialog
    if (ratingOrder != null) {
        val order = ratingOrder!!
        AlertDialog(
            onDismissRequest = { ratingOrder = null; reviewSubmitted = false },
            title = { Text("Rate Service & Driver") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("How was your laundry experience with driver ${order.driver.name}?", fontSize = 13.sp, color = Slate700)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { starRating = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= starRating) AmberWarning else Slate300,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    if (reviewSubmitted) {
                        Text("Thank you for your feedback!", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { reviewSubmitted = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(if (reviewSubmitted) "Submitted" else "Submit Rating")
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingOrder = null; reviewSubmitted = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderHistoryCard(
    order: LaundryOrder,
    onTrack: () -> Unit,
    onViewInvoice: () -> Unit,
    onReorder: () -> Unit,
    onRate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${order.orderId}"),
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
                Column {
                    Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text(order.createdAt, fontSize = 12.sp, color = Slate500)
                }
                StatusBadge(status = order.status)
            }

            // Items summary
            Text(
                text = order.items.joinToString(" • ") { "${it.quantity}x ${it.type.title}" },
                fontSize = 13.sp,
                color = Slate700
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${String.format("%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TealDark
                )
                Text(
                    text = "${order.pickupDate} → ${order.deliveryDate}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            HorizontalDivider(color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!order.status.isCompleted) {
                    Button(
                        onClick = onTrack,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Track", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onRate,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReorder,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onViewInvoice,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 12.sp)
                }
            }
        }
    }
}
