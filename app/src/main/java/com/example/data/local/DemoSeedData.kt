package com.example.data.local

import com.example.model.DestinationCatalog
import com.example.model.TravelVibe

object DemoSeedData {

    suspend fun seedIfEmpty(dao: TripDao): Long? {
        if (dao.getTripCount() > 0) return null

        // Trip 1: Bali Island Escape (International hotspot with global + Indian travelers)
        val baliGuide = DestinationCatalog.findGuideByName("Bali")
        val baliTripId = dao.insertTrip(
            TripEntity(
                title = "Bali Island Escape",
                destinationName = baliGuide.name,
                country = baliGuide.country,
                imageUrl = baliGuide.imageUrl,
                startDate = "May 14",
                endDate = "May 18",
                travelerCount = 3,
                totalBudgetUsd = 1450.0,
                vibe = TravelVibe.BEACH.label,
                centerLat = baliGuide.centerLat,
                centerLng = baliGuide.centerLng,
                createdAt = System.currentTimeMillis()
            )
        )

        dao.insertDays(
            listOf(
                TripDayEntity(tripId = baliTripId, dayNumber = 1, title = "Ubud Culture & Rice Terraces"),
                TripDayEntity(tripId = baliTripId, dayNumber = 2, title = "Waterfalls & Sacred Springs"),
                TripDayEntity(tripId = baliTripId, dayNumber = 3, title = "Uluwatu Cliffs & Sunset")
            )
        )

        dao.insertActivities(
            listOf(
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 1,
                    placeName = "Tegallalang Rice Terraces",
                    timeSlot = TimeSlot.MORNING.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 12.0,
                    notes = "Arrive by 8 AM for golden light and quiet terrace trails.",
                    latitude = -8.4312,
                    longitude = 115.2793,
                    sortOrder = 0
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 1,
                    placeName = "Clear Cafe Ubud",
                    timeSlot = TimeSlot.AFTERNOON.label,
                    category = ActivityCategory.FOOD.label,
                    estimatedCostUsd = 28.0,
                    notes = "Try the Bali Nasi Campur and fresh ginger coconut cooler.",
                    latitude = -8.5069,
                    longitude = 115.2602,
                    sortOrder = 1
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 1,
                    placeName = "Campuhan Ridge Walk",
                    timeSlot = TimeSlot.EVENING.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 0.0,
                    notes = "Scenic sunset stroll above the lush Ayung river valley.",
                    latitude = -8.5042,
                    longitude = 115.2546,
                    sortOrder = 2
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 2,
                    placeName = "Tirta Empul Water Temple",
                    timeSlot = TimeSlot.MORNING.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 10.0,
                    notes = "Sarongs included at entrance; bring spare dry clothes.",
                    latitude = -8.4157,
                    longitude = 115.3156,
                    sortOrder = 0
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 2,
                    placeName = "Tegenungan Waterfall",
                    timeSlot = TimeSlot.AFTERNOON.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 8.0,
                    notes = "Quick 25-min scooter ride south of Ubud center.",
                    latitude = -8.5753,
                    longitude = 115.2886,
                    sortOrder = 1
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 3,
                    placeName = "Uluwatu Cliff Temple",
                    timeSlot = TimeSlot.AFTERNOON.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 15.0,
                    notes = "Book 6 PM Kecak fire dance amphitheater seats early.",
                    latitude = -8.8291,
                    longitude = 115.0849,
                    sortOrder = 0
                ),
                ActivityEntity(
                    tripId = baliTripId,
                    dayNumber = 3,
                    placeName = "Jimbaran Bay Seafood Dinner",
                    timeSlot = TimeSlot.EVENING.label,
                    category = ActivityCategory.FOOD.label,
                    estimatedCostUsd = 45.0,
                    notes = "Beachfront grilled snapper & garlic prawns on the sand.",
                    latitude = -8.7745,
                    longitude = 115.1667,
                    sortOrder = 1
                )
            )
        )

        dao.insertTravelers(
            listOf(
                TravelerEntity(tripId = baliTripId, name = "Alex"),
                TravelerEntity(tripId = baliTripId, name = "Rahul"),
                TravelerEntity(tripId = baliTripId, name = "Priya")
            )
        )

