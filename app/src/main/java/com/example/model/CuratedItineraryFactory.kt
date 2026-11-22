package com.example.model

import com.example.data.local.ActivityCategory
import com.example.data.local.ActivityEntity
import com.example.data.local.TimeSlot
import com.example.data.local.TripDayEntity

private data class CuratedStopSpec(
    val dayNumber: Int,
    val placeName: String,
    val timeSlot: TimeSlot,
    val category: ActivityCategory,
    val costRatioOfDaily: Double,
    val notes: String,
    val lat: Double,
    val lng: Double,
    val transitHint: String
)

object CuratedItineraryFactory {

    fun buildDaysAndActivities(
        tripId: Long,
        guide: DestinationGuide,
        durationDays: Int,
        startingCity: String
    ): Pair<List<TripDayEntity>, List<ActivityEntity>> {
        val safeDays = durationDays.coerceIn(1, 14)
        val origin = startingCity.trim()
        val stopsByDest = curatedStopsForDestination(guide)

        val days = (1..safeDays).map { dayNum ->
            TripDayEntity(
                tripId = tripId,
                dayNumber = dayNum,
                title = curatedDayTitle(guide, dayNum, origin)
            )
        }

        val activities = mutableListOf<ActivityEntity>()

        for (dayNum in 1..safeDays) {
            val dayStops = mutableListOf<CuratedStopSpec>()

            if (dayNum == 1 && origin.isNotBlank() && !origin.equals(guide.name, ignoreCase = true)) {
                val (hubLat, hubLng) = arrivalHubCoords(guide)
                val arrivalLabel = if (guide.isIndia) {
                    "$origin → ${guide.name} Arrival (Flight / Express Train)"
                } else {
                    "$origin → ${guide.name} International Arrival"
                }
                dayStops += CuratedStopSpec(
                    dayNumber = 1,
                    placeName = arrivalLabel,
                    timeSlot = TimeSlot.MORNING,
                    category = ActivityCategory.TRANSIT,
                    costRatioOfDaily = 0.40,
                    notes = "Depart $origin and arrive in ${guide.name}. ${guide.transportTip}",
                    lat = hubLat,
                    lng = hubLng,
                    transitHint = "$origin → ${guide.name} route"
                )
            }

            val templateForDay = stopsByDest.filter { it.dayNumber == dayNum }
            if (templateForDay.isNotEmpty()) {
                dayStops += templateForDay
            } else {
                // Cycle through curated landmarks or highlights for longer trips
                val fallbackIdx = (dayNum - 1) % guide.highlights.size.coerceAtLeast(1)
                val highlight = guide.highlights.getOrElse(fallbackIdx) { "${guide.name} City Walk" }
                val offset = (fallbackIdx - 1) * 0.014
                dayStops += listOf(
                    CuratedStopSpec(
                        dayNumber = dayNum,
                        placeName = highlight,
                        timeSlot = TimeSlot.MORNING,
                        category = ActivityCategory.SIGHTSEEING,
                        costRatioOfDaily = 0.25,
                        notes = "Morning exploration at $highlight. Beat the midday crowd.",
                        lat = guide.centerLat + offset,
                        lng = guide.centerLng + offset * 0.85,
                        transitHint = "15 min ride from hotel"
                    ),
                    CuratedStopSpec(
                        dayNumber = dayNum,
                        placeName = "${guide.name} Local Food & Bazaar",
                        timeSlot = TimeSlot.AFTERNOON,
                        category = ActivityCategory.FOOD,
                        costRatioOfDaily = 0.22,
                        notes = "Sample regional specialties and street snacks in ${guide.name}.",
                        lat = guide.centerLat - offset * 0.6,
                        lng = guide.centerLng + 0.011,
                        transitHint = "10 min local auto / walk"
                    ),
                    CuratedStopSpec(
                        dayNumber = dayNum,
                        placeName = "Sunset Viewpoint & Evening Stroll",
                        timeSlot = TimeSlot.EVENING,
                        category = ActivityCategory.SIGHTSEEING,
                        costRatioOfDaily = 0.15,
                        notes = "Relaxed golden-hour viewpoint in ${guide.name}.",
                        lat = guide.centerLat + 0.012,
                        lng = guide.centerLng - 0.010,
                        transitHint = "12 min cab"
                    )
                )
            }

            dayStops.forEachIndexed { idx, spec ->
                val estUsd = if (spec.costRatioOfDaily <= 0.0) {
                    0.0
                } else {
                    (guide.dailyAvgUsd * spec.costRatioOfDaily).coerceAtLeast(4.0)
                }
                activities += ActivityEntity(
                    tripId = tripId,
                    dayNumber = dayNum,
                    placeName = spec.placeName,
                    timeSlot = spec.timeSlot.label,
                    category = spec.category.label,
                    estimatedCostUsd = estUsd,
                    notes = spec.notes,
                    latitude = spec.lat,
                    longitude = spec.lng,
                    sortOrder = idx,
                    transitHint = spec.transitHint,
                    imageUrl = PlaceImageResolver.resolveActivityImageUrl(
                        placeName = spec.placeName,
                        category = spec.category.label,
                        destinationName = guide.name
                    )
                )
            }
        }

        return days to activities
    }

