package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTripCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    // Days
    @Query("SELECT * FROM trip_days WHERE tripId = :tripId ORDER BY dayNumber ASC")
    fun observeDaysForTrip(tripId: Long): Flow<List<TripDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: TripDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<TripDayEntity>)

    @Query("DELETE FROM trip_days WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteDay(tripId: Long, dayNumber: Int)

    @Query("DELETE FROM trip_days WHERE tripId = :tripId")
    suspend fun deleteDaysForTrip(tripId: Long)

    // Activities
    @Query("SELECT * FROM activities WHERE tripId = :tripId ORDER BY dayNumber ASC, sortOrder ASC")
    fun observeActivitiesForTrip(tripId: Long): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Long)

    @Query("DELETE FROM activities WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteActivitiesForDay(tripId: Long, dayNumber: Int)

    @Query("DELETE FROM activities WHERE tripId = :tripId")
    suspend fun deleteActivitiesForTrip(tripId: Long)

    // Travelers
    @Query("SELECT * FROM travelers WHERE tripId = :tripId ORDER BY id ASC")
    fun observeTravelersForTrip(tripId: Long): Flow<List<TravelerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraveler(traveler: TravelerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelers(travelers: List<TravelerEntity>)

    @Query("DELETE FROM travelers WHERE id = :travelerId")
    suspend fun deleteTraveler(travelerId: Long)

    // Expenses
    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY id DESC")
    fun observeExpensesForTrip(tripId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    // Bookings
    @Query("SELECT * FROM bookings WHERE tripId = :tripId ORDER BY id ASC")
    fun observeBookingsForTrip(tripId: Long): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteBooking(bookingId: Long)

    // Packing Items
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY id ASC")
    fun observePackingItemsForTrip(tripId: Long): Flow<List<PackingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItems(items: List<PackingItemEntity>)

    @Update
    suspend fun updatePackingItem(item: PackingItemEntity)

    @Query("DELETE FROM packing_items WHERE id = :itemId")
    suspend fun deletePackingItem(itemId: Long)

    @Transaction
    suspend fun replaceGeneratedItinerary(
        tripId: Long,
        days: List<TripDayEntity>,
        activities: List<ActivityEntity>
    ) {
        deleteActivitiesForTrip(tripId)
        deleteDaysForTrip(tripId)
        insertDays(days)
        insertActivities(activities)
    }
}
