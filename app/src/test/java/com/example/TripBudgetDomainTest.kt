package com.example

import com.example.ai.ParsedVoiceCommand
import com.example.ai.VoiceCommandProcessor
import com.example.data.local.ActivityEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.TravelerEntity
import com.example.domain.ExpenseSplitter
import com.example.domain.RouteCalculator
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.model.DayWeatherForecast
import com.example.model.DestinationCatalog
import com.example.model.WeatherConditionType
import com.example.model.WeatherForecastSummary
import com.example.viewmodel.AppTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripBudgetDomainTest {

    @Test
    fun expenseSplitter_computesCorrectWhoOwesWhom() {
        val travelers = listOf(
            TravelerEntity(id = 1, tripId = 1, name = "Alex"),
            TravelerEntity(id = 2, tripId = 1, name = "Rahul"),
            TravelerEntity(id = 3, tripId = 1, name = "Priya")
        )
        val expenses = listOf(
            ExpenseEntity(id = 1, tripId = 1, title = "Flights", amountUsd = 300.0, category = "Flights & Transit", dateLabel = "Day 1", paidByName = "Alex"),
            ExpenseEntity(id = 2, tripId = 1, title = "Hotel", amountUsd = 150.0, category = "Hotels & Stays", dateLabel = "Day 1", paidByName = "Rahul")
        )

        val result = ExpenseSplitter.calculate(travelers, expenses)
        // Total = 450, fair share = 150 each.
        // Alex paid 300 (+150), Rahul paid 150 (0), Priya paid 0 (-150)
        assertEquals(1, result.settlements.size)
        val instruction = result.settlements.first()
        assertEquals("Priya", instruction.from)
        assertEquals("Alex", instruction.to)
        assertEquals(150.0, instruction.amountUsd, 0.01)
    }

    @Test
    fun currencyFormatter_convertsBetweenUsdAndInr() {
        val usd = 100.0
        val inr = CurrencyFormatter.toDisplayAmount(usd, AppCurrency.INR)
        assertEquals(8450.0, inr, 0.01)
        val backToUsd = CurrencyFormatter.toUsdAmount(inr, AppCurrency.INR)
        assertEquals(100.0, backToUsd, 0.01)

        val formattedInr = CurrencyFormatter.formatUsd(100.0, AppCurrency.INR)
        assertTrue(formattedInr.startsWith("₹"))
    }

    @Test
    fun routeCalculator_formatsWalkAndDriveLegs() {
        val a = ActivityEntity(
            tripId = 1, dayNumber = 1, placeName = "Stop A", timeSlot = "Morning",
            category = "Sightseeing", estimatedCostUsd = 0.0, notes = "",
            latitude = -8.5069, longitude = 115.2602, sortOrder = 0
        )
        val b = ActivityEntity(
            tripId = 1, dayNumber = 1, placeName = "Stop B", timeSlot = "Afternoon",
            category = "Food & Dining", estimatedCostUsd = 10.0, notes = "",
            latitude = -8.4312, longitude = 115.2793, sortOrder = 1
        )
        val label = RouteCalculator.formatLegLabel(a, b)
        assertTrue(label.contains("km"))
        assertTrue(label.contains("min"))
    }

    @Test
    fun destinationCatalog_containsGlobalAndIndianHotspots_andCompleteSurvivalGuides() {
        val destinations = DestinationCatalog.destinations
        assertEquals(8, destinations.size)
        assertTrue(destinations.any { it.name == "Bali" && !it.isIndia })
        assertTrue(destinations.any { it.name == "Kyoto" && !it.isIndia })
        assertTrue(destinations.any { it.name == "Manali" && it.isIndia })
        assertTrue(destinations.any { it.name == "Jaipur" && it.isIndia })
        assertTrue(destinations.any { it.name == "Goa" && it.isIndia })
        assertTrue(destinations.any { it.name == "Kerala" && it.isIndia })

        destinations.forEach { guide ->
            assertEquals("Expected 6 phrases for ${guide.name}", 6, guide.survivalGuide.phrases.size)
            assertTrue(guide.survivalGuide.phrases.all { it.pronunciation.isNotBlank() && it.localText.isNotBlank() })
            assertTrue(guide.survivalGuide.visaTip.isNotBlank())
            assertTrue(guide.survivalGuide.simTip.isNotBlank())
            assertTrue(guide.survivalGuide.policeNumber.isNotBlank())
        }
    }

    @Test
    fun weatherForecast_suggestsRainAndColdPackingGear() {
        val forecast = WeatherForecastSummary(
            destinationName = "Manali",
            days = listOf(
                DayWeatherForecast("Mon", WeatherConditionType.RAINY, 4, 11),
                DayWeatherForecast("Tue", WeatherConditionType.COLD_SNOW, 1, 8),
                DayWeatherForecast("Wed", WeatherConditionType.PARTLY_CLOUDY, 3, 12),
                DayWeatherForecast("Thu", WeatherConditionType.SUNNY, 5, 14),
                DayWeatherForecast("Fri", WeatherConditionType.COLD_SNOW, 0, 7)
            ),
            isLiveData = false
        )
        val titles = forecast.suggestedPackingItems.map { it.title }
        assertTrue(titles.any { it.contains("Poncho", ignoreCase = true) || it.contains("Rain", ignoreCase = true) })
        assertTrue(titles.any { it.contains("Thermal", ignoreCase = true) || it.contains("Gloves", ignoreCase = true) })
    }

    @Test
    fun voiceCommandProcessor_parsesAppActionsAndQuestions() {
        assertEquals(ParsedVoiceCommand.OpenTab(AppTab.BUDGET_SPLIT), VoiceCommandProcessor.parse("Open Budget"))
        assertEquals(ParsedVoiceCommand.SetCurrency(AppCurrency.INR), VoiceCommandProcessor.parse("Switch to INR"))
        assertEquals(ParsedVoiceCommand.SetDarkMode(true), VoiceCommandProcessor.parse("Turn on dark mode"))
        assertEquals(ParsedVoiceCommand.AddPackingItem("Umbrella"), VoiceCommandProcessor.parse("Add packing Umbrella"))

        val expense = VoiceCommandProcessor.parse("Add expense Dinner 500") as ParsedVoiceCommand.AddExpense
        assertEquals("Dinner", expense.title)
        assertEquals(500.0, expense.amount, 0.01)

        val plan = VoiceCommandProcessor.parse("Plan trip to Goa") as ParsedVoiceCommand.PlanDestination
        assertEquals("Goa", plan.destinationName)

        val route = VoiceCommandProcessor.parse("Kolkata to Jaipur") as ParsedVoiceCommand.PlanRoute
        assertEquals("Kolkata", route.startingCity)
        assertEquals("Jaipur", route.destinationName)
    }

    @Test
    fun kolkataToJaipur_curatedItineraryHasAllDaysImagesAndRealCoordinates() {
        val parsed = com.example.model.PlaceImageResolver.parseOriginAndDestination("Kolkata to Jaipur")
        assertEquals("Kolkata", parsed.originCity)
        assertEquals("Jaipur", parsed.destinationName)

        val jaipur = DestinationCatalog.findGuideByName(parsed.destinationName)
        val (days, activities) = com.example.model.CuratedItineraryFactory.buildDaysAndActivities(
            tripId = 1L,
            guide = jaipur,
            durationDays = 4,
            startingCity = parsed.originCity
        )
        assertEquals(4, days.size)
        assertTrue(days.all { day -> activities.any { it.dayNumber == day.dayNumber } })
        assertTrue(activities.first().placeName.contains("Kolkata"))
        assertTrue(activities.all { it.imageUrl.startsWith("https://") })
        assertTrue(activities.all { it.latitude in 26.0..27.5 && it.longitude in 75.0..76.5 })
    }
}
