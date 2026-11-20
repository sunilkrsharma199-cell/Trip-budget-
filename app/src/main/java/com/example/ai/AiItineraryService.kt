package com.example.ai

import android.util.Log
import com.example.data.local.ActivityCategory
import com.example.data.local.ActivityEntity
import com.example.data.local.TimeSlot
import com.example.data.local.TripDayEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiItineraryService"

data class GeneratedItineraryResult(
    val days: List<TripDayEntity>,
    val activities: List<ActivityEntity>
)

class AiItineraryService {

    private val itinerarySchema = Schema.obj(
        properties = mapOf(
            "days" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "dayNumber" to Schema.integer(description = "1-based day index"),
                        "dayTitle" to Schema.string(description = "Short thematic title for the day"),
                        "activities" to Schema.array(
                            items = Schema.obj(
                                properties = mapOf(
                                    "placeName" to Schema.string(description = "Specific landmark, hidden gem, or authentic local eatery"),
                                    "timeSlot" to Schema.enumeration(
                                        values = listOf("Morning", "Afternoon", "Evening"),
                                        description = "Time of day"
                                    ),
                                    "category" to Schema.enumeration(
                                        values = listOf("Sightseeing", "Food & Dining", "Stay", "Transit"),
                                        description = "Activity category"
                                    ),
                                    "estimatedCostUsd" to Schema.double(description = "Estimated per-group cost in USD"),
                                    "notes" to Schema.string(description = "Practical local tip or must-try dish"),
                                    "latitude" to Schema.double(description = "Real WGS84 latitude near destination"),
                                    "longitude" to Schema.double(description = "Real WGS84 longitude near destination"),
                                    "transitHint" to Schema.string(description = "Short transit estimate from previous stop, e.g. '15 min cab • 4.2 km'")
                                )
                            ),
                            description = "3 to 4 ordered stops for this day"
                        )
                    )
                ),
                description = "Day-by-day itinerary list"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = itinerarySchema
                temperature = 0.7f
            }
        )
    }

    suspend fun generateItinerary(
        tripId: Long,
        destinationName: String,
        country: String,
        numDays: Int,
        travelerCount: Int,
        totalBudgetUsd: Double,
        vibe: String,
        centerLat: Double,
        centerLng: Double
    ): GeneratedItineraryResult {
        val safeDays = numDays.coerceIn(1, 7)
        val prompt = buildString {
            appendLine("Create a curated, geographically logical $safeDays-day travel itinerary for $destinationName, $country.")
            appendLine("Travelers: $travelerCount people. Vibe: $vibe. Total trip budget: ~$${totalBudgetUsd.toInt()} USD.")
            appendLine("Reference city center coordinates: ($centerLat, $centerLng).")
            appendLine("For each day, include 3 to 4 ordered stops spanning Morning, Afternoon, and Evening:")
            appendLine("- Mix iconic landmarks, lesser-known hidden gems, and authentic local food spots.")
            appendLine("- Provide accurate real-world latitude and longitude coordinates near $destinationName so map pins align accurately.")
            appendLine("- Keep estimatedCostUsd realistic for $travelerCount travelers in USD.")
            appendLine("- Include concise local tips in notes and short transitHint strings between stops.")
        }

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
                ?: throw IllegalStateException("AI returned an empty itinerary response.")

            val root = JSONObject(rawJson)
            val daysArray = root.getJSONArray("days")
            val parsedDays = mutableListOf<TripDayEntity>()
            val parsedActivities = mutableListOf<ActivityEntity>()

            for (i in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(i)
                val dayNum = dayObj.optInt("dayNumber", i + 1).coerceAtLeast(1)
                val dayTitle = dayObj.optString("dayTitle", "Day $dayNum Explore")
                parsedDays += TripDayEntity(
                    tripId = tripId,
                    dayNumber = dayNum,
                    title = dayTitle
                )

                val activitiesArr = dayObj.optJSONArray("activities") ?: continue
                for (j in 0 until activitiesArr.length()) {
                    val actObj = activitiesArr.getJSONObject(j)
                    val lat = actObj.optDouble("latitude", centerLat)
                    val lng = actObj.optDouble("longitude", centerLng)
                    parsedActivities += ActivityEntity(
                        tripId = tripId,
                        dayNumber = dayNum,
                        placeName = actObj.optString("placeName", "Local Highlight"),
                        timeSlot = TimeSlot.fromLabel(actObj.optString("timeSlot", "Morning")).label,
                        category = ActivityCategory.fromLabel(actObj.optString("category", "Sightseeing")).label,
                        estimatedCostUsd = actObj.optDouble("estimatedCostUsd", 15.0).coerceAtLeast(0.0),
                        notes = actObj.optString("notes", ""),
                        latitude = if (lat.isNaN() || lat == 0.0) centerLat else lat,
                        longitude = if (lng.isNaN() || lng == 0.0) centerLng else lng,
                        sortOrder = j,
                        transitHint = actObj.optString("transitHint", "")
                    )
                }
            }

            if (parsedDays.isEmpty() || parsedActivities.isEmpty()) {
                throw IllegalStateException("Generated itinerary contained no activities.")
            }

            return GeneratedItineraryResult(days = parsedDays, activities = parsedActivities)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI itinerary generation timed out", e)
            throw IllegalStateException("AI itinerary generation timed out after 60 seconds. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI itinerary generation failed", e)
            throw IllegalStateException(
                e.localizedMessage ?: "Failed to generate itinerary with Gemini.",
                e
            )
        }
    }
}