        dao.insertExpenses(
            listOf(
                ExpenseEntity(
                    tripId = baliTripId,
                    title = "Round-trip Flights & Airport Grab",
                    amountUsd = 480.0,
                    category = ExpenseCategory.FLIGHTS_TRANSIT.label,
                    dateLabel = "May 14",
                    paidByName = "Alex"
                ),
                ExpenseEntity(
                    tripId = baliTripId,
                    title = "Ubud Garden Villa (3 Nights)",
                    amountUsd = 330.0,
                    category = ExpenseCategory.HOTELS_STAYS.label,
                    dateLabel = "May 14",
                    paidByName = "Rahul"
                ),
                ExpenseEntity(
                    tripId = baliTripId,
                    title = "Jimbaran Beach Seafood Feast",
                    amountUsd = 96.0,
                    category = ExpenseCategory.FOOD_DINING.label,
                    dateLabel = "May 16",
                    paidByName = "Priya"
                ),
                ExpenseEntity(
                    tripId = baliTripId,
                    title = "Uluwatu Kecak Dance & Temple Passes",
                    amountUsd = 54.0,
                    category = ExpenseCategory.SIGHTSEEING_TICKETS.label,
                    dateLabel = "May 16",
                    paidByName = "Alex"
                ),
                ExpenseEntity(
                    tripId = baliTripId,
                    title = "Ubud Art Market Handicrafts & SIM",
                    amountUsd = 60.0,
                    category = ExpenseCategory.SHOPPING_MISC.label,
                    dateLabel = "May 15",
                    paidByName = "Rahul"
                )
            )
        )

        dao.insertBookings(
            listOf(
                BookingEntity(
                    tripId = baliTripId,
                    type = BookingType.FLIGHT_TRAIN.label,
                    title = "Singapore Airlines SQ 938 (SIN → DPS)",
                    referenceCode = "PNR-BLI842K",
                    timingDetails = "Dep 09:20 AM • Arr 12:05 PM",
                    notes = "Terminal 3 • 30 kg checked baggage included"
                ),
                BookingEntity(
                    tripId = baliTripId,
                    type = BookingType.HOTEL_STAY.label,
                    title = "Alaya Resort Ubud",
                    referenceCode = "CONF-992140",
                    timingDetails = "Check-in May 14, 2:00 PM • Out May 17",
                    notes = "Jl. Hanoman, Ubud • Free breakfast & welcome drink"
                ),
                BookingEntity(
                    tripId = baliTripId,
                    type = BookingType.PASSPORT_VISA.label,
                    title = "Indonesia e-VOA & All-Indonesia QR",
                    referenceCode = "EVOA-4481920",
                    timingDetails = "Valid 30 days from arrival",
                    notes = "Ensure passport has 6+ months validity & 2 blank pages"
                ),
                BookingEntity(
                    tripId = baliTripId,
                    type = BookingType.EMERGENCY_EMBASSY.label,
                    title = "Bali Tourist Police & Consulate",
                    referenceCode = "+62 361 224111",
                    timingDetails = "24/7 Emergency Helpline",
                    notes = "BIMC Hospital Kuta: +62 361 761263"
                )
            )
        )

        dao.insertPackingItems(defaultPackingItems(baliTripId, isBeach = true))

        // Trip 2: Manali & Solang Adventure (Indian hotspot)
        val manaliGuide = DestinationCatalog.findGuideByName("Manali")
        val manaliTripId = dao.insertTrip(
            TripEntity(
                title = "Manali & Solang Escape",
                destinationName = manaliGuide.name,
                country = manaliGuide.country,
                imageUrl = manaliGuide.imageUrl,
                startDate = "Oct 10",
                endDate = "Oct 13",
                travelerCount = 2,
                totalBudgetUsd = 320.0,
                vibe = TravelVibe.MOUNTAIN.label,
                centerLat = manaliGuide.centerLat,
                centerLng = manaliGuide.centerLng,
                createdAt = System.currentTimeMillis() - 60_000L
            )
        )

        dao.insertDays(
            listOf(
                TripDayEntity(tripId = manaliTripId, dayNumber = 1, title = "Old Manali & Hidimba Temple"),
                TripDayEntity(tripId = manaliTripId, dayNumber = 2, title = "Solang Valley & Atal Tunnel")
            )
        )

        dao.insertActivities(
            listOf(
                ActivityEntity(
                    tripId = manaliTripId,
                    dayNumber = 1,
                    placeName = "Hidimba Devi Temple",
                    timeSlot = TimeSlot.MORNING.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 2.0,
                    notes = "Cedar forest walk around the 16th-century wooden pagoda.",
                    latitude = 32.2484,
                    longitude = 77.1809,
                    sortOrder = 0
                ),
                ActivityEntity(
                    tripId = manaliTripId,
                    dayNumber = 1,
                    placeName = "Cafe 1947 Old Manali",
                    timeSlot = TimeSlot.AFTERNOON.label,
                    category = ActivityCategory.FOOD.label,
                    estimatedCostUsd = 16.0,
                    notes = "Riverside Himalayan trout & wood-fired pizza.",
                    latitude = 32.2571,
                    longitude = 77.1778,
                    sortOrder = 1
                ),
                ActivityEntity(
                    tripId = manaliTripId,
                    dayNumber = 2,
                    placeName = "Solang Valley Ropeway",
                    timeSlot = TimeSlot.MORNING.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 18.0,
                    notes = "Book morning gondola slot for clear snow-peak views.",
                    latitude = 32.3156,
                    longitude = 77.1573,
                    sortOrder = 0
                ),
                ActivityEntity(
                    tripId = manaliTripId,
                    dayNumber = 2,
                    placeName = "Atal Tunnel & Sissu Lake",
                    timeSlot = TimeSlot.AFTERNOON.label,
                    category = ActivityCategory.SIGHTSEEING.label,
                    estimatedCostUsd = 14.0,
                    notes = "Cross into Lahaul valley for waterfall views & Maggi stop.",
                    latitude = 32.4785,
                    longitude = 77.1294,
                    sortOrder = 1
                )
            )
        )

