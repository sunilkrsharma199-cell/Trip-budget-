package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.model.DestinationGuide
import com.example.model.TravelVibe
import com.example.ui.components.RemoteTravelImage
import com.example.ui.theme.DeepOceanTeal
import com.example.ui.theme.VibrantSkyBlue
import com.example.ui.theme.WarmSunsetCoral

@Composable
fun ExploreScreen(
    currency: AppCurrency,
    filterState: com.example.viewmodel.DestinationFilterState,
    destinations: List<DestinationGuide>,
    onApplySearch: (startingCity: String, destinationQuery: String, maxBudgetDisplay: String, durationDays: Int, vibe: TravelVibe) -> Unit,
    onResetFilter: () -> Unit,
    onPlanTrip: (DestinationGuide) -> Unit,
    onPlanStartToDestination: (startingCity: String, destinationInput: String, durationDays: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var startingCity by remember(filterState.startingCity) {
        mutableStateOf(filterState.startingCity)
    }
    var destinationCity by remember(filterState.destinationQuery) {
        mutableStateOf(filterState.destinationQuery)
    }
    var maxBudgetText by remember(currency, filterState.maxBudgetUsd) {
        mutableStateOf(
            filterState.maxBudgetUsd?.let {
                CurrencyFormatter.toDisplayAmount(it, currency).toLong().toString()
            } ?: ""
        )
    }
    var durationDays by remember(filterState.durationDays) {
        mutableIntStateOf(filterState.durationDays)
    }
    var selectedVibe by remember(filterState.selectedVibe) {
        mutableStateOf(filterState.selectedVibe)
    }

    val quickCities = remember { listOf("Delhi", "Kolkata", "Mumbai", "Ranchi", "Bengaluru", "London") }
    val quickDestinations = remember { listOf("Jaipur", "Goa", "Manali", "Kerala", "Bali", "Kyoto") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp
        val rows = remember(destinations, isWide) {
            if (isWide) destinations.chunked(2) else destinations.map { listOf(it) }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section + Glassmorphism Search Bar
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    RemoteTravelImage(
                        imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=80",
                        contentDescription = "Scenic tropical coast banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TravelExplore,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Smart Destination Finder",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.hero_headline),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.hero_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Glassmorphism Search Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 4.dp,
                            shadowElevation = 6.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = startingCity,
                                        onValueChange = { startingCity = it },
                                        label = { Text("Start From (Origin)") },
                                        placeholder = { Text("e.g., Kolkata, Delhi, Mumbai") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("starting_city_input")
                                    )

                                    IconButton(
                                        onClick = {
                                            val prevStart = startingCity
                                            startingCity = destinationCity
                                            destinationCity = prevStart
                                        },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapVert,
                                            contentDescription = "Swap start and destination",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Quick origin city chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(quickCities) { city ->
                                        FilterChip(
                                            selected = startingCity.equals(city, ignoreCase = true),
                                            onClick = { startingCity = city },
                                            label = { Text(city) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = destinationCity,
                                    onValueChange = { destinationCity = it },
                                    label = { Text("Destination City (Optional)") },
                                    placeholder = { Text("e.g., Jaipur, Goa, Manali, Bali") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.FlightLand,
                                            contentDescription = null,
                                            tint = WarmSunsetCoral
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarmSunsetCoral
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("destination_city_input")
                                )

                                // Quick destination chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(quickDestinations) { dest ->
                                        FilterChip(
                                            selected = destinationCity.equals(dest, ignoreCase = true),
                                            onClick = {
                                                destinationCity = if (destinationCity.equals(dest, ignoreCase = true)) "" else dest
                                            },
                                            label = { Text("To $dest") },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = maxBudgetText,
                                        onValueChange = { input ->
                                            maxBudgetText = input.filter { it.isDigit() }
                                        },
                                        label = { Text("Max Budget (${currency.symbol})") },
                                        placeholder = { Text("Any") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1.25f)
                                            .testTag("max_budget_input")
                                    )

                                    // Duration Days Stepper
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = { if (durationDays > 1) durationDays-- },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Remove,
                                                    contentDescription = "Decrease days"
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$durationDays",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (durationDays == 1) "Day" else "Days",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = { if (durationDays < 21) durationDays++ },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = "Increase days"
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Travel Vibe",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(TravelVibe.entries) { vibe ->
                                        val isSelected = selectedVibe == vibe
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedVibe = vibe
                                                onApplySearch(
                                                    startingCity,
                                                    destinationCity,
                                                    maxBudgetText,
                                                    durationDays,
                                                    vibe
                                                )
                                            },
                                            label = { Text(vibe.label) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (destinationCity.isNotBlank()) {
                                                onPlanStartToDestination(
                                                    startingCity,
                                                    destinationCity,
                                                    durationDays
                                                )
                                            } else {
                                                onApplySearch(
                                                    startingCity,
                                                    destinationCity,
                                                    maxBudgetText,
                                                    durationDays,
                                                    selectedVibe
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WarmSunsetCoral,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .testTag("find_destinations_button")
                                    ) {
                                        Icon(
                                            imageVector = if (destinationCity.isNotBlank()) Icons.Rounded.FlightTakeoff else Icons.Rounded.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (destinationCity.isNotBlank()) {
                                                val fromLabel = startingCity.trim().ifBlank { "Start" }
                                                "Plan $fromLabel → ${destinationCity.trim()}"
                                            } else {
                                                stringResource(R.string.btn_find_destinations)
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (filterState.maxBudgetUsd != null || filterState.selectedVibe != TravelVibe.ALL || destinationCity.isNotBlank()) {
                                        TextButton(
                                            onClick = {
                                                destinationCity = ""
                                                maxBudgetText = ""
                                                selectedVibe = TravelVibe.ALL
                                                onResetFilter()
                                            }
                                        ) {
                                            Text("Reset")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Curated Global & Indian Hotspots",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${destinations.size} destinations • Estimated for $durationDays days from ${startingCity.ifBlank { "your city" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (destinations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TravelExplore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No destinations match that budget",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try increasing your max budget, reducing trip days, or switching to All Vibes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = {
                                    destinationCity = ""
                                    maxBudgetText = ""
                                    selectedVibe = TravelVibe.ALL
                                    onResetFilter()
                                }
                            ) {
                                Text("Show All 8 Destinations")
                            }
                        }
                    }
                }
            } else {
                items(rows) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { guide ->
                            DestinationCard(
                                guide = guide,
                                currency = currency,
                                durationDays = durationDays,
                                startingCity = startingCity,
                                onPlanTrip = { onPlanTrip(guide) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (isWide && rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationCard(
    guide: DestinationGuide,
    currency: AppCurrency,
    durationDays: Int,
    startingCity: String,
    onPlanTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estimatedTotalUsd = remember(guide, durationDays, startingCity) {
        guide.estimatedTotalUsd(durationDays, startingCity)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Photo Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
            ) {
                RemoteTravelImage(
                    imageUrl = guide.imageUrl,
                    contentDescription = "${guide.name}, ${guide.country}",
                    modifier = Modifier.fillMaxSize()
                )

                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (guide.isIndia) DeepOceanTeal else VibrantSkyBlue
                    ) {
                        Text(
                            text = guide.country,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = guide.vibe.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Overlay Title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = guide.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best Season: ${guide.bestSeason}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // Card Body
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Est. $durationDays-Day Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatUsd(estimatedTotalUsd, currency),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Daily Avg",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${CurrencyFormatter.formatUsd(guide.dailyAvgUsd, currency)}/day",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Local Transport Tip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsTransit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = guide.transportTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onPlanTrip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmSunsetCoral,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("plan_trip_${guide.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_plan_trip),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
