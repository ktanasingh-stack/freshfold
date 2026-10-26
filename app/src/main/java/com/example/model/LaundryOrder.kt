package com.example.model

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val orderId: String,
    val timeAgo: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val status: OrderStatus? = null
)

data class LaundryPreferences(
    val detergentType: String = "Eco Fragrance-Free", // Eco Fragrance-Free, Lavender Bliss, Hypoallergenic
    val fabricSoftener: Boolean = true,
    val waterTemp: String = "Cold / 30°C (Eco)",
    val packagingPreference: String = "Neatly Folded", // Neatly Folded, On Hangers
    val specialInstructions: String = ""
)

data class LaundryOrder(
    val orderId: String,
    val items: List<ServiceItem>,
    val pickupDate: String,
    val pickupSlot: String,
    val deliveryDate: String,
    val deliverySlot: String,
    val isExpress: Boolean = false,
    val address: String,
    val apartmentOrNotes: String = "",
    val preferences: LaundryPreferences = LaundryPreferences(),
    val status: OrderStatus = OrderStatus.PLACED,
    val paymentMethod: PaymentMethodType = PaymentMethodType.CREDIT_CARD,
    val paymentReference: String = "TXN-88491",
    val isPaid: Boolean = true,
    val subtotal: Double,
    val discount: Double = 0.0,
    val expressFee: Double = 0.0,
    val serviceFee: Double = 1.99,
    val totalAmount: Double,
    val driver: DriverInfo = DriverInfo(),
    val createdAt: String = "Oct 26, 09:30 AM",
    val estimatedDeliveryTime: String = "Oct 27, 02:00 PM - 04:00 PM"
) {
    val totalItemCount: Int
        get() = items.sumOf { it.quantity }
}
