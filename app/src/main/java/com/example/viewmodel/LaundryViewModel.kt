package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LaundryRepository
import com.example.data.NotificationHelper
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PaymentProcessingState {
    object Idle : PaymentProcessingState()
    data class Processing(val stageMessage: String) : PaymentProcessingState()
    data class Success(val orderId: String, val transactionId: String) : PaymentProcessingState()
    data class Error(val message: String) : PaymentProcessingState()
}

data class BookingDraft(
    val itemQuantities: Map<ServiceType, Int> = mapOf(
        ServiceType.WASH_AND_FOLD to 3,
        ServiceType.WASH_AND_PRESS to 2
    ),
    val pickupDate: DateSlot = SlotConstants.defaultPickupDates[0],
    val pickupSlot: TimeSlot = SlotConstants.timeSlots[0],
    val deliveryDate: DateSlot = SlotConstants.defaultDeliveryDates[0],
    val deliverySlot: TimeSlot = SlotConstants.timeSlots[2],
    val isExpress: Boolean = false,
    val address: String = "742 Evergreen Terrace, Apt 4B",
    val notes: String = "Ring bell. Leave bag with doorman if unavailable.",
    val preferences: LaundryPreferences = LaundryPreferences(),
    val paymentMethod: PaymentMethodType = PaymentMethodType.CREDIT_CARD,
    val cardDetails: CardDetails = CardDetails(
        cardNumber = "4532 8920 1192 4812",
        cardHolder = "Alex Morgan",
        expiry = "08/28",
        cvv = "842"
    ),
    val appliedPromo: PromoCode? = null,
    val promoInput: String = "FRESH20",
    val promoMessage: String? = null
) {
    val itemsList: List<ServiceItem>
        get() = itemQuantities.filter { it.value > 0 }.map { ServiceItem(it.key, it.value) }

    val subtotal: Double
        get() = itemsList.sumOf { it.totalPrice }

    val expressFee: Double
        get() = if (isExpress) 6.00 else 0.00

    val serviceFee: Double
        get() = if (itemsList.isEmpty()) 0.00 else 1.99

    val discount: Double
        get() {
            val promo = appliedPromo ?: return 0.0
            return if (promo.discountPercent > 0) {
                subtotal * promo.discountPercent
            } else {
                promo.discountAmount.coerceAtMost(subtotal)
            }
        }

    val total: Double
        get() = (subtotal - discount + expressFee + serviceFee).coerceAtLeast(0.0)

    val canProceedToSlot: Boolean
        get() = itemsList.isNotEmpty()

    val canProceedToPayment: Boolean
        get() = address.isNotBlank()
}

class LaundryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LaundryRepository()
    private val notificationHelper = NotificationHelper(application.applicationContext)

    val activeOrder: StateFlow<LaundryOrder?> = repository.activeOrder
    val orderHistory: StateFlow<List<LaundryOrder>> = repository.orderHistory
    val notifications: StateFlow<List<AppNotification>> = repository.notifications

    private val _bookingDraft = MutableStateFlow(BookingDraft())
    val bookingDraft: StateFlow<BookingDraft> = _bookingDraft.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentProcessingState>(PaymentProcessingState.Idle)
    val paymentState: StateFlow<PaymentProcessingState> = _paymentState.asStateFlow()

    init {
        // Apply default FRESH20 promo initially so users immediately get a nice deal!
        applyPromoCode("FRESH20")
    }

    fun incrementItem(serviceType: ServiceType) {
        _bookingDraft.update { draft ->
            val current = draft.itemQuantities[serviceType] ?: 0
            draft.copy(itemQuantities = draft.itemQuantities + (serviceType to (current + 1)))
        }
    }

    fun decrementItem(serviceType: ServiceType) {
        _bookingDraft.update { draft ->
            val current = draft.itemQuantities[serviceType] ?: 0
            if (current > 0) {
                draft.copy(itemQuantities = draft.itemQuantities + (serviceType to (current - 1)))
            } else draft
        }
    }

    fun setPickupDate(slot: DateSlot) {
        _bookingDraft.update { it.copy(pickupDate = slot) }
    }

    fun setPickupSlot(slot: TimeSlot) {
        _bookingDraft.update { it.copy(pickupSlot = slot) }
    }

    fun setDeliveryDate(slot: DateSlot) {
        _bookingDraft.update { it.copy(deliveryDate = slot) }
    }

    fun setDeliverySlot(slot: TimeSlot) {
        _bookingDraft.update { it.copy(deliverySlot = slot) }
    }

    fun setExpress(express: Boolean) {
        _bookingDraft.update { it.copy(isExpress = express) }
    }

    fun setAddress(address: String) {
        _bookingDraft.update { it.copy(address = address) }
    }

    fun setNotes(notes: String) {
        _bookingDraft.update { it.copy(notes = notes) }
    }

    fun updatePreferences(transform: (LaundryPreferences) -> LaundryPreferences) {
        _bookingDraft.update { it.copy(preferences = transform(it.preferences)) }
    }

    fun setPaymentMethod(method: PaymentMethodType) {
        _bookingDraft.update { it.copy(paymentMethod = method) }
    }

    fun updateCardDetails(transform: (CardDetails) -> CardDetails) {
        _bookingDraft.update { it.copy(cardDetails = transform(it.cardDetails)) }
    }

    fun setPromoInput(code: String) {
        _bookingDraft.update { it.copy(promoInput = code) }
    }

    fun applyPromoCode(code: String) {
        val valid = repository.validatePromoCode(code)
        if (valid != null) {
            _bookingDraft.update {
                it.copy(
                    appliedPromo = valid,
                    promoMessage = "Code applied! ${valid.description}"
                )
            }
        } else {
            _bookingDraft.update {
                it.copy(
                    appliedPromo = null,
                    promoMessage = "Invalid promo code. Try 'FRESH20' or 'CLEAN5'"
                )
            }
        }
    }

    fun removePromoCode() {
        _bookingDraft.update { it.copy(appliedPromo = null, promoMessage = null) }
    }

    fun processPaymentAndPlaceOrder(onSuccess: (String) -> Unit) {
        val draft = _bookingDraft.value
        if (draft.itemsList.isEmpty()) return

        viewModelScope.launch {
            _paymentState.value = PaymentProcessingState.Processing("Contacting payment gateway via 256-bit TLS...")
            delay(750)
            _paymentState.value = PaymentProcessingState.Processing("Authorizing token with issuing bank (3DS)...")
            delay(850)
            _paymentState.value = PaymentProcessingState.Processing("Securing pickup slot & allocating driver...")
            delay(600)

            val orderId = "FF-${(1000..9999).random()}"
            val txnId = "TXN-${(10000..99999).random()}"

            val newOrder = LaundryOrder(
                orderId = orderId,
                items = draft.itemsList,
                pickupDate = "${draft.pickupDate.dayName}, ${draft.pickupDate.dateString}",
                pickupSlot = draft.pickupSlot.label,
                deliveryDate = "${draft.deliveryDate.dayName}, ${draft.deliveryDate.dateString}",
                deliverySlot = draft.deliverySlot.label,
                isExpress = draft.isExpress,
                address = draft.address,
                apartmentOrNotes = draft.notes,
                preferences = draft.preferences,
                status = OrderStatus.PLACED,
                paymentMethod = draft.paymentMethod,
                paymentReference = txnId,
                isPaid = true,
                subtotal = draft.subtotal,
                discount = draft.discount,
                expressFee = draft.expressFee,
                serviceFee = draft.serviceFee,
                totalAmount = draft.total,
                driver = DriverInfo(
                    name = "Marcus Vance",
                    phone = "+1 (555) 234-8901",
                    vehicleModel = "Eco Electric Van",
                    licensePlate = "7XG-412",
                    rating = 4.9,
                    avatarInitials = "MV",
                    currentProgressOnRoute = 0.10f
                ),
                createdAt = "Just now",
                estimatedDeliveryTime = "${draft.deliveryDate.dayName}, ${draft.deliverySlot.label}"
            )

            repository.placeOrder(newOrder)

            // Trigger real system push notification!
            notificationHelper.postOrderStatusNotification(
                orderId = orderId,
                status = OrderStatus.PLACED,
                customMessage = "Order #$orderId confirmed! Driver scheduled for pickup on ${newOrder.pickupDate}."
            )

            _paymentState.value = PaymentProcessingState.Success(orderId, txnId)
            delay(600)
            onSuccess(orderId)
            resetPaymentState()
        }
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentProcessingState.Idle
    }

    fun advanceActiveOrderStatus() {
        val next = repository.advanceActiveOrderStatus()
        val order = repository.activeOrder.value
        if (next != null && order != null) {
            notificationHelper.postOrderStatusNotification(
                orderId = order.orderId,
                status = next
            )
        }
    }

    fun sendTestPushNotification() {
        val order = repository.activeOrder.value
        val orderId = order?.orderId ?: "FF-9024"
        notificationHelper.postOrderStatusNotification(
            orderId = orderId,
            status = order?.status ?: OrderStatus.OUT_FOR_DELIVERY,
            customMessage = "FreshFold Alert: Your fresh laundry is ready! Driver Marcus is arriving shortly."
        )
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsRead()
    }

    fun markNotificationRead(id: String) {
        repository.markNotificationRead(id)
    }
}
