package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.LiveVoiceStatus
import com.example.ai.configureAppCheck
import com.example.ui.components.TripBudgetTopBar
import com.example.ui.components.VoiceAssistantDialog
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.ItineraryScreen
import com.example.ui.screens.VaultChecklistScreen
import com.example.ui.theme.TripBudgetTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.TripBudgetUiState
import com.example.viewmodel.TripBudgetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModel creation
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val viewModel: TripBudgetViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TripBudgetTheme(darkTheme = uiState.isDarkMode) {
                TripBudgetRoot(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TripBudgetRoot(
    uiState: TripBudgetUiState,
    viewModel: TripBudgetViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showVoiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    BackHandler(enabled = uiState.selectedTab != AppTab.EXPLORE) {
        viewModel.selectTab(AppTab.EXPLORE)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = uiState.selectedTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = tabIcon(tab, selected),
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tabShortLabel(tab),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TripBudgetTopBar(
                        currency = uiState.currency,
                        isDarkMode = uiState.isDarkMode,
                        isVoiceActive = uiState.liveVoiceStatus is LiveVoiceStatus.Active,
                        onSelectCurrency = viewModel::setCurrency,
                        onToggleDarkMode = viewModel::toggleDarkMode,
                        onOpenVoiceAssistant = { showVoiceDialog = true }
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            AppTab.entries.forEach { tab ->
                                val selected = uiState.selectedTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tabShortLabel(tab),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (uiState.selectedTab) {
                        AppTab.EXPLORE -> ExploreScreen(
                            currency = uiState.currency,
                            filterState = uiState.filterState,
                            destinations = uiState.filteredDestinations,
                            onApplySearch = viewModel::updateDestinationSearch,
                            onResetFilter = viewModel::resetDestinationFilter,
                            onPlanTrip = viewModel::planTripFromDestination
                        )

                        AppTab.MY_TRIPS -> ItineraryScreen(
                            currency = uiState.currency,
                            trips = uiState.trips,
                            activeTrip = uiState.activeTrip,
                            days = uiState.days,
                            selectedDayNumber = uiState.selectedDayNumber,
                            activities = uiState.activities,
                            travelers = uiState.travelers,
                            expenses = uiState.expenses,
                            bookings = uiState.bookings,
                            packingItems = uiState.packingItems,
                            weatherForecast = uiState.weatherForecast,
                            aiState = uiState.aiState,
                            onSelectTrip = viewModel::selectTrip,
                            onCreateCustomTrip = viewModel::createCustomTrip,
                            onUpdateTripDetails = viewModel::updateActiveTripDetails,
                            onSelectDay = viewModel::selectDay,
                            onAddDay = viewModel::addDayToActiveTrip,
                            onDeleteDay = viewModel::deleteDayFromActiveTrip,
                            onSaveActivity = viewModel::saveActivity,
                            onDeleteActivity = viewModel::deleteActivity,
                            onAiAutoGenerate = viewModel::aiAutoGenerateItinerary,
                            onDismissAiBanner = viewModel::dismissAiBanner,
                            onShowMessage = viewModel::showSnackbar
                        )

                        AppTab.BUDGET_SPLIT -> BudgetScreen(
                            currency = uiState.currency,
                            trips = uiState.trips,
                            activeTrip = uiState.activeTrip,
                            travelers = uiState.travelers,
                            expenses = uiState.expenses,
                            splitSummary = uiState.splitSummary,
                            onSelectTrip = viewModel::selectTrip,
                            onUpdateBudget = viewModel::updateActiveTripBudget,
                            onAddTraveler = viewModel::addTraveler,
                            onRemoveTraveler = viewModel::removeTraveler,
                            onAddExpense = viewModel::addExpense,
                            onDeleteExpense = viewModel::deleteExpense,
                            onShowMessage = viewModel::showSnackbar
                        )

                        AppTab.VAULT_CHECKLIST -> VaultChecklistScreen(
                            currency = uiState.currency,
                            trips = uiState.trips,
                            activeTrip = uiState.activeTrip,
                            days = uiState.days,
                            activities = uiState.activities,
                            travelers = uiState.travelers,
                            expenses = uiState.expenses,
                            bookings = uiState.bookings,
                            packingItems = uiState.packingItems,
                            weatherForecast = uiState.weatherForecast,
                            onSelectTrip = viewModel::selectTrip,
                            onSaveBooking = viewModel::saveBooking,
                            onDeleteBooking = viewModel::deleteBooking,
                            onAddPackingItem = viewModel::addPackingItem,
                            onAddWeatherSuggestion = viewModel::addWeatherSuggestion,
                            onSyncAllWeatherSuggestions = viewModel::syncAllWeatherSuggestions,
                            onTogglePackingItem = viewModel::togglePackingItem,
                            onDeletePackingItem = viewModel::deletePackingItem,
                            onShowMessage = viewModel::showSnackbar
                        )
                    }
                }
            }
        }
    }

    if (showVoiceDialog) {
        VoiceAssistantDialog(
            destinationLabel = uiState.activeTrip?.destinationName ?: "Global Travel",
            status = uiState.liveVoiceStatus,
            transcripts = uiState.liveTranscripts,
            onStartVoiceSession = viewModel::startVoiceAssistant,
            onStopVoiceSession = viewModel::stopVoiceAssistant,
            onSendText = viewModel::sendVoiceTextMessage,
            onDismiss = { showVoiceDialog = false }
        )
    }
}

private fun tabIcon(tab: AppTab, selected: Boolean) = when (tab) {
    AppTab.EXPLORE -> if (selected) Icons.Rounded.Explore else Icons.Outlined.Explore
    AppTab.MY_TRIPS -> if (selected) Icons.Rounded.Map else Icons.Outlined.Map
    AppTab.BUDGET_SPLIT -> if (selected) Icons.Rounded.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
    AppTab.VAULT_CHECKLIST -> if (selected) Icons.Rounded.VerifiedUser else Icons.Outlined.VerifiedUser
}

private fun tabShortLabel(tab: AppTab): String = when (tab) {
    AppTab.EXPLORE -> "Explore"
    AppTab.MY_TRIPS -> "My Trips"
    AppTab.BUDGET_SPLIT -> "Budget"
    AppTab.VAULT_CHECKLIST -> "Vault"
}
