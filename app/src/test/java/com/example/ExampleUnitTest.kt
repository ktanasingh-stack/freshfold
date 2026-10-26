package com.example

import com.example.data.LaundryRepository
import com.example.model.OrderStatus
import com.example.model.ServiceItem
import com.example.model.ServiceType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testServicePricingCalculation() {
        val washItem = ServiceItem(ServiceType.WASH_AND_FOLD, quantity = 4)
        assertEquals(10.0, washItem.totalPrice, 0.01)

        val dryCleanItem = ServiceItem(ServiceType.DRY_CLEANING, quantity = 2)
        assertEquals(17.0, dryCleanItem.totalPrice, 0.01)
    }

    @Test
    fun testPromoCodeValidation() {
        val repo = LaundryRepository()
        val promo20 = repo.validatePromoCode("fresh20")
        assertNotNull(promo20)
        assertEquals(0.20, promo20!!.discountPercent, 0.01)

        val promoClean5 = repo.validatePromoCode("clean5")
        assertNotNull(promoClean5)
        assertEquals(5.0, promoClean5!!.discountAmount, 0.01)

        val invalid = repo.validatePromoCode("NONEXISTENT")
        assertNull(invalid)
    }

    @Test
    fun testOrderStatusSequence() {
        assertEquals(OrderStatus.DRIVER_ASSIGNED, OrderStatus.PLACED.nextStatus())
        assertEquals(OrderStatus.PICKED_UP, OrderStatus.DRIVER_ASSIGNED.nextStatus())
        assertEquals(OrderStatus.WASHING, OrderStatus.PICKED_UP.nextStatus())
        assertEquals(OrderStatus.QUALITY_CHECK, OrderStatus.WASHING.nextStatus())
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.QUALITY_CHECK.nextStatus())
        assertEquals(OrderStatus.DELIVERED, OrderStatus.OUT_FOR_DELIVERY.nextStatus())
        assertNull(OrderStatus.DELIVERED.nextStatus())
    }
}
