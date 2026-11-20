package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TimeSlot(val label: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening");

    companion object {
        fun fromLabel(value: String): TimeSlot =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: MORNING
    }
}

enum class ActivityCategory(val label: String) {
    SIGHTSEEING("Sightseeing"),
    FOOD("Food & Dining"),
    STAY("Stay"),
    TRANSIT("Transit");

    companion object {
        fun fromLabel(value: String): ActivityCategory =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: SIGHTSEEING
    }
}

enum class ExpenseCategory(val label: String) {
    FLIGHTS_TRANSIT("Flights & Transit"),
    HOTELS_STAYS("Hotels & Stays"),
    FOOD_DINING("Food & Dining"),
    SIGHTSEEING_TICKETS("Sightseeing & Tickets"),
    SHOPPING_MISC("Shopping & Misc");

    companion object {
        fun fromLabel(value: String): ExpenseCategory =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: SHOPPING_MISC
    }
}

enum class BookingType(val label: String) {
    FLIGHT_TRAIN("Flight / Train"),
    HOTEL_STAY("Hotel Stay"),
    PASSPORT_VISA("Passport & Visa"),
    EMERGENCY_EMBASSY("Emergency & Embassy");

    companion object {
        fun fromLabel(value: String): BookingType =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: FLIGHT_TRAIN
    }
}

enum class PackingCategory(val label: String) {
    DOCUMENTS("Documents & Visa"),
    CLOTHING("Clothing"),
    TECH("Tech & Gadgets"),
    HEALTH("Health & Medicines");

    companion object {
        fun fromLabel(value: String): PackingCategory =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: CLOTHING
    }
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val destinationName: String,
    val country: String,
    val imageUrl: String,
    val startDate: String,
    val endDate: String,
    val travelerCount: Int,
    val totalBudgetUsd: Double,
    val vibe: String,
    val centerLat: Double,
    val centerLng: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trip_days",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class TripDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val dayNumber: Int,
    val title: String
)

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val dayNumber: Int,
    val placeName: String,
    val timeSlot: String,
    val category: String,
    val estimatedCostUsd: Double,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val sortOrder: Int,
    val transitHint: String = ""
)

@Entity(
    tableName = "travelers",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class TravelerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val name: String
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val title: String,
    val amountUsd: Double,
    val category: String,
    val dateLabel: String,
    val paidByName: String,
    val splitAmongCsv: String = ""
)

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val type: String,
    val title: String,
    val referenceCode: String,
    val timingDetails: String,
    val notes: String
)

@Entity(
    tableName = "packing_items",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class PackingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val title: String,
    val category: String,
    val isPacked: Boolean = false
)
