package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityCategory
import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingItemEntity
import com.example.data.local.TimeSlot
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.domain.AffiliateAndMapsLinks
import com.example.domain.RouteCalculator
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.model.PlaceImageResolver
import com.example.model.WeatherConditionType
import com.example.model.WeatherForecastSummary
import com.example.ui.components.LeafletMapCard
import com.example.ui.components.RemoteTravelImage
import com.example.ui.components.ShareTripModal
import com.example.ui.components.TripSelectorStrip
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.VibrantSkyBlue
import com.example.ui.theme.WarmSunsetCoral
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AiUiState

@Composable
fun ItineraryScreen(
    currency: AppCurrency,
    trips: List<TripEntity>,
    activeTrip: TripEntity?,
    days: List<TripDayEntity>,
    selectedDayNumber: Int,
    activities: List<ActivityEntity>,
    travelers: List<TravelerEntity>,
    expenses: List<ExpenseEntity>,
    bookings: List<BookingEntity>,
    packingItems: List<PackingItemEntity>,
    weatherForecast: WeatherForecastSummary?,
    aiState: AiUiState,
    onSelectTrip: (Long) -> Unit,
    onCreateCustomTrip: (title: String, dest: String, country: String, days: Int, budgetDisplay: Double, travelers: Int, startingCity: String) -> Unit,
    onUpdateTripDetails: (title: String, startDate: String, endDate: String, travelers: Int, budgetDisplay: Double) -> Unit,
    onSelectDay: (Int) -> Unit,
    onAddDay: () -> Unit,
    onDeleteDay: (Int) -> Unit,
    onSaveActivity: (id: Long, day: Int, name: String, slot: String, cat: String, costDisplay: Double, notes: String) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onAiAutoGenerate: () -> Unit,
    onLoadCuratedPlan: () -> Unit,
    onDismissAiBanner: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNewTripDialog by remember { mutableStateOf(false) }
    var showEditTripDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showMapPanel by remember { mutableStateOf(true) }
    var showShareModal by remember { mutableStateOf(false) }
    var showWeatherDetails by remember { mutableStateOf(false) }

    val dayActivities = remember(activities, selectedDayNumber) {
        activities.filter { it.dayNumber == selectedDayNumber }.sortedBy { it.sortOrder }
    }
    val mapActivities = remember(dayActivities, activities) {
        dayActivities.ifEmpty { activities }
    }
    val selectedDayObj = remember(days, selectedDayNumber) {
        days.firstOrNull { it.dayNumber == selectedDayNumber }
    }
    val totalSpentUsd = remember(expenses) { expenses.sumOf { it.amountUsd } }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 840.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Trip Selector
            item {
                TripSelectorStrip(
                    trips = trips,
                    activeTripId = activeTrip?.id,
                    onSelectTrip = onSelectTrip,
                    onCreateTripClick = { showNewTripDialog = true }
                )
            }

            if (activeTrip == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Map,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No Trips Yet",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Pick a destination in Explore or create a custom trip to start planning.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showNewTripDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral)
                            ) {
                                Text("Create Custom Trip")
                            }
                        }
                    }
                }
            } else {
                // Trip Cover Header
                item {
                    val ratio = if (activeTrip.totalBudgetUsd > 0) {
                        (totalSpentUsd / activeTrip.totalBudgetUsd).toFloat()
                    } else 0f
                    val budgetPillColor = when {
                        ratio > 1f -> DangerRed
                        ratio >= 0.8f -> WarningAmber
                        else -> SafeEmerald
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(222.dp)
                        ) {
                            RemoteTravelImage(
                                imageUrl = activeTrip.imageUrl,
                                contentDescription = activeTrip.title,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top Action Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = budgetPillColor
                                ) {
                                    Text(
                                        text = "Spent ${CurrencyFormatter.formatUsd(totalSpentUsd, currency)} / ${CurrencyFormatter.formatUsd(activeTrip.totalBudgetUsd, currency)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { showEditTripDialog = true }
                                            .testTag("edit_trip_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit trip details",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { showShareModal = true }
                                            .testTag("export_trip_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Share,
                                                contentDescription = stringResource(R.string.cd_export_trip),
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom Trip Details + 5-Day Weather Forecast Badge
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (weatherForecast != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = VibrantSkyBlue.copy(alpha = 0.88f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { showWeatherDetails = !showWeatherDetails }
                                            .testTag("weather_forecast_badge")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = weatherConditionIcon(weatherForecast.primaryCondition),
                                                contentDescription = "5-day weather forecast",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = weatherForecast.headerBadgeText,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = activeTrip.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HeaderMetaChip(
                                        icon = Icons.Rounded.LocationOn,
                                        text = if (activeTrip.startingCity.isNotBlank() &&
                                            !activeTrip.startingCity.equals(activeTrip.destinationName, ignoreCase = true)
                                        ) {
                                            "${activeTrip.startingCity} → ${activeTrip.destinationName}"
                                        } else {
                                            "${activeTrip.destinationName}, ${activeTrip.country}"
                                        }
                                    )
                                    HeaderMetaChip(
                                        icon = Icons.Rounded.CalendarMonth,
                                        text = "${activeTrip.startDate} – ${activeTrip.endDate}"
                                    )
                                    HeaderMetaChip(
                                        icon = Icons.Rounded.Group,
                                        text = "${activeTrip.travelerCount}"
                                    )
                                }
                            }
                        }
                    }

                    // Expandable 5-Day Weather Forecast Card
                    AnimatedVisibility(visible = showWeatherDetails && weatherForecast != null) {
                        weatherForecast?.let { forecast ->
                            FiveDayWeatherForecastCard(
                                forecast = forecast,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // AI Auto-Generate Itinerary CTA + Status Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAiAutoGenerate,
                                enabled = aiState !is AiUiState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmSunsetCoral,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("ai_generate_itinerary_button")
                            ) {
                                if (aiState is AiUiState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Curating with Gemini...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.btn_ai_generate),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showMapPanel = !showMapPanel },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Map,
                                    contentDescription = "Toggle interactive map",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showMapPanel) "Hide Map" else "Map")
                            }
                        }

                        AnimatedVisibility(visible = aiState is AiUiState.Success || aiState is AiUiState.Error) {
                            when (aiState) {
                                is AiUiState.Success -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SafeEmerald.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = SafeEmerald
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = aiState.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = onDismissAiBanner) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Dismiss")
                                            }
                                        }
                                    }
                                }

                                is AiUiState.Error -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DangerRed.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ErrorOutline,
                                                contentDescription = null,
                                                tint = DangerRed
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = aiState.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = onLoadCuratedPlan) {
                                                Text("Curated")
                                            }
                                            TextButton(onClick = onAiAutoGenerate) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }

                // Horizontal Day Tabs + Add Day
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(days, key = { it.dayNumber }) { day ->
                            val selected = day.dayNumber == selectedDayNumber
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDay(day.dayNumber) },
                                label = {
                                    Text(
                                        text = "Day ${day.dayNumber}",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAddDay() }
                                    .testTag("add_day_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add Day",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Day",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Day Header + Add Activity
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedDayObj?.let { "Day ${it.dayNumber}: ${it.title}" }
                                    ?: "Day $selectedDayNumber Plan",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${dayActivities.size} stops • Est. ${
                                    CurrencyFormatter.formatUsd(
                                        dayActivities.sumOf { it.estimatedCostUsd },
                                        currency
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (days.size > 1) {
                                IconButton(onClick = { onDeleteDay(selectedDayNumber) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove day",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    editingActivity = null
                                    showAddActivityDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_activity_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Stop")
                            }
                        }
                    }
                }

                if (isExpanded && showMapPanel) {
                    // Side-by-side Route Timeline + Leaflet Map on tablets
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1.1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                DayStopsTimelineList(
                                    dayActivities = dayActivities,
                                    destinationName = activeTrip.destinationName,
                                    currency = currency,
                                    onEdit = {
                                        editingActivity = it
                                        showAddActivityDialog = true
                                    },
                                    onDelete = onDeleteActivity,
                                    onAddFirst = {
                                        editingActivity = null
                                        showAddActivityDialog = true
                                    }
                                )
                            }

                            LeafletMapCard(
                                destinationName = activeTrip.destinationName,
                                centerLat = activeTrip.centerLat,
                                centerLng = activeTrip.centerLng,
                                activities = mapActivities,
                                currency = currency,
                                startingCity = activeTrip.startingCity,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                } else {
                    if (dayActivities.isEmpty()) {
                        item {
                            EmptyDayCard(
                                onAddStop = {
                                    editingActivity = null
                                    showAddActivityDialog = true
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        itemsIndexed(dayActivities, key = { _, act -> act.id }) { index, activity ->
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ActivityStopCard(
                                    stopNumber = index + 1,
                                    activity = activity,
                                    destinationName = activeTrip.destinationName,
                                    currency = currency,
                                    onEdit = {
                                        editingActivity = activity
                                        showAddActivityDialog = true
                                    },
                                    onDelete = { onDeleteActivity(activity.id) }
                                )

                                if (index < dayActivities.lastIndex) {
                                    val next = dayActivities[index + 1]
                                    RouteLegConnector(
                                        label = RouteCalculator.formatLegLabel(activity, next)
                                    )
                                }
                            }
                        }
                    }

                    if (showMapPanel) {
                        item {
                            LeafletMapCard(
                                destinationName = activeTrip.destinationName,
                                centerLat = activeTrip.centerLat,
                                centerLng = activeTrip.centerLng,
                                activities = mapActivities,
                                currency = currency,
                                startingCity = activeTrip.startingCity,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewTripDialog) {
        NewTripDialog(
            currency = currency,
            onDismiss = { showNewTripDialog = false },
            onConfirm = { title, startingCity, dest, country, dayCount, budget, travelersCount ->
                onCreateCustomTrip(title, dest, country, dayCount, budget, travelersCount, startingCity)
                showNewTripDialog = false
            }
        )
    }

    if (showEditTripDialog && activeTrip != null) {
        EditTripDialog(
            trip = activeTrip,
            currency = currency,
            onDismiss = { showEditTripDialog = false },
            onConfirm = { title, start, end, travelersCount, budgetDisplay ->
                onUpdateTripDetails(title, start, end, travelersCount, budgetDisplay)
                showEditTripDialog = false
            }
        )
    }

    if (showAddActivityDialog && activeTrip != null) {
        ActivityEditorDialog(
            existing = editingActivity,
            currency = currency,
            onDismiss = { showAddActivityDialog = false },
            onConfirm = { name, slot, category, costDisplay, notes ->
                onSaveActivity(
                    editingActivity?.id ?: 0L,
                    selectedDayNumber,
                    name,
                    slot,
                    category,
                    costDisplay,
                    notes
                )
                showAddActivityDialog = false
            }
        )
    }

    if (showShareModal && activeTrip != null) {
        ShareTripModal(
            trip = activeTrip,
            days = days,
            activities = activities,
            travelers = travelers,
            expenses = expenses,
            bookings = bookings,
            packingItems = packingItems,
            currency = currency,
            onDismiss = { showShareModal = false },
            onShowMessage = onShowMessage
        )
    }
}

private fun weatherConditionIcon(condition: WeatherConditionType): ImageVector = when (condition) {
    WeatherConditionType.SUNNY -> Icons.Rounded.WbSunny
    WeatherConditionType.PARTLY_CLOUDY -> Icons.Rounded.Cloud
    WeatherConditionType.RAINY -> Icons.Rounded.Grain
    WeatherConditionType.COLD_SNOW -> Icons.Rounded.AcUnit
    WeatherConditionType.BREEZY -> Icons.Rounded.Air
}

@Composable
private fun FiveDayWeatherForecastCard(
    forecast: WeatherForecastSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${forecast.destinationName} 5-Day Forecast",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (forecast.isLiveData) "Live Open-Meteo" else "Seasonal Outlook",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                forecast.days.take(5).forEach { day ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = weatherConditionIcon(day.condition),
                                contentDescription = day.condition.label,
                                tint = if (day.condition == WeatherConditionType.SUNNY) WarmSunsetCoral else VibrantSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${day.maxTempC}°/${day.minTempC}°",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderMetaChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DayStopsTimelineList(
    dayActivities: List<ActivityEntity>,
    destinationName: String,
    currency: AppCurrency,
    onEdit: (ActivityEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onAddFirst: () -> Unit
) {
    if (dayActivities.isEmpty()) {
        EmptyDayCard(onAddStop = onAddFirst)
    } else {
        dayActivities.forEachIndexed { index, activity ->
            ActivityStopCard(
                stopNumber = index + 1,
                activity = activity,
                destinationName = destinationName,
                currency = currency,
                onEdit = { onEdit(activity) },
                onDelete = { onDelete(activity.id) }
            )
            if (index < dayActivities.lastIndex) {
                RouteLegConnector(
                    label = RouteCalculator.formatLegLabel(activity, dayActivities[index + 1])
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCard(
    onAddStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "No stops planned for this day",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap AI Auto-Generate Itinerary above or add stops manually.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onAddStop) {
                Text("+ Add First Stop")
            }
        }
    }
}

@Composable
private fun ActivityStopCard(
    stopNumber: Int,
    activity: ActivityEntity,
    destinationName: String,
    currency: AppCurrency,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val activityCategory = ActivityCategory.fromLabel(activity.category)
    val catIcon = when (activityCategory) {
        ActivityCategory.FOOD -> Icons.Rounded.Restaurant
        ActivityCategory.STAY -> Icons.Rounded.Hotel
        ActivityCategory.TRANSIT -> Icons.Rounded.Train
        ActivityCategory.SIGHTSEEING -> Icons.Rounded.Place
    }
    val stopImageUrl = remember(activity.imageUrl, activity.placeName, activity.category, destinationName) {
        activity.imageUrl.ifBlank {
            PlaceImageResolver.resolveActivityImageUrl(
                placeName = activity.placeName,
                category = activity.category,
                destinationName = destinationName
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Day-wise Stop Photo Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
            ) {
                RemoteTravelImage(
                    imageUrl = stopImageUrl,
                    contentDescription = activity.placeName,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$stopNumber",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = activity.timeSlot,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activity.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = activity.placeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activity.notes.isNotBlank()) {
                    Text(
                        text = activity.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Subtle Affiliate Booking Links for Stay & Transit stops
                if (activityCategory == ActivityCategory.STAY || activityCategory == ActivityCategory.TRANSIT) {
                    val isStay = activityCategory == ActivityCategory.STAY
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VibrantSkyBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, VibrantSkyBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (isStay) {
                                    AffiliateAndMapsLinks.openBookingComRates(
                                        context = context,
                                        query = "${activity.placeName} $destinationName"
                                    )
                                } else {
                                    AffiliateAndMapsLinks.openSkyscannerRates(
                                        context = context,
                                        destination = destinationName
                                    )
                                }
                            }
                            .testTag(if (isStay) "affiliate_booking_stay" else "affiliate_skyscanner_transit")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = VibrantSkyBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isStay) {
                                    "Check Best Rates (Booking.com)"
                                } else {
                                    "Check Best Rates (Skyscanner)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = VibrantSkyBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activity.estimatedCostUsd <= 0.0) "Free Entry"
                        else "Est. ${CurrencyFormatter.formatUsd(activity.estimatedCostUsd, currency)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmSunsetCoral,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                AffiliateAndMapsLinks.openGoogleMapsNavigation(
                                    context = context,
                                    latitude = activity.latitude,
                                    longitude = activity.longitude,
                                    label = activity.placeName
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = "Open in Google Maps",
                                tint = VibrantSkyBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Google Map",
                                style = MaterialTheme.typography.labelSmall,
                                color = VibrantSkyBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit activity",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete activity",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteLegConnector(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(26.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
        )
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun NewTripDialog(
    currency: AppCurrency,
    onDismiss: () -> Unit,
    onConfirm: (title: String, startingCity: String, dest: String, country: String, days: Int, budget: Double, travelers: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startingCity by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("4") }
    var budgetText by remember { mutableStateOf("1000") }
    var travelersText by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Start → Destination Trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startingCity,
                    onValueChange = { startingCity = it },
                    label = { Text("Starting City (Origin)") },
                    placeholder = { Text("e.g., Kolkata, Delhi, Mumbai") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination City") },
                    placeholder = { Text("e.g., Jaipur, Goa, Tokyo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Trip Title (Optional)") },
                    placeholder = { Text("Auto: Kolkata → Jaipur") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    placeholder = { Text("e.g., India or Japan") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                        label = { Text("Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = travelersText,
                        onValueChange = { travelersText = it.filter { c -> c.isDigit() } },
                        label = { Text("Travelers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Total Budget (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dest = destination.ifBlank { "Jaipur" }
                    val origin = startingCity.trim()
                    val autoTitle = if (origin.isNotBlank()) "$origin → $dest" else "$dest Trip"
                    onConfirm(
                        title.ifBlank { autoTitle },
                        origin,
                        dest,
                        country.ifBlank { "India" },
                        daysText.toIntOrNull() ?: 4,
                        budgetText.toDoubleOrNull() ?: 1000.0,
                        travelersText.toIntOrNull() ?: 2
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral)
            ) {
                Text("Create Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditTripDialog(
    trip: TripEntity,
    currency: AppCurrency,
    onDismiss: () -> Unit,
    onConfirm: (title: String, start: String, end: String, travelers: Int, budgetDisplay: Double) -> Unit
) {
    var title by remember { mutableStateOf(trip.title) }
    var startDate by remember { mutableStateOf(trip.startDate) }
    var endDate by remember { mutableStateOf(trip.endDate) }
    var travelers by remember { mutableStateOf(trip.travelerCount.toString()) }
    var budget by remember {
        mutableStateOf(
            CurrencyFormatter.toDisplayAmount(trip.totalBudgetUsd, currency).toLong().toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Trip Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Trip Title") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = travelers,
                        onValueChange = { travelers = it.filter { c -> c.isDigit() } },
                        label = { Text("Travelers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Budget (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        startDate,
                        endDate,
                        travelers.toIntOrNull() ?: trip.travelerCount,
                        budget.toDoubleOrNull() ?: CurrencyFormatter.toDisplayAmount(trip.totalBudgetUsd, currency)
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ActivityEditorDialog(
    existing: ActivityEntity?,
    currency: AppCurrency,
    onDismiss: () -> Unit,
    onConfirm: (name: String, slot: String, category: String, costDisplay: Double, notes: String) -> Unit
) {
    var placeName by remember { mutableStateOf(existing?.placeName ?: "") }
    var selectedSlot by remember { mutableStateOf(existing?.timeSlot ?: TimeSlot.MORNING.label) }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: ActivityCategory.SIGHTSEEING.label) }
    var costText by remember {
        mutableStateOf(
            existing?.let {
                CurrencyFormatter.toDisplayAmount(it.estimatedCostUsd, currency).toLong().toString()
            } ?: "15"
        )
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Itinerary Stop" else "Edit Itinerary Stop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("Place / Activity Name") },
                    placeholder = { Text("e.g., Hawa Mahal Sunrise") },
                    singleLine = true
                )
                Text("Time Slot", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TimeSlot.entries) { slot ->
                        FilterChip(
                            selected = selectedSlot == slot.label,
                            onClick = { selectedSlot = slot.label },
                            label = { Text(slot.label) }
                        )
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ActivityCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.label,
                            onClick = { selectedCategory = cat.label },
                            label = { Text(cat.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Estimated Cost (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Local Tips") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (placeName.isNotBlank()) {
                        onConfirm(
                            placeName,
                            selectedSlot,
                            selectedCategory,
                            costText.toDoubleOrNull() ?: 0.0,
                            notes
                        )
                    }
                },
                enabled = placeName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral)
            ) {
                Text(if (existing == null) "Add Stop" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
