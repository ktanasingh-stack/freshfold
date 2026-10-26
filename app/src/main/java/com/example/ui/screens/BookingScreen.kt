package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CreditCardPreview
import com.example.ui.components.PaymentMethodSelector
import com.example.ui.components.PaymentProcessingDialog
import com.example.ui.theme.*
import com.example.viewmodel.BookingDraft
import com.example.viewmodel.LaundryViewModel
import com.example.viewmodel.PaymentProcessingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: LaundryViewModel,
    onBack: () -> Unit,
    onBookingSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val draft by viewModel.bookingDraft.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) } // 1: Items & Care, 2: Slots & Address, 3: Secure Payment

    PaymentProcessingDialog(
        state = paymentState,
        onDismiss = { viewModel.resetPaymentState() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentStep) {
                                1 -> "Select Laundry Items"
                                2 -> "Choose Pickup & Delivery"
                                else -> "Secure Checkout"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Step $currentStep of 3",
                            fontSize = 12.sp,
                            color = TealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) currentStep-- else onBack()
                        },
                        modifier = Modifier.testTag("booking_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ESTIMATED TOTAL", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Bold)
                        Text(
                            "$${String.format("%.2f", draft.total)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                1 -> if (draft.canProceedToSlot) currentStep = 2
                                2 -> if (draft.canProceedToPayment) currentStep = 3
                                3 -> viewModel.processPaymentAndPlaceOrder(onBookingSuccess)
                            }
                        },
                        enabled = when (currentStep) {
                            1 -> draft.canProceedToSlot
                            2 -> draft.canProceedToPayment
                            3 -> paymentState is PaymentProcessingState.Idle
                            else -> true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("booking_primary_button")
                    ) {
                        Text(
                            text = when (currentStep) {
                                1 -> "Choose Slots →"
                                2 -> "Review & Pay →"
                                else -> "Pay & Book Now \uD83D\uDD12"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Slate50),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                1 -> {
                    // STEP 1: ITEM SELECTION & CARE INSTRUCTIONS
                    item {
                        Text(
                            "What would you like cleaned?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    items(ServiceType.entries) { service ->
                        val qty = draft.itemQuantities[service] ?: 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_counter_${service.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(service.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                    Text(service.subtitle, fontSize = 12.sp, color = Slate500)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "$${String.format("%.2f", service.basePrice)} ${service.unitLabel}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealDark,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decrementItem(service) },
                                        enabled = qty > 0,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (qty > 0) TealLight else Slate100)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = if (qty > 0) TealDark else Slate500, modifier = Modifier.size(18.dp))
                                    }

                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Slate900,
                                        modifier = Modifier.widthIn(min = 24.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    IconButton(
                                        onClick = { viewModel.incrementItem(service) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = PureWhite, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Preferences Card
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
                                Text("Washing & Fabric Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)

                                // Detergent chips
                                Text("Detergent Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                val detergents = listOf("Eco Fragrance-Free", "Lavender Bliss", "Hypoallergenic Baby-Safe")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(detergents) { det ->
                                        val isSel = draft.preferences.detergentType == det
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { viewModel.updatePreferences { it.copy(detergentType = det) } },
                                            label = { Text(det, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TealLight,
                                                selectedLabelColor = TealDark
                                            )
                                        )
                                    }
                                }

                                // Packaging Choice
                                Text("Packaging Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("Neatly Folded", "On Hangers").forEach { style ->
                                        val isSel = draft.preferences.packagingPreference == style
                                        OutlinedButton(
                                            onClick = { viewModel.updatePreferences { it.copy(packagingPreference = style) } },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = if (isSel) ButtonDefaults.outlinedButtonColors(containerColor = TealLight) else ButtonDefaults.outlinedButtonColors()
                                        ) {
                                            Text(style, color = if (isSel) TealDark else Slate700, fontSize = 12.sp)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = draft.preferences.specialInstructions,
                                    onValueChange = { text -> viewModel.updatePreferences { it.copy(specialInstructions = text) } },
                                    label = { Text("Special garment instructions or stain notes") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: PICKUP & DELIVERY SLOTS
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TealPrimary)
                                    Text("Preferred Pickup Slot", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                }

                                Text("Select Pickup Date", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(SlotConstants.defaultPickupDates) { slot ->
                                        val isSel = slot.id == draft.pickupDate.id
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.setPickupDate(slot) }
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) TealPrimary else Slate300,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .testTag("pickup_date_${slot.id}"),
                                            color = if (isSel) TealLight else PureWhite
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(slot.dayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSel) TealDark else Slate900)
                                                Text(slot.dateString, fontSize = 11.sp, color = if (isSel) TealDark else Slate500)
                                            }
                                        }
                                    }
                                }

                                Text("Select Pickup Time Window", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SlotConstants.timeSlots.forEach { timeSlot ->
                                        val isSel = timeSlot.id == draft.pickupSlot.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.setPickupSlot(timeSlot) }
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) TealPrimary else Slate300,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .testTag("pickup_time_${timeSlot.id}"),
                                            color = if (isSel) TealLight.copy(alpha = 0.5f) else PureWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = if (isSel) TealPrimary else Slate500, modifier = Modifier.size(16.dp))
                                                    Text(timeSlot.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900)
                                                }
                                                Text(timeSlot.period, fontSize = 11.sp, color = Slate500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Delivery Slot Card
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = EmeraldSuccess)
                                    Text("Preferred Delivery Slot", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                }

                                // Express Switch
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (draft.isExpress) Color(0xFFFEF3C7) else Slate100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                                Text("Express 6-Hour Clean", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                            }
                                            Text("Same-day priority turnaround (+$6.00)", fontSize = 11.sp, color = Slate500)
                                        }
                                        Switch(
                                            checked = draft.isExpress,
                                            onCheckedChange = { viewModel.setExpress(it) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning)
                                        )
                                    }
                                }

                                Text("Select Delivery Date", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(SlotConstants.defaultDeliveryDates) { slot ->
                                        val isSel = slot.id == draft.deliveryDate.id
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.setDeliveryDate(slot) }
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) TealPrimary else Slate300,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .testTag("delivery_date_${slot.id}"),
                                            color = if (isSel) TealLight else PureWhite
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(slot.dayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSel) TealDark else Slate900)
                                                Text(slot.dateString, fontSize = 11.sp, color = if (isSel) TealDark else Slate500)
                                            }
                                        }
                                    }
                                }

                                Text("Select Delivery Time Window", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SlotConstants.timeSlots.forEach { timeSlot ->
                                        val isSel = timeSlot.id == draft.deliverySlot.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.setDeliverySlot(timeSlot) }
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) TealPrimary else Slate300,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .testTag("delivery_time_${timeSlot.id}"),
                                            color = if (isSel) TealLight.copy(alpha = 0.5f) else PureWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = if (isSel) TealPrimary else Slate500, modifier = Modifier.size(16.dp))
                                                    Text(timeSlot.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900)
                                                }
                                                Text(timeSlot.period, fontSize = 11.sp, color = Slate500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Address Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Pickup & Delivery Address", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                OutlinedTextField(
                                    value = draft.address,
                                    onValueChange = { viewModel.setAddress(it) },
                                    label = { Text("Street Address") },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = TealPrimary) },
                                    modifier = Modifier.fillMaxWidth().testTag("address_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = draft.notes,
                                    onValueChange = { viewModel.setNotes(it) },
                                    label = { Text("Apt / Gate code / Driver pickup instructions") },
                                    modifier = Modifier.fillMaxWidth().testTag("driver_notes_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: SECURE PAYMENT GATEWAY INTEGRATION
                    item {
                        // Order Summary Breakdown
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Order Summary Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)

                                draft.itemsList.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.quantity}x ${item.type.title}", fontSize = 13.sp, color = Slate700)
                                        Text("$${String.format("%.2f", item.totalPrice)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    }
                                }

                                HorizontalDivider(color = Slate100, thickness = 1.dp)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Items Subtotal", fontSize = 13.sp, color = Slate500)
                                    Text("$${String.format("%.2f", draft.subtotal)}", fontSize = 13.sp, color = Slate700)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pickup & Delivery", fontSize = 13.sp, color = Slate500)
                                    Text("FREE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }

                                if (draft.isExpress) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Express 6h Turnaround", fontSize = 13.sp, color = AmberWarning)
                                        Text("+$6.00", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AmberWarning)
                                    }
                                }

                                if (draft.discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Promo Code (${draft.appliedPromo?.code})", fontSize = 13.sp, color = EmeraldSuccess)
                                        Text("-$${String.format("%.2f", draft.discount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Facility & Eco Service Fee", fontSize = 13.sp, color = Slate500)
                                    Text("$${String.format("%.2f", draft.serviceFee)}", fontSize = 13.sp, color = Slate700)
                                }

                                HorizontalDivider(color = Slate300, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Amount Due", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                    Text("$${String.format("%.2f", draft.total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TealDark)
                                }
                            }
                        }
                    }

                    // Promo Code Input Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Promo or Voucher Code", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = draft.promoInput,
                                        onValueChange = { viewModel.setPromoInput(it) },
                                        placeholder = { Text("Try FRESH20") },
                                        modifier = Modifier.weight(1f).testTag("promo_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = { viewModel.applyPromoCode(draft.promoInput) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }
                                if (draft.promoMessage != null) {
                                    Text(
                                        text = draft.promoMessage!!,
                                        fontSize = 12.sp,
                                        color = if (draft.appliedPromo != null) EmeraldSuccess else RoseAlert,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Payment Gateway Selector
                    item {
                        Text("Select Secure Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    }

                    item {
                        PaymentMethodSelector(
                            selectedMethod = draft.paymentMethod,
                            onSelect = { viewModel.setPaymentMethod(it) }
                        )
                    }

                    // If Credit Card is selected: show Card preview and fields
                    if (draft.paymentMethod == PaymentMethodType.CREDIT_CARD) {
                        item {
                            CreditCardPreview(card = draft.cardDetails)
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = draft.cardDetails.cardNumber,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }.take(16)
                                            val formatted = digits.chunked(4).joinToString(" ")
                                            viewModel.updateCardDetails { it.copy(cardNumber = formatted) }
                                        },
                                        label = { Text("Card Number") },
                                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = TealPrimary) },
                                        modifier = Modifier.fillMaxWidth().testTag("card_number_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = draft.cardDetails.cardHolder,
                                        onValueChange = { name -> viewModel.updateCardDetails { it.copy(cardHolder = name) } },
                                        label = { Text("Cardholder Name") },
                                        modifier = Modifier.fillMaxWidth().testTag("card_holder_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = draft.cardDetails.expiry,
                                            onValueChange = { input ->
                                                val digits = input.filter { it.isDigit() }.take(4)
                                                val formatted = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
                                                viewModel.updateCardDetails { it.copy(expiry = formatted) }
                                            },
                                            label = { Text("MM/YY") },
                                            modifier = Modifier.weight(1f).testTag("card_expiry_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = draft.cardDetails.cvv,
                                            onValueChange = { input ->
                                                val digits = input.filter { it.isDigit() }.take(4)
                                                viewModel.updateCardDetails { it.copy(cvv = digits) }
                                            },
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f).testTag("card_cvv_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                        Text("Encrypted with TLS 1.3 & Level 1 PCI-DSS compliant", fontSize = 11.sp, color = Slate500)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
