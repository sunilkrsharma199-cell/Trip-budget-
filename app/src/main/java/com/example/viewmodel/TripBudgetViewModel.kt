package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiItineraryService
import com.example.ai.GeminiLiveAssistant
import com.example.ai.LiveTranscriptLine
import com.example.ai.LiveVoiceStatus
import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingItemEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDatabase
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.WeatherService
import com.example.data.repository.TripRepository
import com.example.domain.ExpenseSplitter
import com.example.domain.SplitSummary
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.model.DestinationCatalog
import com.example.model.DestinationGuide
import com.example.model.SuggestedPackingItem
import com.example.model.TravelVibe
import com.example.model.WeatherForecastSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "TripBudgetViewModel"

enum class AppTab(val label: String) {
    EXPLORE("Explore"),
    MY_TRIPS("My Trips"),
    BUDGET_SPLIT("Budget & Split"),
    VAULT_CHECKLIST("Vault & Checklist")
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val message: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class DestinationFilterState(
    val startingCity: String = "Delhi",
    val maxBudgetUsd: Double? = null,
    val durationDays: Int = 5,
    val selectedVibe: TravelVibe = TravelVibe.ALL,
    val hasAppliedSearch: Boolean = false
)

data class TripBudgetUiState(
    val currency: AppCurrency = AppCurrency.USD,
    val isDarkMode: Boolean = false,
    val selectedTab: AppTab = AppTab.EXPLORE,
    val trips: List<TripEntity> = emptyList(),
    val activeTrip: TripEntity? = null,
    val days: List<TripDayEntity> = emptyList(),
    val selectedDayNumber: Int = 1,
    val activities: List<ActivityEntity> = emptyList(),
    val travelers: List<TravelerEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val bookings: List<BookingEntity> = emptyList(),
    val packingItems: List<PackingItemEntity> = emptyList(),
    val splitSummary: SplitSummary = SplitSummary(emptyList(), emptyList()),
    val weatherForecast: WeatherForecastSummary? = null,
    val aiState: AiUiState = AiUiState.Idle,
    val filterState: DestinationFilterState = DestinationFilterState(),
    val filteredDestinations: List<DestinationGuide> = DestinationCatalog.destinations,
    val liveVoiceStatus: LiveVoiceStatus = LiveVoiceStatus.Idle,
    val liveTranscripts: List<LiveTranscriptLine> = emptyList(),
    val snackbarMessage: String? = null
)

private data class TripScopedData(
    val days: List<TripDayEntity> = emptyList(),
    val activities: List<ActivityEntity> = emptyList(),
    val travelers: List<TravelerEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val bookings: List<BookingEntity> = emptyList(),
    val packingItems: List<PackingItemEntity> = emptyList(),
    val weatherForecast: WeatherForecastSummary? = null
)

private data class LocalUiControlState(
    val selectedTab: AppTab = AppTab.EXPLORE,
    val selectedDayNumber: Int = 1,
    val aiState: AiUiState = AiUiState.Idle,
    val filterState: DestinationFilterState = DestinationFilterState(),
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TripBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getInstance(application)
    private val repository = TripRepository(database.tripDao())
    private val prefsRepository = UserPreferencesRepository(application)
    private val aiService = AiItineraryService()
    private val liveAssistant = GeminiLiveAssistant()

    private val localControlState = MutableStateFlow(LocalUiControlState())

    private val tripsFlow = repository.observeTrips()
    private val prefsFlow = prefsRepository.preferencesFlow

    private val activeTripFlow = combine(tripsFlow, prefsFlow) { trips, prefs ->
        trips.firstOrNull { it.id == prefs.activeTripId } ?: trips.firstOrNull()
    }

    private val weatherFlow = activeTripFlow.flatMapLatest { trip ->
        if (trip == null) {
            flowOf<WeatherForecastSummary?>(null)
        } else {
            flow<WeatherForecastSummary?> {
                emit(
                    WeatherService.fetch5DayForecast(
                        destinationName = trip.destinationName,
                        latitude = trip.centerLat,
                        longitude = trip.centerLng
                    )
                )
            }
        }
    }

    private val tripScopedDataFlow = activeTripFlow.flatMapLatest { trip ->
        if (trip == null) {
            flowOf(TripScopedData())
        } else {
            combine(
                repository.observeDays(trip.id),
                repository.observeActivities(trip.id),
                repository.observeTravelers(trip.id),
                repository.observeExpenses(trip.id),
                repository.observeBookings(trip.id),
                repository.observePackingItems(trip.id),
                weatherFlow
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                TripScopedData(
                    days = values[0] as List<TripDayEntity>,
                    activities = values[1] as List<ActivityEntity>,
                    travelers = values[2] as List<TravelerEntity>,
                    expenses = values[3] as List<ExpenseEntity>,
                    bookings = values[4] as List<BookingEntity>,
                    packingItems = values[5] as List<PackingItemEntity>,
                    weatherForecast = values[6] as WeatherForecastSummary?
                )
            }
        }
    }

    private val liveVoiceFlow = combine(
        liveAssistant.status,
        liveAssistant.transcripts
    ) { status, transcripts -> status to transcripts }

    val uiState: StateFlow<TripBudgetUiState> = combine(
        prefsFlow,
        tripsFlow,
        activeTripFlow,
        tripScopedDataFlow,
        localControlState,
        liveVoiceFlow
    ) { values ->
        val prefs = values[0] as UserPreferences
        @Suppress("UNCHECKED_CAST")
        val trips = values[1] as List<TripEntity>
        val activeTrip = values[2] as TripEntity?
        val scoped = values[3] as TripScopedData
        val control = values[4] as LocalUiControlState
        @Suppress("UNCHECKED_CAST")
        val voicePair = values[5] as Pair<LiveVoiceStatus, List<LiveTranscriptLine>>

        val validDayNumber = if (scoped.days.any { it.dayNumber == control.selectedDayNumber }) {
            control.selectedDayNumber
        } else {
            scoped.days.firstOrNull()?.dayNumber ?: 1
        }

        val filtered = filterDestinations(control.filterState)
        val split = ExpenseSplitter.calculate(scoped.travelers, scoped.expenses)

        TripBudgetUiState(
            currency = prefs.currency,
            isDarkMode = prefs.isDarkMode,
            selectedTab = control.selectedTab,
            trips = trips,
            activeTrip = activeTrip,
            days = scoped.days,
            selectedDayNumber = validDayNumber,
            activities = scoped.activities,
            travelers = scoped.travelers,
            expenses = scoped.expenses,
            bookings = scoped.bookings,
            packingItems = scoped.packingItems,
            splitSummary = split,
            weatherForecast = scoped.weatherForecast,
            aiState = control.aiState,
            filterState = control.filterState,
            filteredDestinations = filtered,
            liveVoiceStatus = voicePair.first,
            liveTranscripts = voicePair.second,
            snackbarMessage = control.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TripBudgetUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val seededId = repository.ensureDemoDataSeeded()
            if (seededId != null) {
                prefsRepository.setActiveTripId(seededId)
            }
        }
    }

    private fun filterDestinations(filter: DestinationFilterState): List<DestinationGuide> {
        return DestinationCatalog.destinations.filter { guide ->
            val vibeMatches = filter.selectedVibe == TravelVibe.ALL || guide.vibe == filter.selectedVibe
            val budgetLimit = filter.maxBudgetUsd
            val budgetMatches = if (budgetLimit == null || budgetLimit <= 0.0) {
                true
            } else {
                guide.estimatedTotalUsd(filter.durationDays, filter.startingCity) <= budgetLimit
            }
            vibeMatches && budgetMatches
        }.sortedBy {
            it.estimatedTotalUsd(filter.durationDays, filter.startingCity)
        }
    }

    fun selectTab(tab: AppTab) {
        localControlState.value = localControlState.value.copy(selectedTab = tab)
    }

    fun setCurrency(currency: AppCurrency) {
        viewModelScope.launch {
            prefsRepository.setCurrency(currency)
        }
    }

    fun toggleDarkMode() {
        val current = uiState.value.isDarkMode
        viewModelScope.launch {
            prefsRepository.setDarkMode(!current)
        }
    }

    fun selectTrip(tripId: Long) {
        viewModelScope.launch {
            prefsRepository.setActiveTripId(tripId)
            localControlState.value = localControlState.value.copy(
                selectedDayNumber = 1,
                aiState = AiUiState.Idle
            )
        }
    }

    fun selectDay(dayNumber: Int) {
        localControlState.value = localControlState.value.copy(selectedDayNumber = dayNumber)
    }

    fun updateDestinationSearch(
        startingCity: String,
        maxBudgetDisplayText: String,
        durationDays: Int,
        vibe: TravelVibe,
        applySearch: Boolean = true
    ) {
        val currency = uiState.value.currency
        val parsedDisplay = maxBudgetDisplayText.replace(",", "").trim().toDoubleOrNull()
        val maxBudgetUsd = parsedDisplay?.takeIf { it > 0.0 }?.let {
            CurrencyFormatter.toUsdAmount(it, currency)
        }
        localControlState.value = localControlState.value.copy(
            filterState = DestinationFilterState(
                startingCity = startingCity,
                maxBudgetUsd = maxBudgetUsd,
                durationDays = durationDays.coerceIn(1, 30),
                selectedVibe = vibe,
                hasAppliedSearch = applySearch
            )
        )
    }

    fun resetDestinationFilter() {
        localControlState.value = localControlState.value.copy(
            filterState = DestinationFilterState()
        )
    }

    fun planTripFromDestination(guide: DestinationGuide) {
        val filter = uiState.value.filterState
        val estimatedUsd = guide.estimatedTotalUsd(filter.durationDays, filter.startingCity)
        viewModelScope.launch {
            val newTripId = repository.createTripFromDestination(
                guide = guide,
                durationDays = filter.durationDays,
                budgetUsd = estimatedUsd
            )
            prefsRepository.setActiveTripId(newTripId)
            localControlState.value = localControlState.value.copy(
                selectedTab = AppTab.MY_TRIPS,
                selectedDayNumber = 1,
                aiState = AiUiState.Idle,
                snackbarMessage = "${guide.name} trip created! Tap AI Auto-Generate to build full stops."
            )
        }
    }

    fun createCustomTrip(
        title: String,
        destinationName: String,
        country: String,
        daysCount: Int,
        budgetDisplayAmount: Double,
        travelersCount: Int
    ) {
        val budgetUsd = CurrencyFormatter.toUsdAmount(budgetDisplayAmount, uiState.value.currency)
        viewModelScope.launch {
            val newId = repository.createBlankTrip(
                title = title,
                destinationName = destinationName,
                country = country,
                daysCount = daysCount,
                budgetUsd = budgetUsd,
                travelersCount = travelersCount
            )
            prefsRepository.setActiveTripId(newId)
            localControlState.value = localControlState.value.copy(
                selectedTab = AppTab.MY_TRIPS,
                selectedDayNumber = 1,
                snackbarMessage = "Created \"$title\""
            )
        }
    }

    fun updateActiveTripDetails(
        title: String,
        startDate: String,
        endDate: String,
        travelerCount: Int,
        budgetDisplayAmount: Double
    ) {
        val trip = uiState.value.activeTrip ?: return
        val budgetUsd = CurrencyFormatter.toUsdAmount(
            budgetDisplayAmount.coerceAtLeast(1.0),
            uiState.value.currency
        )
        viewModelScope.launch {
            repository.updateTrip(
                trip.copy(
                    title = title.ifBlank { trip.title },
                    startDate = startDate.ifBlank { trip.startDate },
                    endDate = endDate.ifBlank { trip.endDate },
                    travelerCount = travelerCount.coerceIn(1, 20),
                    totalBudgetUsd = budgetUsd
                )
            )
            showSnackbar("Trip details updated")
        }
    }

    fun updateActiveTripBudget(budgetDisplayAmount: Double) {
        val trip = uiState.value.activeTrip ?: return
        val budgetUsd = CurrencyFormatter.toUsdAmount(
            budgetDisplayAmount.coerceAtLeast(1.0),
            uiState.value.currency
        )
        viewModelScope.launch {
            repository.updateTrip(trip.copy(totalBudgetUsd = budgetUsd))
            showSnackbar("Trip budget updated")
        }
    }

    fun addDayToActiveTrip() {
        val trip = uiState.value.activeTrip ?: return
        val nextDay = (uiState.value.days.maxOfOrNull { it.dayNumber } ?: 0) + 1
        viewModelScope.launch {
            repository.addDay(trip.id, nextDay)
            localControlState.value = localControlState.value.copy(selectedDayNumber = nextDay)
        }
    }

    fun deleteDayFromActiveTrip(dayNumber: Int) {
        val trip = uiState.value.activeTrip ?: return
        if (uiState.value.days.size <= 1) return
        viewModelScope.launch {
            repository.deleteDay(trip.id, dayNumber)
            val fallback = uiState.value.days.firstOrNull { it.dayNumber != dayNumber }?.dayNumber ?: 1
            localControlState.value = localControlState.value.copy(selectedDayNumber = fallback)
        }
    }

    fun saveActivity(
        existingId: Long = 0L,
        dayNumber: Int,
        placeName: String,
        timeSlot: String,
        category: String,
        costDisplayAmount: Double,
        notes: String
    ) {
        val trip = uiState.value.activeTrip ?: return
        val costUsd = CurrencyFormatter.toUsdAmount(costDisplayAmount.coerceAtLeast(0.0), uiState.value.currency)
        val currentDayActs = uiState.value.activities.filter { it.dayNumber == dayNumber }
        val existing = currentDayActs.firstOrNull { it.id == existingId }
        val offset = (currentDayActs.size % 5 - 2) * 0.012
        viewModelScope.launch {
            repository.saveActivity(
                ActivityEntity(
                    id = existingId,
                    tripId = trip.id,
                    dayNumber = dayNumber,
                    placeName = placeName.trim(),
                    timeSlot = timeSlot,
                    category = category,
                    estimatedCostUsd = costUsd,
                    notes = notes.trim(),
                    latitude = existing?.latitude ?: (trip.centerLat + offset),
                    longitude = existing?.longitude ?: (trip.centerLng + offset * 0.85),
                    sortOrder = existing?.sortOrder ?: currentDayActs.size,
                    transitHint = existing?.transitHint ?: ""
                )
            )
            showSnackbar(if (existingId == 0L) "Added $placeName" else "Updated $placeName")
        }
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            repository.deleteActivity(activityId)
        }
    }

