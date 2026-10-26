package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.OrderStatus

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "freshfold_order_channel"
        const val CHANNEL_NAME = "Order Status Updates"
        const val NOTIFICATION_ID_BASE = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for laundry pickup, cleaning stages, and delivery arrival"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun postOrderStatusNotification(
        orderId: String,
        status: OrderStatus,
        customMessage: String? = null
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("TRACK_ORDER_ID", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = when (status) {
            OrderStatus.PLACED -> "Order Confirmed: #$orderId"
            OrderStatus.DRIVER_ASSIGNED -> "Driver Assigned for Pickup"
            OrderStatus.PICKED_UP -> "Laundry Picked Up!"
            OrderStatus.WASHING -> "Clothes Washing & Treating"
            OrderStatus.QUALITY_CHECK -> "Quality Press & Inspection"
            OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery! \uD83D\uDE9A"
            OrderStatus.DELIVERED -> "Order Delivered Fresh! \u2728"
        }

        val body = customMessage ?: status.detailedStatus

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + status.stepIndex, builder.build())
        } catch (_: SecurityException) {
            // Permission revoked or not granted
        }
    }
}
