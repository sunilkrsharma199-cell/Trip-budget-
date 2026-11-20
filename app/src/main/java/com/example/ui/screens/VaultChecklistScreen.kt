package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.ElectricRickshaw
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.BookingType
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingCategory
import com.example.data.local.PackingItemEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.domain.AffiliateAndMapsLinks
import com.example.domain.TripExporter
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.model.DestinationCatalog
import com.example.model.LocalSurvivalGuide
import com.example.model.SuggestedPackingItem
import com.example.model.WeatherForecastSummary
import com.example.ui.components.ShareTripModal
import com.example.ui.components.TripSelectorStrip
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.VibrantSkyBlue
import com.example.ui.theme.WarmSunsetCoral
import com.example.ui.theme.WarningAmber

@Composable
fun VaultChecklistScreen(
    currency: AppCurrency,
    trips: List<TripEntity>,
    activeTrip: TripEntity?,
    days: List<TripDayEntity>,
    activities: List<ActivityEntity>,
    travelers: List<TravelerEntity>,
    expenses: List<ExpenseEntity>,
    bookings: List<BookingEntity>,
    packingItems: List<PackingItemEntity>,
    weatherForecast: WeatherForecastSummary?,
    onSelectTrip: (Long) -> Unit,
    onSaveBooking: (id: Long, type: String, title: String, ref: String, timing: String, notes: String) -> Unit,
    onDeleteBooking: (Long) -> Unit,
    onAddPackingItem: (title: String, category: String) -> Unit,
    onAddWeatherSuggestion: (SuggestedPackingItem) -> Unit,
    onSyncAllWeatherSuggestions: () -> Unit,
    onTogglePackingItem: (PackingItemEntity) -> Unit,
    onDeletePackingItem: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var showAddBookingDialog by remember { mutableStateOf(false) }
    var showShareModal by remember { mutableStateOf(false) }
    var selectedPackingFilter by remember { mutableStateOf<PackingCategory?>(null) }
    var newPackingTitle by remember { mutableStateOf("") }
    var newPackingCategory by remember { mutableStateOf(PackingCategory.CLOTHING) }

    val destinationGuide = remember(activeTrip?.destinationName) {
        DestinationCatalog.findGuideByName(activeTrip?.destinationName ?: "Bali")
    }

    val packedCount = remember(packingItems) { packingItems.count { it.isPacked } }
    val totalPacking = packingItems.size
    val packingPercent = if (totalPacking > 0) packedCount.toFloat() / totalPacking.toFloat() else 0f
    val animatedPackingProgress by animateFloatAsState(
        targetValue = packingPercent,
        animationSpec = tween(500),
        label = "packing_progress"
    )

    val filteredPackingItems = remember(packingItems, selectedPackingFilter) {
        val cat = selectedPackingFilter
        if (cat == null) packingItems
        else packingItems.filter { it.category.equals(cat.label, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TripSelectorStrip(
                trips = trips,
                activeTripId = activeTrip?.id,
                onSelectTrip = onSelectTrip
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Select or create a trip to access your Booking Vault and Packing List.")
                    }
                }
            }
        } else {
            // Local Travel Fares & Scam Alert Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "${activeTrip.destinationName} Local Fares & Safety",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = destinationGuide.localTips.localTransportSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Avg Local Transport Fares
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransportFarePill(
                                icon = Icons.Rounded.LocalTaxi,
                                label = "Taxi / km",
                                value = CurrencyFormatter.formatUsd(
                                    destinationGuide.localTips.taxiFarePerKmUsd,
                                    currency,
                                    showDecimals = true
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TransportFarePill(
                                icon = Icons.Rounded.DirectionsBus,
                                label = "Metro / Bus",
                                value = CurrencyFormatter.formatUsd(
                                    destinationGuide.localTips.metroOrBusFareUsd,
                                    currency,
                                    showDecimals = true
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TransportFarePill(
                                icon = Icons.Rounded.ElectricRickshaw,
                                label = "Auto / Local",
                                value = CurrencyFormatter.formatUsd(
                                    destinationGuide.localTips.autoOrLocalFareUsd,
                                    currency,
                                    showDecimals = true
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Scam Alert Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarningAmber.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = WarmSunsetCoral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tourist Tips & Scam Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                destinationGuide.localTips.scamAlerts.forEach { alert ->
                                    Text(
                                        text = "• $alert",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Local Survival Guide: 6 Essential Phrases, Visa/SIM Tips, Emergency Numbers
            item {
                LocalSurvivalGuideCard(
                    destinationName = activeTrip.destinationName,
                    guide = destinationGuide.survivalGuide,
                    onCopyPhrase = { phraseText ->
                        clipboard.setText(AnnotatedString(phraseText))
                        onShowMessage("Copied \"$phraseText\"")
                    },
                    onDialEmergency = { number ->
                        AffiliateAndMapsLinks.dialEmergencyNumber(context, number)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Booking & Document Vault Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Booking & Document Vault",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Offline PNRs, hotel confirmations & embassy contacts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddBookingDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_booking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            if (bookings.isEmpty()) {
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text("No bookings saved yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Store flight PNRs, train tickets, hotel vouchers, and emergency numbers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(bookings, key = { it.id }) { booking ->
                    BookingVaultCard(
                        booking = booking,
                        destinationName = activeTrip.destinationName,
                        onCopyRef = {
                            clipboard.setText(AnnotatedString(booking.referenceCode))
                            onShowMessage("Copied ${booking.referenceCode}")
                        },
                        onDelete = { onDeleteBooking(booking.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Smart Packing Checklist Header + Progress Bar + Add Item
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Checklist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Smart Packing Checklist",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SafeEmerald.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${(packingPercent * 100).toInt()}% Packed",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SafeEmerald,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "$packedCount of $totalPacking essentials packed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedPackingProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SafeEmerald)
                            )
                        }

                        // Weather & Smart Packing Sync
                        if (weatherForecast != null && weatherForecast.suggestedPackingItems.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = VibrantSkyBlue.copy(alpha = 0.09f),
                                border = BorderStroke(1.dp, VibrantSkyBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.WbSunny,
                                                contentDescription = null,
                                                tint = WarmSunsetCoral,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Weather-Smart Gear Sync",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = weatherForecast.headerBadgeText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VibrantSkyBlue
                                                )
                                            }
                                        }
                                        TextButton(
                                            onClick = onSyncAllWeatherSuggestions,
                                            modifier = Modifier.testTag("sync_all_weather_gear_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sync All")
                                        }
                                    }

                                    weatherForecast.suggestedPackingItems.forEach { suggestion ->
                                        val alreadyAdded = packingItems.any {
                                            it.title.equals(suggestion.title, ignoreCase = true)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = suggestion.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = suggestion.reason,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (alreadyAdded) {
                                                    SafeEmerald.copy(alpha = 0.16f)
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable(enabled = !alreadyAdded) {
                                                        onAddWeatherSuggestion(suggestion)
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (alreadyAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                                                        contentDescription = null,
                                                        tint = if (alreadyAdded) SafeEmerald else Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (alreadyAdded) "Added" else "Add",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (alreadyAdded) SafeEmerald else Color.White,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Category Filter
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedPackingFilter == null,
                                    onClick = { selectedPackingFilter = null },
                                    label = { Text("All") }
                                )
                            }
                            items(PackingCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedPackingFilter == cat,
                                    onClick = { selectedPackingFilter = cat },
                                    label = { Text(cat.label) }
                                )
                            }
                        }

                        // Quick Add Packing Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPackingTitle,
                                onValueChange = { newPackingTitle = it },
                                label = { Text("Add custom item") },
                                placeholder = { Text("e.g., Rain poncho, Power bank") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("packing_item_input")
                            )
                            Button(
                                onClick = {
                                    if (newPackingTitle.isNotBlank()) {
                                        onAddPackingItem(
                                            newPackingTitle,
                                            (selectedPackingFilter ?: newPackingCategory).label
                                        )
                                        newPackingTitle = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("add_packing_item_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add packing item")
                            }
                        }

                        // Packing Items List
                        filteredPackingItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onTogglePackingItem(item) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isPacked,
                                    onCheckedChange = { onTogglePackingItem(item) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = if (item.isPacked) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (item.isPacked) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                IconButton(
                                    onClick = { onDeletePackingItem(item.id) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete packing item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export & Backup Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Export & Offline Backup",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Share your full itinerary, group settlement instructions, and booking references or copy a JSON backup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showShareModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("vault_story_card_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Style,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Story Card", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    val summary = TripExporter.buildTextSummary(
                                        trip = activeTrip,
                                        days = days,
                                        activities = activities,
                                        travelers = travelers,
                                        expenses = expenses,
                                        bookings = bookings,
                                        packingItems = packingItems,
                                        currency = currency
                                    )
                                    TripExporter.shareText(
                                        context = context,
                                        subject = "${activeTrip.title} — TripBudget Plan",
                                        body = summary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Plan", maxLines = 1)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val json = TripExporter.buildJsonBackup(
                                    trip = activeTrip,
                                    days = days,
                                    activities = activities,
                                    expenses = expenses,
                                    bookings = bookings
                                )
                                clipboard.setText(AnnotatedString(json))
                                onShowMessage("JSON trip backup copied to clipboard!")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Offline JSON Backup")
                        }
                    }
                }
            }
        }
    }

    if (showAddBookingDialog) {
        AddBookingDialog(
            onDismiss = { showAddBookingDialog = false },
            onConfirm = { type, title, ref, timing, notes ->
                onSaveBooking(0L, type, title, ref, timing, notes)
                showAddBookingDialog = false
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

@Composable
private fun LocalSurvivalGuideCard(
    destinationName: String,
    guide: LocalSurvivalGuide,
    onCopyPhrase: (String) -> Unit,
    onDialEmergency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("local_survival_guide_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "$destinationName Local Survival Guide",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "6 Essential ${guide.languageName} phrases, Visa/SIM & SOS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6 Essential Local Phrases
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                guide.phrases.take(6).forEach { phrase ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCopyPhrase(phrase.localText) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = phrase.english,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = phrase.localText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Say: ${phrase.pronunciation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy ${phrase.localText}",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Visa & SIM Tips
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Visa & Entry Tip",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = guide.visaTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SimCard,
                            contentDescription = null,
                            tint = VibrantSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "SIM & Connectivity",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = guide.simTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Local Emergency Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyDialPill(
                    icon = Icons.Rounded.LocalPolice,
                    label = "Police",
                    number = guide.policeNumber,
                    onClick = { onDialEmergency(guide.policeNumber) },
                    modifier = Modifier.weight(1f)
                )
                EmergencyDialPill(
                    icon = Icons.Rounded.LocalHospital,
                    label = "Ambulance",
                    number = guide.ambulanceNumber,
                    onClick = { onDialEmergency(guide.ambulanceNumber) },
                    modifier = Modifier.weight(1f)
                )
                EmergencyDialPill(
                    icon = Icons.Rounded.SupportAgent,
                    label = "Tourist SOS",
                    number = guide.touristHelpline,
                    onClick = { onDialEmergency(guide.touristHelpline) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmergencyDialPill(
    icon: ImageVector,
    label: String,
    number: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = DangerRed.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = DangerRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = "Call $label",
                    tint = DangerRed,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DangerRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransportFarePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = VibrantSkyBlue,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BookingVaultCard(
    booking: BookingEntity,
    destinationName: String,
    onCopyRef: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookingType = BookingType.fromLabel(booking.type)
    val icon = when (bookingType) {
        BookingType.FLIGHT_TRAIN -> Icons.Rounded.Flight
        BookingType.HOTEL_STAY -> Icons.Rounded.Hotel
        BookingType.PASSPORT_VISA -> Icons.Rounded.Badge
        BookingType.EMERGENCY_EMBASSY -> Icons.Rounded.MedicalServices
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = booking.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete booking",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = booking.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (booking.timingDetails.isNotBlank()) {
                Text(
                    text = booking.timingDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (booking.referenceCode.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCopyRef() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reference / PNR / Contact",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = booking.referenceCode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy reference",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (booking.notes.isNotBlank()) {
                Text(
                    text = booking.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (bookingType == BookingType.HOTEL_STAY || bookingType == BookingType.FLIGHT_TRAIN) {
                val isStay = bookingType == BookingType.HOTEL_STAY
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
                                    query = "${booking.title} $destinationName"
                                )
                            } else {
                                AffiliateAndMapsLinks.openSkyscannerRates(
                                    context = context,
                                    destination = destinationName
                                )
                            }
                        }
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
        }
    }
}

@Composable
private fun AddBookingDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String, ref: String, timing: String, notes: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(BookingType.FLIGHT_TRAIN.label) }
    var title by remember { mutableStateOf("") }
    var referenceCode by remember { mutableStateOf("") }
    var timingDetails by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Booking Vault") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BookingType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type.label,
                            onClick = { selectedType = type.label },
                            label = { Text(type.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Airline / Hotel") },
                    placeholder = { Text("e.g., IndiGo 6E-204 DEL → GOI") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = referenceCode,
                    onValueChange = { referenceCode = it },
                    label = { Text("PNR / Confirmation / Phone") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = timingDetails,
                    onValueChange = { timingDetails = it },
                    label = { Text("Check-in / Departure Timing") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Terminal / Address") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(selectedType, title, referenceCode, timingDetails, notes)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