    fun aiAutoGenerateItinerary() {
        val trip = uiState.value.activeTrip ?: return
        val dayCount = uiState.value.days.size.coerceIn(1, 7)

        viewModelScope.launch {
            localControlState.value = localControlState.value.copy(aiState = AiUiState.Loading)
            try {
                val result = aiService.generateItinerary(
                    tripId = trip.id,
                    destinationName = trip.destinationName,
                    country = trip.country,
                    numDays = dayCount,
                    travelerCount = trip.travelerCount,
                    totalBudgetUsd = trip.totalBudgetUsd,
                    vibe = trip.vibe,
                    centerLat = trip.centerLat,
                    centerLng = trip.centerLng
                )
                repository.replaceItinerary(trip.id, result.days, result.activities)
                localControlState.value = localControlState.value.copy(
                    selectedDayNumber = result.days.firstOrNull()?.dayNumber ?: 1,
                    aiState = AiUiState.Success(
                        "Generated ${result.days.size}-day itinerary with ${result.activities.size} curated local stops!"
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI auto-generate failed", e)
                localControlState.value = localControlState.value.copy(
                    aiState = AiUiState.Error(
                        e.localizedMessage ?: "Unable to generate itinerary. Check network and try again."
                    )
                )
            }
        }
    }

    fun dismissAiBanner() {
        localControlState.value = localControlState.value.copy(aiState = AiUiState.Idle)
    }

    fun addTraveler(name: String) {
        val trip = uiState.value.activeTrip ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (uiState.value.travelers.any { it.name.equals(trimmed, ignoreCase = true) }) {
            showSnackbar("$trimmed is already in this trip")
            return
        }
        viewModelScope.launch {
            repository.addTraveler(trip.id, trimmed)
            repository.updateTrip(trip.copy(travelerCount = uiState.value.travelers.size + 1))
        }
    }

    fun removeTraveler(traveler: TravelerEntity) {
        val trip = uiState.value.activeTrip ?: return
        if (uiState.value.travelers.size <= 1) {
            showSnackbar("At least 1 traveler is required")
            return
        }
        viewModelScope.launch {
            repository.removeTraveler(traveler.id)
            repository.updateTrip(trip.copy(travelerCount = (uiState.value.travelers.size - 1).coerceAtLeast(1)))
        }
    }

    fun addExpense(
        title: String,
        amountDisplay: Double,
        category: String,
        dateLabel: String,
        paidByName: String,
        splitAmongNames: List<String>
    ) {
        val trip = uiState.value.activeTrip ?: return
        val amountUsd = CurrencyFormatter.toUsdAmount(amountDisplay.coerceAtLeast(0.0), uiState.value.currency)
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    tripId = trip.id,
                    title = title.trim(),
                    amountUsd = amountUsd,
                    category = category,
                    dateLabel = dateLabel.ifBlank { "Today" },
                    paidByName = paidByName,
                    splitAmongCsv = splitAmongNames.joinToString(",")
                )
            )
            showSnackbar("Expense logged")
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    fun saveBooking(
        existingId: Long = 0L,
        type: String,
        title: String,
        referenceCode: String,
        timingDetails: String,
        notes: String
    ) {
        val trip = uiState.value.activeTrip ?: return
        viewModelScope.launch {
            repository.saveBooking(
                BookingEntity(
                    id = existingId,
                    tripId = trip.id,
                    type = type,
                    title = title.trim(),
                    referenceCode = referenceCode.trim(),
                    timingDetails = timingDetails.trim(),
                    notes = notes.trim()
                )
            )
            showSnackbar(if (existingId == 0L) "Saved to Booking Vault" else "Booking updated")
        }
    }

    fun deleteBooking(bookingId: Long) {
        viewModelScope.launch {
            repository.deleteBooking(bookingId)
        }
    }

    fun addPackingItem(title: String, category: String) {
        val trip = uiState.value.activeTrip ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addPackingItem(
                PackingItemEntity(
                    tripId = trip.id,
                    title = title.trim(),
                    category = category,
                    isPacked = false
                )
            )
        }
    }

