package com.example.data.repository

import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.DemoSeedData
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingItemEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDao
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.model.DestinationGuide
import kotlinx.coroutines.flow.Flow

class TripRepository(private val dao: TripDao) {

    fun observeTrips(): Flow<List<TripEntity>> = dao.observeTrips()

    fun observeDays(tripId: Long): Flow<List<TripDayEntity>> = dao.observeDaysForTrip(tripId)

    fun observeActivities(tripId: Long): Flow<List<ActivityEntity>> = dao.observeActivitiesForTrip(tripId)

    fun observeTravelers(tripId: Long): Flow<List<TravelerEntity>> = dao.observeTravelersForTrip(tripId)

    fun observeExpenses(tripId: Long): Flow<List<ExpenseEntity>> = dao.observeExpensesForTrip(tripId)

    fun observeBookings(tripId: Long): Flow<List<BookingEntity>> = dao.observeBookingsForTrip(tripId)

    fun observePackingItems(tripId: Long): Flow<List<PackingItemEntity>> = dao.observePackingItemsForTrip(tripId)

    suspend fun ensureDemoDataSeeded(): Long? = DemoSeedData.seedIfEmpty(dao)

    suspend fun createTripFromDestination(
        guide: DestinationGuide,
        durationDays: Int,
        budgetUsd: Double,
        travelerNames: List<String> = listOf("You", "Travel Buddy")
    ): Long {
        val safeDays = durationDays.coerceIn(1, 14)
        val tripId = dao.insertTrip(
            TripEntity(
                title = "${guide.name} Getaway",
                destinationName = guide.name,
                country = guide.country,
                imageUrl = guide.imageUrl,
                startDate = "Day 1",
                endDate = "Day $safeDays",
                travelerCount = travelerNames.size.coerceAtLeast(1),
                totalBudgetUsd = budgetUsd.coerceAtLeast(50.0),
                vibe = guide.vibe.label,
                centerLat = guide.centerLat,
                centerLng = guide.centerLng
            )
        )

        val days = (1..safeDays).map { day ->
            TripDayEntity(
                tripId = tripId,
                dayNumber = day,
                title = if (day == 1) "Arrival & ${guide.highlights.firstOrNull() ?: guide.name}"
                else guide.highlights.getOrNull(day - 1) ?: "Local Discovery Day $day"
            )
        }
        dao.insertDays(days)

        // Seed initial anchor highlights on Day 1 so the route/map immediately shows pins
        val initialActivities = guide.highlights.take(3).mapIndexed { index, highlight ->
            val offset = (index - 1) * 0.018
            ActivityEntity(
                tripId = tripId,
                dayNumber = 1,
                placeName = highlight,
                timeSlot = when (index) {
                    0 -> "Morning"
                    1 -> "Afternoon"
                    else -> "Evening"
                },
                category = if (index == 1) "Food & Dining" else "Sightseeing",
                estimatedCostUsd = (guide.dailyAvgUsd * 0.25).coerceAtLeast(5.0),
                notes = guide.transportTip,
                latitude = guide.centerLat + offset,
                longitude = guide.centerLng + (offset * 0.8),
                sortOrder = index
            )
        }
        dao.insertActivities(initialActivities)

        dao.insertTravelers(travelerNames.map { TravelerEntity(tripId = tripId, name = it) })
        dao.insertPackingItems(
            DemoSeedData.defaultPackingItems(
                tripId = tripId,
                isBeach = guide.vibe.name == "BEACH"
            )
        )
        return tripId
    }

    suspend fun createBlankTrip(
        title: String,
        destinationName: String,
        country: String,
        daysCount: Int,
        budgetUsd: Double,
        travelersCount: Int
    ): Long {
        val guide = DestinationCatalogHelper.findGuide(destinationName)
        return createTripFromDestination(
            guide = guide.copy(
                name = destinationName.ifBlank { guide.name },
                country = country.ifBlank { guide.country }
            ),
            durationDays = daysCount,
            budgetUsd = budgetUsd,
            travelerNames = (1..travelersCount.coerceIn(1, 6)).map { idx ->
                if (idx == 1) "You" else "Traveler $idx"
            }
        ).also { newId ->
            dao.updateTrip(
                TripEntity(
                    id = newId,
                    title = title.ifBlank { "$destinationName Trip" },
                    destinationName = destinationName.ifBlank { guide.name },
                    country = country.ifBlank { guide.country },
                    imageUrl = guide.imageUrl,
                    startDate = "Day 1",
                    endDate = "Day ${daysCount.coerceIn(1, 14)}",
                    travelerCount = travelersCount.coerceIn(1, 6),
                    totalBudgetUsd = budgetUsd.coerceAtLeast(50.0),
                    vibe = guide.vibe.label,
                    centerLat = guide.centerLat,
                    centerLng = guide.centerLng
                )
            )
        }
    }

    suspend fun updateTrip(trip: TripEntity) = dao.updateTrip(trip)

    suspend fun deleteTrip(tripId: Long) = dao.deleteTrip(tripId)

    suspend fun addDay(tripId: Long, nextDayNumber: Int) {
        dao.insertDay(
            TripDayEntity(
                tripId = tripId,
                dayNumber = nextDayNumber,
                title = "Day $nextDayNumber Adventure"
            )
        )
    }

    suspend fun deleteDay(tripId: Long, dayNumber: Int) {
        dao.deleteActivitiesForDay(tripId, dayNumber)
        dao.deleteDay(tripId, dayNumber)
    }

    suspend fun saveActivity(activity: ActivityEntity) {
        if (activity.id == 0L) {
            dao.insertActivity(activity)
        } else {
            dao.updateActivity(activity)
        }
    }

    suspend fun deleteActivity(activityId: Long) = dao.deleteActivity(activityId)

    suspend fun replaceItinerary(
        tripId: Long,
        days: List<TripDayEntity>,
        activities: List<ActivityEntity>
    ) = dao.replaceGeneratedItinerary(tripId, days, activities)

    suspend fun addTraveler(tripId: Long, name: String) {
        if (name.isNotBlank()) {
            dao.insertTraveler(TravelerEntity(tripId = tripId, name = name.trim()))
        }
    }

    suspend fun removeTraveler(travelerId: Long) = dao.deleteTraveler(travelerId)

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)

    suspend fun deleteExpense(expenseId: Long) = dao.deleteExpense(expenseId)

    suspend fun saveBooking(booking: BookingEntity) {
        if (booking.id == 0L) dao.insertBooking(booking) else dao.updateBooking(booking)
    }

    suspend fun deleteBooking(bookingId: Long) = dao.deleteBooking(bookingId)

    suspend fun addPackingItem(item: PackingItemEntity) = dao.insertPackingItem(item)

    suspend fun togglePackingItem(item: PackingItemEntity) =
        dao.updatePackingItem(item.copy(isPacked = !item.isPacked))

    suspend fun deletePackingItem(itemId: Long) = dao.deletePackingItem(itemId)
}

private object DestinationCatalogHelper {
    fun findGuide(name: String) = com.example.model.DestinationCatalog.findGuideByName(name)
}
