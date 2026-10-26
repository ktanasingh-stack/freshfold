package com.example.model

enum class OrderStatus(
    val stepIndex: Int,
    val displayTitle: String,
    val detailedStatus: String,
    val estimateText: String
) {
    PLACED(
        stepIndex = 0,
        displayTitle = "Order Placed",
        detailedStatus = "Your booking is confirmed. Pickup slot reserved.",
        estimateText = "Next: Driver pickup"
    ),
    DRIVER_ASSIGNED(
        stepIndex = 1,
        displayTitle = "Driver En Route",
        detailedStatus = "Driver is heading to your address for laundry collection.",
        estimateText = "Est. arrival: 10 mins"
    ),
    PICKED_UP(
        stepIndex = 2,
        displayTitle = "Laundry Collected",
        detailedStatus = "Items collected, tagged, and transported to FreshFold Hub.",
        estimateText = "Arrived at sorting facility"
    ),
    WASHING(
        stepIndex = 3,
        displayTitle = "Washing & Treating",
        detailedStatus = "Fabrics sorted by care label, stain-treated and sanitised.",
        estimateText = "In wash cycle (40°C Eco)"
    ),
    QUALITY_CHECK(
        stepIndex = 4,
        displayTitle = "Steam Press & Fold",
        detailedStatus = "Garments inspected, wrinkle-steamed, and neatly packed.",
        estimateText = "Final quality approval passed"
    ),
    OUT_FOR_DELIVERY(
        stepIndex = 5,
        displayTitle = "Out for Delivery",
        detailedStatus = "Your fresh clothes are in the delivery van on the way to you.",
        estimateText = "Est. arrival: 15 mins"
    ),
    DELIVERED(
        stepIndex = 6,
        displayTitle = "Delivered Fresh",
        detailedStatus = "Delivered to your door. Enjoy crisp, fresh clothes!",
        estimateText = "Order complete"
    );

    val isCompleted: Boolean
        get() = this == DELIVERED

    val isInDeliveryOrPickup: Boolean
        get() = this == DRIVER_ASSIGNED || this == OUT_FOR_DELIVERY

    fun nextStatus(): OrderStatus? {
        val nextIdx = this.stepIndex + 1
        return entries.find { it.stepIndex == nextIdx }
    }
}
