package com.example.model

data class DateSlot(
    val id: String,
    val dayName: String,     // e.g. "Today", "Tomorrow", "Wed"
    val dateString: String,  // e.g. "Oct 26", "Oct 27"
    val isExpressAllowed: Boolean = true
)

data class TimeSlot(
    val id: String,
    val label: String,       // e.g. "08:00 AM - 10:00 AM"
    val period: String       // e.g. "Morning", "Afternoon", "Evening"
)

object SlotConstants {
    val defaultPickupDates = listOf(
        DateSlot("d1", "Today", "Oct 26"),
        DateSlot("d2", "Tomorrow", "Oct 27"),
        DateSlot("d3", "Mon", "Oct 28"),
        DateSlot("d4", "Tue", "Oct 29"),
        DateSlot("d5", "Wed", "Oct 30")
    )

    val defaultDeliveryDates = listOf(
        DateSlot("dd1", "Tomorrow", "Oct 27", isExpressAllowed = true),
        DateSlot("dd2", "Mon", "Oct 28"),
        DateSlot("dd3", "Tue", "Oct 29"),
        DateSlot("dd4", "Wed", "Oct 30"),
        DateSlot("dd5", "Thu", "Oct 31")
    )

    val timeSlots = listOf(
        TimeSlot("t1", "08:00 AM - 10:00 AM", "Morning"),
        TimeSlot("t2", "11:00 AM - 01:00 PM", "Mid-Day"),
        TimeSlot("t3", "02:00 PM - 04:00 PM", "Afternoon"),
        TimeSlot("t4", "05:00 PM - 07:00 PM", "Evening"),
        TimeSlot("t5", "07:00 PM - 09:00 PM", "Night")
    )
}
