package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (badgeBg, textColor, dotColor) = when (status) {
        OrderStatus.PLACED -> Triple(SkyLight, SkyBlue, SkyBlue)
        OrderStatus.DRIVER_ASSIGNED -> Triple(Color(0xFFEDE9FE), IndigoStage, IndigoStage)
        OrderStatus.PICKED_UP -> Triple(TealLight, TealDark, TealPrimary)
        OrderStatus.WASHING -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), AmberWarning)
        OrderStatus.QUALITY_CHECK -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), SkyBlue)
        OrderStatus.OUT_FOR_DELIVERY -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), Color(0xFFEC4899))
        OrderStatus.DELIVERED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), EmeraldSuccess)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!status.isCompleted) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = alphaPulse))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = status.displayTitle,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