        dao.insertTravelers(
            listOf(
                TravelerEntity(tripId = manaliTripId, name = "Rahul"),
                TravelerEntity(tripId = manaliTripId, name = "Priya")
            )
        )

        dao.insertExpenses(
            listOf(
                ExpenseEntity(
                    tripId = manaliTripId,
                    title = "HRTC Volvo Sleeper (Delhi → Manali)",
                    amountUsd = 42.0,
                    category = ExpenseCategory.FLIGHTS_TRANSIT.label,
                    dateLabel = "Oct 10",
                    paidByName = "Rahul"
                ),
                ExpenseEntity(
                    tripId = manaliTripId,
                    title = "Old Manali Pine Chalet (3 Nights)",
                    amountUsd = 110.0,
                    category = ExpenseCategory.HOTELS_STAYS.label,
                    dateLabel = "Oct 10",
                    paidByName = "Priya"
                ),
                ExpenseEntity(
                    tripId = manaliTripId,
                    title = "River Cafe Trout & Hot Chocolate",
                    amountUsd = 26.0,
                    category = ExpenseCategory.FOOD_DINING.label,
                    dateLabel = "Oct 11",
                    paidByName = "Rahul"
                )
            )
        )

        dao.insertBookings(
            listOf(
                BookingEntity(
                    tripId = manaliTripId,
                    type = BookingType.FLIGHT_TRAIN.label,
                    title = "HRTC Himsuta Volvo (ISBT Kashmiri Gate)",
                    referenceCode = "HRTC-774129",
                    timingDetails = "Dep 08:30 PM • Arr 08:15 AM",
                    notes = "Boarding Bay 4 • Carry warm fleece for AC cabin"
                ),
                BookingEntity(
                    tripId = manaliTripId,
                    type = BookingType.HOTEL_STAY.label,
                    title = "The Pinewood Chalet Old Manali",
                    referenceCode = "PMC-30812",
                    timingDetails = "Check-in Oct 10, 12:00 PM • Out Oct 13",
                    notes = "Manu Temple Road • Heater included"
                )
            )
        )

        dao.insertPackingItems(defaultPackingItems(manaliTripId, isBeach = false))

        return baliTripId
    }

    fun defaultPackingItems(tripId: Long, isBeach: Boolean): List<PackingItemEntity> = listOf(
        PackingItemEntity(tripId = tripId, title = "Passport / Govt Photo ID & Copies", category = PackingCategory.DOCUMENTS.label, isPacked = true),
        PackingItemEntity(tripId = tripId, title = "Flight / Train PNR & Hotel Vouchers", category = PackingCategory.DOCUMENTS.label, isPacked = true),
        PackingItemEntity(tripId = tripId, title = "Travel Insurance & Forex / UPI Card", category = PackingCategory.DOCUMENTS.label, isPacked = false),
        PackingItemEntity(
            tripId = tripId,
            title = if (isBeach) "Breathable Linen Shirts & Swimwear" else "Thermal Layers & Windproof Jacket",
            category = PackingCategory.CLOTHING.label,
            isPacked = true
        ),
        PackingItemEntity(
            tripId = tripId,
            title = if (isBeach) "Polarized Sunglasses & Reef-Safe Hat" else "Woolen Beanie & Hiking Shoes",
            category = PackingCategory.CLOTHING.label,
            isPacked = false
        ),
        PackingItemEntity(tripId = tripId, title = "20,000mAh Power Bank & Universal Adapter", category = PackingCategory.TECH.label, isPacked = true),
        PackingItemEntity(tripId = tripId, title = "Offline Maps & eSIM QR Backup", category = PackingCategory.TECH.label, isPacked = false),
        PackingItemEntity(tripId = tripId, title = "Personal Meds, Motion Sickness & Band-Aids", category = PackingCategory.HEALTH.label, isPacked = false),
        PackingItemEntity(tripId = tripId, title = "SPF 50 Sunscreen & Hydration Electrolytes", category = PackingCategory.HEALTH.label, isPacked = true)
    )
}