    private fun curatedDayTitle(guide: DestinationGuide, dayNum: Int, origin: String): String {
        val prefix = if (dayNum == 1 && origin.isNotBlank() && !origin.equals(guide.name, ignoreCase = true)) {
            "$origin → ${guide.name}: "
        } else {
            ""
        }
        val baseTitle = when (guide.id) {
            "jaipur" -> when (dayNum) {
                1 -> "Pink City & Hawa Mahal"
                2 -> "Amber Fort & Jal Mahal"
                3 -> "City Palace & Nahargarh Sunset"
                4 -> "Patrika Gate, Bazaars & Chokhi Dhani"
                else -> "Royal Heritage & Crafts Day $dayNum"
            }
            "manali" -> when (dayNum) {
                1 -> "Old Manali & Hidimba Temple"
                2 -> "Solang Valley & Atal Tunnel"
                3 -> "Vashisht Hot Springs & Jogini Falls"
                else -> "Himalayan Valley Discovery"
            }
            "goa" -> when (dayNum) {
                1 -> "Fontainhas & Panaji Waterfront"
                2 -> "Chapora Fort & North Goa Beaches"
                3 -> "Palolem & South Goa Coast"
                else -> "Dudhsagar & Spice Plantations"
            }
            "kerala" -> when (dayNum) {
                1 -> "Fort Kochi & Chinese Fishing Nets"
                2 -> "Alleppey Backwaters Houseboat"
                3 -> "Munnar Tea Estates & Hills"
                else -> "Varkala Cliff & Coastal Retreat"
            }
            "bali" -> when (dayNum) {
                1 -> "Ubud Culture & Rice Terraces"
                2 -> "Waterfalls & Sacred Springs"
                3 -> "Uluwatu Cliffs & Sunset"
                else -> "Nusa Penida & Beach Clubs"
            }
            "kyoto" -> when (dayNum) {
                1 -> "Fushimi Inari & Gion Lanterns"
                2 -> "Arashiyama Bamboo & River"
                3 -> "Kiyomizu-dera & Higashiyama"
                else -> "Nishiki Market & Zen Gardens"
            }
            "interlaken" -> when (dayNum) {
                1 -> "Harder Kulm & Lake Brienz"
                2 -> "Jungfraujoch Top of Europe"
                3 -> "Lauterbrunnen Waterfalls"
                else -> "Grindelwald First Alpine Walk"
            }
            "paris" -> when (dayNum) {
                1 -> "Eiffel Tower & Seine River"
                2 -> "Louvre & Tuileries Stroll"
                3 -> "Montmartre & Sacré-Cœur"
                else -> "Le Marais & Cafe Hopping"
            }
            else -> if (dayNum == 1) "Arrival & ${guide.highlights.firstOrNull() ?: guide.name}"
            else guide.highlights.getOrNull(dayNum - 1) ?: "Local Discovery Day $dayNum"
        }
        return "$prefix$baseTitle"
    }

