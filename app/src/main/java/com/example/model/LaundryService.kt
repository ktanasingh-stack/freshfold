package com.example.model

enum class ServiceType(
    val title: String,
    val subtitle: String,
    val unitLabel: String,
    val basePrice: Double,
    val turnaroundHours: Int,
    val iconName: String
) {
    WASH_AND_FOLD(
        title = "Wash & Fold",
        subtitle = "Everyday clothes, t-shirts, gym wear, towels",
        unitLabel = "per kg",
        basePrice = 2.50,
        turnaroundHours = 24,
        iconName = "LocalLaundryService"
    ),
    WASH_AND_PRESS(
        title = "Wash & Iron",
        subtitle = "Business shirts, trousers, blouses, dresses",
        unitLabel = "per item",
        basePrice = 3.99,
        turnaroundHours = 24,
        iconName = "Iron"
    ),
    DRY_CLEANING(
        title = "Dry Clean",
        subtitle = "Suits, wool coats, silk garments, blazers",
        unitLabel = "per item",
        basePrice = 8.50,
        turnaroundHours = 48,
        iconName = "DryCleaning"
    ),
    BEDDING_LINENS(
        title = "Bedding & Linens",
        subtitle = "Duvets, heavy comforters, bedsheets, blankets",
        unitLabel = "per item",
        basePrice = 12.00,
        turnaroundHours = 36,
        iconName = "Bed"
    ),
    SNEAKER_CARE(
        title = "Sneaker Spa",
        subtitle = "Deep scrub, deodorize, midsole brightening",
        unitLabel = "per pair",
        basePrice = 14.99,
        turnaroundHours = 48,
        iconName = "Checkroom"
    )
}

data class ServiceItem(
    val type: ServiceType,
    val quantity: Int = 0
) {
    val totalPrice: Double
        get() = quantity * type.basePrice
}
