package com.example.model

data class DriverInfo(
    val name: String = "Marcus Vance",
    val phone: String = "+1 (555) 234-8901",
    val vehicleModel: String = "Eco Electric Van",
    val licensePlate: String = "7XG-412",
    val rating: Double = 4.9,
    val completedTrips: Int = 1240,
    val avatarInitials: String = "MV",
    val currentProgressOnRoute: Float = 0.65f // 0.0 to 1.0 along the route
)

enum class PaymentMethodType(val title: String, val subtitle: String) {
    CREDIT_CARD("Credit / Debit Card", "Instant 256-bit encrypted checkout"),
    GOOGLE_PAY("Google Pay", "Fast, frictionless one-tap payment"),
    CASH_ON_DELIVERY("Pay on Delivery", "Cash or tap card with driver terminal")
}

data class CardDetails(
    val cardNumber: String = "",
    val cardHolder: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val saveCard: Boolean = true
) {
    val cardBrand: String
        get() = when {
            cardNumber.startsWith("4") -> "Visa"
            cardNumber.startsWith("5") -> "Mastercard"
            cardNumber.startsWith("3") -> "Amex"
            else -> "Card"
        }

    val isValid: Boolean
        get() {
            val cleanNumber = cardNumber.replace(" ", "")
            return cleanNumber.length >= 15 && expiry.length >= 5 && cvv.length >= 3 && cardHolder.isNotBlank()
        }
}

data class PromoCode(
    val code: String,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val description: String
)
