package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaundryOrder
import com.example.model.OrderStatus
import com.example.ui.theme.*

@Composable
fun LiveTrackingMap(
    order: LaundryOrder,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val progress = order.driver.currentProgressOnRoute.coerceIn(0.05f, 0.95f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE2E8F0))
            .testTag("live_tracking_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw stylized background terrain & roads
            drawRect(color = Color(0xFFE8EEF5))

            // Soft park area
            drawCircle(
                color = Color(0xFFD1FAE5),
                radius = width * 0.35f,
                center = Offset(width * 0.85f, height * 0.15f)
            )

            // Street lines
            val roadColor = Color(0xFFFFFFFF)
            val roadStroke = 12f
            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.3f),
                end = Offset(width, height * 0.3f),
                strokeWidth = roadStroke
            )
            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.7f),
                end = Offset(width, height * 0.7f),
                strokeWidth = roadStroke
            )
            drawLine(
                color = roadColor,
                start = Offset(width * 0.35f, 0f),
                end = Offset(width * 0.35f, height),
                strokeWidth = roadStroke
            )
            drawLine(
                color = roadColor,
                start = Offset(width * 0.72f, 0f),
                end = Offset(width * 0.72f, height),
                strokeWidth = roadStroke
            )

            // 2. Hub coordinates & Home destination
            val hubX = width * 0.16f
            val hubY = height * 0.68f

            val homeX = width * 0.84f
            val homeY = height * 0.32f

            // 3. Navigation Route Path (Smooth S-curve)
            val routePath = Path().apply {
                moveTo(hubX, hubY)
                cubicTo(
                    width * 0.38f, hubY - 10f,
                    width * 0.60f, homeY + 15f,
                    homeX, homeY
                )
            }

            // Draw route shadow
            drawPath(
                path = routePath,
                color = TealDark.copy(alpha = 0.25f),
                style = Stroke(width = 8f)
            )
            // Draw active route
            drawPath(
                path = routePath,
                color = TealPrimary,
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                )
            )

            // 4. Calculate driver point along curve approximately
            // Parametric cubic bezier: B(t) = (1-t)^3 P0 + 3(1-t)^2 t P1 + 3(1-t) t^2 P2 + t^3 P3
            val t = progress
            val p0X = hubX; val p0Y = hubY
            val p1X = width * 0.38f; val p1Y = hubY - 10f
            val p2X = width * 0.60f; val p2Y = homeY + 15f
            val p3X = homeX; val p3Y = homeY

            val oneMinusT = 1f - t
            val driverX = (oneMinusT * oneMinusT * oneMinusT * p0X) +
                    (3f * oneMinusT * oneMinusT * t * p1X) +
                    (3f * oneMinusT * t * t * p2X) +
                    (t * t * t * p3X)
            val driverY = (oneMinusT * oneMinusT * oneMinusT * p0Y) +
                    (3f * oneMinusT * oneMinusT * t * p1Y) +
                    (3f * oneMinusT * t * t * p2Y) +
                    (t * t * t * p3Y)

            // 5. Draw Hub Pin
            drawCircle(color = Slate800, radius = 14f, center = Offset(hubX, hubY))
            drawCircle(color = PureWhite, radius = 6f, center = Offset(hubX, hubY))

            // 6. Draw Home Pin
            drawCircle(color = Color(0xFFEF4444), radius = 16f, center = Offset(homeX, homeY))
            drawCircle(color = PureWhite, radius = 7f, center = Offset(homeX, homeY))

            // 7. Draw Driver Pulse & Van Marker
            drawCircle(
                color = SkyBlue.copy(alpha = pulseAlpha),
                radius = pulseRadius * 1.5f,
                center = Offset(driverX, driverY)
            )
            drawCircle(
                color = SkyBlue,
                radius = 16f,
                center = Offset(driverX, driverY)
            )
            drawCircle(
                color = PureWhite,
                radius = 6f,
                center = Offset(driverX, driverY)
            )
        }

        // Hub Label Chip (Bottom Left)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Slate900.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.LocalLaundryService,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Text("FreshFold Hub", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Destination Home Label Chip (Top Right)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEF4444).copy(alpha = 0.90f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(14.dp)
                )
                Text("Your Door", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live Driver ETA Floating Pill (Center Top)
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess)
                )
                Text(
                    text = when (order.status) {
                        OrderStatus.DRIVER_ASSIGNED -> "Driver arriving in 10 mins"
                        OrderStatus.PICKED_UP -> "In transit to cleaning hub"
                        OrderStatus.WASHING -> "Facility cleaning underway"
                        OrderStatus.QUALITY_CHECK -> "Packaging & readying van"
                        OrderStatus.OUT_FOR_DELIVERY -> "Driver arriving in 12 mins"
                        OrderStatus.DELIVERED -> "Delivered to door"
                        else -> "Live Route Scheduled"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
            }
        }
    }
}