    private fun arrivalHubCoords(guide: DestinationGuide): Pair<Double, Double> = when (guide.id) {
        "jaipur" -> 26.8242 to 75.8122 // Jaipur International Airport (JAI)
        "manali" -> 31.8763 to 77.1544 // Kullu-Manali / Bus Stand gateway
        "goa" -> 15.3808 to 73.8314 // Goa Dabolim / Transit Hub
        "kerala" -> 10.1518 to 76.3930 // Cochin International (COK)
        "bali" -> -8.7467 to 115.1668 // Ngurah Rai DPS
        "kyoto" -> 34.9858 to 135.7588 // Kyoto Station
        "interlaken" -> 46.6830 to 7.8516 // Interlaken West/Ost
        "paris" -> 48.8809 to 2.3553 // Paris Gare du Nord / Center
        else -> guide.centerLat to guide.centerLng
    }

    private fun curatedStopsForDestination(guide: DestinationGuide): List<CuratedStopSpec> = when (guide.id) {
        "jaipur" -> listOf(
            CuratedStopSpec(1, "Hawa Mahal (Palace of Winds)", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.14, "Best facade photos from Wind View Cafe across the street.", 26.9239, 75.8267, "25 min cab from Airport/Station"),
            CuratedStopSpec(1, "Bapu Bazaar & LMB Heritage Sweets", TimeSlot.EVENING, ActivityCategory.FOOD, 0.20, "Try famous Ghewar, Dal Baati Churma & Rajasthani block prints.", 26.9154, 75.8197, "6 min walk • 500 m"),
            CuratedStopSpec(2, "Amber Fort & Sheesh Mahal", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.22, "Reach at 8:30 AM to explore the Mirror Palace before tour buses.", 26.9855, 75.8513, "25 min Uber/auto north"),
            CuratedStopSpec(2, "Jal Mahal Water Palace Stop", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.0, "Scenic promenade overlooking Man Sagar Lake on the return.", 26.9535, 75.8467, "10 min ride • 4.3 km"),
            CuratedStopSpec(2, "Rawat Mishtan Bhandar Pyaaz Kachori", TimeSlot.EVENING, ActivityCategory.FOOD, 0.14, "Must-try crispy Jaipur pyaaz kachori & mawa kachori.", 26.9209, 75.7966, "18 min ride • 6.5 km"),
            CuratedStopSpec(3, "City Palace & Jantar Mantar", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.28, "Use the Composite Monument Ticket for both UNESCO & royal courtyards.", 26.9258, 75.8237, "12 min auto • 3.4 km"),
            CuratedStopSpec(3, "Johari Bazaar Gem & Lassi Walk", TimeSlot.AFTERNOON, ActivityCategory.FOOD, 0.15, "Grab earthen-cup kullad lassi at Lassiwala MI Road.", 26.9182, 75.8245, "5 min walk • 700 m"),
            CuratedStopSpec(3, "Nahargarh Fort Sunset Overlook", TimeSlot.EVENING, ActivityCategory.SIGHTSEEING, 0.18, "Panoramic sunset view over the entire illuminated Pink City.", 26.9373, 75.8155, "22 min cab up Aravalli hill"),
            CuratedStopSpec(4, "Patrika Gate Morning Photography", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.0, "Vibrant painted archway gates at Jawahar Circle Garden.", 26.8424, 75.8020, "20 min cab south"),
            CuratedStopSpec(4, "Albert Hall Museum & Birla Mandir", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.16, "Indo-Saracenic architecture & white marble evening aarti.", 26.9116, 75.8194, "18 min ride • 8.1 km"),
            CuratedStopSpec(4, "Chokhi Dhani Traditional Dinner", TimeSlot.EVENING, ActivityCategory.FOOD, 0.35, "Authentic Rajasthani thali, folk dances & camel rides.", 26.7679, 75.8364, "32 min cab south")
        )
        "manali" -> listOf(
            CuratedStopSpec(1, "Hidimba Devi Temple", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.10, "Walk through towering deodar cedar woods around the wooden shrine.", 32.2484, 77.1809, "10 min walk from Old Manali"),
            CuratedStopSpec(1, "Cafe 1947 Riverside Lunch", TimeSlot.AFTERNOON, ActivityCategory.FOOD, 0.30, "Fresh Manalsu river trout and hot wood-fired pizza.", 32.2571, 77.1778, "8 min walk • 900 m"),
            CuratedStopSpec(1, "Mall Road Evening Stroll", TimeSlot.EVENING, ActivityCategory.FOOD, 0.20, "Try hot siddu, momos & woollen shawl shopping.", 32.2432, 77.1892, "10 min auto • 2.1 km"),
            CuratedStopSpec(2, "Solang Valley Ropeway & Paragliding", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.45, "Book early morning gondola slot for clearest snow peaks.", 32.3156, 77.1573, "30 min union cab • 13 km"),
            CuratedStopSpec(2, "Atal Tunnel & Sissu Waterfall", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.30, "Drive through the 9 km Himalayan tunnel into Lahaul Valley.", 32.4785, 77.1294, "40 min scenic drive • 26 km")
        )
        "goa" -> listOf(
            CuratedStopSpec(1, "Fontainhas Latin Quarter Walk", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.10, "Pastel Portuguese villas and artisanal bakeries in Panaji.", 15.4963, 73.8305, "25 min cab from airport"),
            CuratedStopSpec(1, "Mandovi River & Panaji Goan Thali", TimeSlot.EVENING, ActivityCategory.FOOD, 0.25, "Fresh kingfish rava fry & solkadhi at Ritz Classic.", 15.4989, 73.8278, "6 min walk"),
            CuratedStopSpec(2, "Chapora Fort & Vagator Cliffs", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.10, "Sweeping views over Vagator & Morjim coast.", 15.6054, 73.7366, "35 min scooter ride north"),
            CuratedStopSpec(2, "Anjuna Beach Shack & Sunset", TimeSlot.EVENING, ActivityCategory.FOOD, 0.35, "Live sunset music and coastal seafood.", 15.5736, 73.7407, "10 min ride • 3.8 km"),
            CuratedStopSpec(3, "Palolem Crescent Beach Kayaking", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.28, "Calm emerald bay ideal for sunrise kayaking.", 15.0100, 74.0232, "South Goa scenic drive"),
            CuratedStopSpec(3, "Cabo de Rama Sunset Point", TimeSlot.EVENING, ActivityCategory.SIGHTSEEING, 0.10, "Dramatic ocean cliff fort away from crowds.", 15.0884, 73.9212, "28 min ride • 16 km")
        )
        "kerala" -> listOf(
            CuratedStopSpec(1, "Fort Kochi Chinese Fishing Nets", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.12, "Iconic sunset silhouette along Vasco da Gama Square.", 9.9684, 76.2422, "Water Metro ferry from Ernakulam"),
            CuratedStopSpec(1, "Jew Town & Kashi Art Cafe", TimeSlot.EVENING, ActivityCategory.FOOD, 0.25, "Spice markets, antique shops & Malabar curry.", 9.9574, 76.2593, "8 min auto • 2.4 km"),
            CuratedStopSpec(2, "Alleppey Backwater Shikara Cruise", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.45, "Glide past coconut lagoons and village canals.", 9.4981, 76.3388, "1.5 hr drive south"),
            CuratedStopSpec(3, "Munnar Eravikulam Tea Gardens", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.30, "Misty green tea hills and spice plantation walk.", 10.0889, 77.0595, "Scenic Western Ghats drive")
        )
        "bali" -> listOf(
            CuratedStopSpec(1, "Tegallalang Rice Terraces", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.20, "Arrive by 8 AM for soft morning light over emerald paddies.", -8.4312, 115.2793, "35 min scooter from Ubud"),
            CuratedStopSpec(1, "Clear Cafe Ubud", TimeSlot.AFTERNOON, ActivityCategory.FOOD, 0.24, "Try Balinese Nasi Campur and fresh ginger coconut.", -8.5069, 115.2602, "20 min ride south"),
            CuratedStopSpec(1, "Campuhan Ridge Sunset Walk", TimeSlot.EVENING, ActivityCategory.SIGHTSEEING, 0.0, "Free scenic hilltop footpath over the valley.", -8.5042, 115.2546, "5 min walk"),
            CuratedStopSpec(2, "Tirta Empul Water Temple", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.18, "Sacred spring temple; sarongs provided at gate.", -8.4157, 115.3156, "25 min ride northeast"),
            CuratedStopSpec(2, "Tegenungan Waterfall", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.14, "Refreshing canyon waterfall 25 min south of Ubud.", -8.5753, 115.2886, "28 min scooter • 16 km"),
            CuratedStopSpec(3, "Uluwatu Cliff Temple & Kecak Dance", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.25, "Book 6 PM cliffside fire dance seats early.", -8.8291, 115.0849, "50 min cab to Bukit"),
            CuratedStopSpec(3, "Jimbaran Bay Beachfront Dinner", TimeSlot.EVENING, ActivityCategory.FOOD, 0.45, "Grilled snapper and garlic prawns right on the sand.", -8.7745, 115.1667, "22 min ride north")
        )
        "kyoto" -> listOf(
            CuratedStopSpec(1, "Fushimi Inari Taisha", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.0, "Walk the 10,000 vermilion torii gates early morning.", 34.9671, 135.7727, "5 min JR Nara Line"),
            CuratedStopSpec(1, "Nishiki Market & Gion Lanterns", TimeSlot.EVENING, ActivityCategory.FOOD, 0.30, "Matcha sweets, yuba, and historic Hanamikoji street.", 35.0050, 135.7649, "12 min Keihan train"),
            CuratedStopSpec(2, "Arashiyama Bamboo Grove", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.15, "Cross Togetsukyo Bridge and wander the bamboo path.", 35.0168, 135.6713, "16 min JR Sagano Line"),
            CuratedStopSpec(3, "Kiyomizu-dera & Sannenzaka", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.22, "Wooden temple balcony overlooking Kyoto skyline.", 34.9949, 135.7850, "15 min Kyoto City Bus")
        )
        "interlaken" -> listOf(
            CuratedStopSpec(1, "Harder Kulm Funicular", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.28, "Two-Lakes Bridge panorama over Thun & Brienz.", 46.6980, 7.8519, "10 min walk from Ost"),
            CuratedStopSpec(2, "Jungfraujoch Top of Europe", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.65, "Check clear mountain cams before boarding Eiger Express.", 46.5475, 7.9851, "Mountain rail via Grindelwald"),
            CuratedStopSpec(3, "Lauterbrunnen Staubbach Falls", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.20, "Iconic valley of 72 waterfalls; pack light rain shell.", 46.5935, 7.9091, "20 min BOB train")
        )
        "paris" -> listOf(
            CuratedStopSpec(1, "Eiffel Tower & Trocadéro", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.28, "Golden hour view from Esplanade du Trocadéro.", 48.8584, 2.2945, "Metro Line 6 / 9"),
            CuratedStopSpec(1, "Seine River Sunset Cruise", TimeSlot.EVENING, ActivityCategory.SIGHTSEEING, 0.18, "Sparkling Eiffel Tower light show on the hour.", 48.8626, 2.3025, "8 min riverside walk"),
            CuratedStopSpec(2, "Louvre Museum & Palais-Royal", TimeSlot.MORNING, ActivityCategory.SIGHTSEEING, 0.25, "Pre-book morning timed-entry pass.", 48.8606, 2.3376, "Metro Line 1"),
            CuratedStopSpec(3, "Montmartre & Sacré-Cœur", TimeSlot.AFTERNOON, ActivityCategory.SIGHTSEEING, 0.20, "Artist square Place du Tertre & hillside crepes.", 48.8867, 2.3431, "Metro Line 2 Anvers")
        )
        else -> emptyList()
    }
}