    fun addWeatherSuggestion(suggestion: SuggestedPackingItem) {
        val trip = uiState.value.activeTrip ?: return
        val exists = uiState.value.packingItems.any {
            it.title.equals(suggestion.title, ignoreCase = true)
        }
        if (exists) {
            showSnackbar("Already in your packing list")
            return
        }
        viewModelScope.launch {
            repository.addPackingItem(
                PackingItemEntity(
                    tripId = trip.id,
                    title = suggestion.title,
                    category = suggestion.category,
                    isPacked = false
                )
            )
            showSnackbar("Added ${suggestion.title}")
        }
    }

    fun syncAllWeatherSuggestions() {
        val trip = uiState.value.activeTrip ?: return
        val suggestions = uiState.value.weatherForecast?.suggestedPackingItems.orEmpty()
        val existingTitles = uiState.value.packingItems.map { it.title.lowercase() }.toSet()
        val missing = suggestions.filter { it.title.lowercase() !in existingTitles }
        if (missing.isEmpty()) {
            showSnackbar("All weather gear is already synced!")
            return
        }
        viewModelScope.launch {
            missing.forEach { item ->
                repository.addPackingItem(
                    PackingItemEntity(
                        tripId = trip.id,
                        title = item.title,
                        category = item.category,
                        isPacked = false
                    )
                )
            }
            showSnackbar("Synced ${missing.size} weather-smart items to checklist!")
        }
    }

