package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CardDetails
import com.example.model.PaymentMethodType
import com.example.ui.theme.*
import com.example.viewmodel.PaymentProcessingState

@Composable
fun CreditCardPreview(
    card: CardDetails,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF0F766E)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp)
        ) {
            // Chip and Brand Logo Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold EMV Chip representation
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF59E0B))
                        .border(1.dp, Color(0xFFD97706), RoundedCornerShape(6.dp))
                )
                // Card Brand Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PureWhite.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = card.cardBrand.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Card Number
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = if (card.cardNumber.isBlank()) "•••• •••• •••• ••••" else card.cardNumber,
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }

            // Holder and Expiry Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("CARDHOLDER", color = Slate300, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (card.cardHolder.isBlank()) "YOUR NAME" else card.cardHolder.uppercase(),
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPIRES", color = Slate300, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (card.expiry.isBlank()) "MM/YY" else card.expiry,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethodType,
    onSelect: (PaymentMethodType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PaymentMethodType.entries.forEach { method ->
            val isSelected = method == selectedMethod
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(method) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) TealPrimary else Slate300,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .testTag("payment_method_${method.name.lowercase()}"),
                color = if (isSelected) TealLight.copy(alpha = 0.35f) else PureWhite,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(method) },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Icon(
                        imageVector = when (method) {
                            PaymentMethodType.CREDIT_CARD -> Icons.Default.CreditCard
                            PaymentMethodType.GOOGLE_PAY -> Icons.Default.AccountBalanceWallet
                            PaymentMethodType.CASH_ON_DELIVERY -> Icons.Default.Payments
                        },
                        contentDescription = null,
                        tint = if (isSelected) TealPrimary else Slate700
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = method.title,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900,
                            fontSize = 15.sp
                        )
                        Text(
                            text = method.subtitle,
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentProcessingDialog(
    state: PaymentProcessingState,
    onDismiss: () -> Unit
) {
    if (state is PaymentProcessingState.Idle) return

    Dialog(onDismissRequest = { /* Prevent dismiss during active processing */ }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state) {
                    is PaymentProcessingState.Processing -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            CircularProgressIndicator(
                                color = TealPrimary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Secure Lock",
                                tint = TealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            "Securing Transaction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            text = state.stageMessage,
                            fontSize = 13.sp,
                            color = Slate500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text("256-Bit TLS Bank Encryption", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    is PaymentProcessingState.Success -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = PureWhite, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            "Payment Approved!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Slate900
                        )
                        Text(
                            "Ref: ${state.transactionId}\nOrder #${state.orderId} is confirmed and scheduled.",
                            fontSize = 13.sp,
                            color = Slate700,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    is PaymentProcessingState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RoseAlert),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Error", tint = PureWhite, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            "Payment Failed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            state.message,
                            fontSize = 13.sp,
                            color = Slate700,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Try Again")
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
