package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LaundryRepository {

    private val _activeOrder = MutableStateFlow<LaundryOrder?>(null)
    val activeOrder: StateFlow<LaundryOrder?> = _activeOrder.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<LaundryOrder>>(emptyList())
    val orderHistory: StateFlow<List<LaundryOrder>> = _orderHistory.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val availablePromoCodes = listOf(
        PromoCode("FRESH20", discountPercent = 0.20, description = "20% off your entire order"),
        PromoCode("CLEAN5", discountAmount = 5.00, description = "$5 off instant discount"),
        PromoCode("SAVE10", discountPercent = 0.10, description = "10% weekend special")
    )

    init {
        // Seed an initial active order in progress so tracking and push notifications are instantly demonstrable
        val initialOrder = LaundryOrder(
            orderId = "FF-9024",
            items = listOf(
                ServiceItem(ServiceType.WASH_AND_FOLD, quantity = 4), // 4 kg
                ServiceItem(ServiceType.WASH_AND_PRESS, quantity = 3), // 3 shirts
                ServiceItem(ServiceType.DRY_CLEANING, quantity = 1)   // 1 blazer
            ),
            pickupDate = "Today, Oct 26",
            pickupSlot = "08:00 AM - 10:00 AM",
            deliveryDate = "Tomorrow, Oct 27",
            deliverySlot = "02:00 PM - 04:00 PM",
            isExpress = false,
            address = "742 Evergreen Terrace, Apt 4B",
            apartmentOrNotes = "Gate code: #4921. Ring bell.",
            preferences = LaundryPreferences(
                detergentType = "Eco Fragrance-Free",
                fabricSoftener = true,
                packagingPreference = "Neatly Folded",
                specialInstructions = "Please use gentle cycle for silk blouse"
            ),
            status = OrderStatus.WASHING,
            paymentMethod = PaymentMethodType.CREDIT_CARD,
            paymentReference = "TXN-88491",
            isPaid = true,
            subtotal = 30.47,
            discount = 6.09,
            expressFee = 0.0,
            serviceFee = 1.99,
            totalAmount = 26.37,
            driver = DriverInfo(
                name = "Marcus Vance",
                phone = "+1 (555) 234-8901",
                vehicleModel = "Eco Electric Van",
                licensePlate = "7XG-412",
                rating = 4.9,
                avatarInitials = "MV",
                currentProgressOnRoute = 0.60f
            ),
            createdAt = "Oct 26, 08:15 AM",
            estimatedDeliveryTime = "Tomorrow, Oct 27, 02:00 PM - 04:00 PM"
        )

        _activeOrder.value = initialOrder

        val pastOrder1 = LaundryOrder(
            orderId = "FF-8721",
            items = listOf(
                ServiceItem(ServiceType.WASH_AND_FOLD, quantity = 6),
                ServiceItem(ServiceType.BEDDING_LINENS, quantity = 1)
            ),
            pickupDate = "Oct 20",
            pickupSlot = "11:00 AM - 01:00 PM",
            deliveryDate = "Oct 21",
            deliverySlot = "05:00 PM - 07:00 PM",
            isExpress = false,
            address = "742 Evergreen Terrace, Apt 4B",
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethodType.GOOGLE_PAY,
            paymentReference = "GPAY-99214",
            isPaid = true,
            subtotal = 27.00,
            discount = 0.0,
            expressFee = 0.0,
            serviceFee = 1.99,
            totalAmount = 28.99,
            driver = DriverInfo("Sarah Jenkins", "+1 (555) 881-2244", "Toyota Prius", "9KL-551", 4.95, 840, "SJ"),
            createdAt = "Oct 20, 10:45 AM",
            estimatedDeliveryTime = "Oct 21, 05:00 PM"
        )

        val pastOrder2 = LaundryOrder(
            orderId = "FF-8110",
            items = listOf(
                ServiceItem(ServiceType.SNEAKER_CARE, quantity = 2),
                ServiceItem(ServiceType.DRY_CLEANING, quantity = 2)
            ),
            pickupDate = "Oct 12",
            pickupSlot = "02:00 PM - 04:00 PM",
            deliveryDate = "Oct 14",
            deliverySlot = "02:00 PM - 04:00 PM",
            isExpress = true,
            address = "742 Evergreen Terrace, Apt 4B",
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethodType.CREDIT_CARD,
            paymentReference = "TXN-71822",
            isPaid = true,
            subtotal = 46.98,
            discount = 5.00,
            expressFee = 6.00,
            serviceFee = 1.99,
            totalAmount = 49.97,
            driver = DriverInfo("David Miller", "+1 (555) 301-4492", "Ford Transit", "3XY-901", 4.88, 520, "DM"),
            createdAt = "Oct 12, 01:10 PM",
            estimatedDeliveryTime = "Oct 14, 02:00 PM"
        )

        _orderHistory.value = listOf(pastOrder1, pastOrder2)

        _notifications.value = listOf(
            AppNotification(
                id = "notif-1",
                title = "Order #FF-9024 in Wash Cycle",
                message = "Your garments are currently undergoing our gentle 40°C Eco cycle with hypoallergenic care.",
                orderId = "FF-9024",
                timeAgo = "10 mins ago",
                isRead = false,
                status = OrderStatus.WASHING
            ),
            AppNotification(
                id = "notif-2",
                title = "Laundry Collected",
                message = "Driver Marcus collected 3 bags from 742 Evergreen Terrace.",
                orderId = "FF-9024",
                timeAgo = "45 mins ago",
                isRead = true,
                status = OrderStatus.PICKED_UP
            ),
            AppNotification(
                id = "notif-3",
                title = "Order #FF-8721 Delivered",
                message = "Your fresh laundry was delivered successfully on Oct 21.",
                orderId = "FF-8721",
                timeAgo = "5 days ago",
                isRead = true,
                status = OrderStatus.DELIVERED
            )
        )
    }

    fun validatePromoCode(code: String): PromoCode? {
        val clean = code.trim().uppercase()
        return availablePromoCodes.find { it.code == clean }
    }

    fun placeOrder(order: LaundryOrder) {
        _activeOrder.value = order
        val newNotif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            title = "Order Placed Successfully",
            message = "Booking #${order.orderId} is confirmed for pickup on ${order.pickupDate} (${order.pickupSlot}).",
            orderId = order.orderId,
            timeAgo = "Just now",
            isRead = false,
            status = OrderStatus.PLACED
        )
        _notifications.update { listOf(newNotif) + it }
    }

    fun advanceActiveOrderStatus(): OrderStatus? {
        val current = _activeOrder.value ?: return null
        val next = current.status.nextStatus() ?: return null

        val updatedProgress = when (next) {
            OrderStatus.PLACED -> 0.05f
            OrderStatus.DRIVER_ASSIGNED -> 0.25f
            OrderStatus.PICKED_UP -> 0.45f
            OrderStatus.WASHING -> 0.60f
            OrderStatus.QUALITY_CHECK -> 0.75f
            OrderStatus.OUT_FOR_DELIVERY -> 0.90f
            OrderStatus.DELIVERED -> 1.0f
        }

        val updatedOrder = current.copy(
            status = next,
            driver = current.driver.copy(currentProgressOnRoute = updatedProgress)
        )
        _activeOrder.value = updatedOrder

        val notif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            title = "Status: ${next.displayTitle}",
            message = next.detailedStatus,
            orderId = current.orderId,
            timeAgo = "Just now",
            isRead = false,
            status = next
        )
        _notifications.update { listOf(notif) + it }

        if (next == OrderStatus.DELIVERED) {
            // Move to history if delivered
            _orderHistory.update { listOf(updatedOrder) + it }
        }

        return next
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }
}