    fun togglePackingItem(item: PackingItemEntity) {
        viewModelScope.launch {
            repository.togglePackingItem(item)
        }
    }

    fun deletePackingItem(itemId: Long) {
        viewModelScope.launch {
            repository.deletePackingItem(itemId)
        }
    }

    fun startVoiceAssistant() {
        val trip = uiState.value.activeTrip
        val currency = uiState.value.currency
        val weather = uiState.value.weatherForecast?.headerBadgeText ?: "pleasant weather"
        val contextSummary = if (trip != null) {
            "Trip '${trip.title}' to ${trip.destinationName}, ${trip.country} for ${trip.travelerCount} travelers. " +
                "Budget: ${CurrencyFormatter.formatUsd(trip.totalBudgetUsd, currency)}. " +
                "5-day forecast: $weather."
        } else {
            "Exploring Global & Indian destinations with currency ${currency.code}."
        }

        viewModelScope.launch {
            liveAssistant.startSession(getApplication(), contextSummary)
        }
    }

    fun stopVoiceAssistant() {
        viewModelScope.launch {
            liveAssistant.stopSession()
        }
    }

    fun sendVoiceTextMessage(text: String) {
        viewModelScope.launch {
            liveAssistant.sendTextMessage(text)
        }
    }

    fun showSnackbar(message: String) {
        localControlState.value = localControlState.value.copy(snackbarMessage = message)
    }

    fun clearSnackbar() {
        localControlState.value = localControlState.value.copy(snackbarMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            liveAssistant.stopSession()
        }
    }
}
