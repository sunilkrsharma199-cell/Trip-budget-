package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ExpenseCategory
import com.example.data.local.ExpenseEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripEntity
import com.example.domain.SplitSummary
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import com.example.ui.components.TripSelectorStrip
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepOceanTeal
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.VibrantSkyBlue
import com.example.ui.theme.WarmSunsetCoral
import com.example.ui.theme.WarningAmber

@Composable
fun BudgetScreen(
    currency: AppCurrency,
    trips: List<TripEntity>,
    activeTrip: TripEntity?,
    travelers: List<TravelerEntity>,
    expenses: List<ExpenseEntity>,
    splitSummary: SplitSummary,
    onSelectTrip: (Long) -> Unit,
    onUpdateBudget: (Double) -> Unit,
    onAddTraveler: (String) -> Unit,
    onRemoveTraveler: (TravelerEntity) -> Unit,
    onAddExpense: (title: String, amountDisplay: Double, category: String, date: String, paidBy: String, splitAmong: List<String>) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var showAddTravelerDialog by remember { mutableStateOf(false) }

    val totalSpentUsd = remember(expenses) { expenses.sumOf { it.amountUsd } }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        LazyColumn(
            modifier = Modifier
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
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select or create a trip to track expenses",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                // Budget Health Dashboard + Currency Converter Mini-Widget
                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BudgetHealthCard(
                                trip = activeTrip,
                                totalSpentUsd = totalSpentUsd,
                                currency = currency,
                                onEditBudget = { showEditBudgetDialog = true },
                                modifier = Modifier.weight(1.2f)
                            )
                            CurrencyConverterMiniWidget(
                                defaultFrom = currency,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BudgetHealthCard(
                                trip = activeTrip,
                                totalSpentUsd = totalSpentUsd,
                                currency = currency,
                                onEditBudget = { showEditBudgetDialog = true }
                            )
                            CurrencyConverterMiniWidget(defaultFrom = currency)
                        }
                    }
                }

                // Category-Wise Visual Breakdown (5 categories)
                item {
                    CategoryBreakdownCard(
                        expenses = expenses,
                        totalSpentUsd = totalSpentUsd,
                        currency = currency,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Splitwise-Style Group Settlement
                item {
                    GroupSettlementCard(
                        travelers = travelers,
                        splitSummary = splitSummary,
                        currency = currency,
                        onAddTravelerClick = { showAddTravelerDialog = true },
                        onRemoveTraveler = onRemoveTraveler,
                        onCopySettlements = {
                            val summary = if (splitSummary.settlements.isEmpty()) {
                                "All expenses are settled up!"
                            } else {
                                splitSummary.settlements.joinToString("\n") { s ->
                                    "${s.from} owes ${s.to} ${CurrencyFormatter.formatUsd(s.amountUsd, currency, showDecimals = true)}"
                                }
                            }
                            clipboard.setText(AnnotatedString(summary))
                            onShowMessage("Settlement instructions copied!")
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Expense Logger Header
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
                                text = "Expense Log",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${expenses.size} entries recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmSunsetCoral,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_expense_button")
                        ) {
                            Text(
                                text = stringResource(R.string.btn_add_expense),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                if (expenses.isEmpty()) {
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                )
                                Text(
                                    text = "No expenses logged yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Tap + Add Expense to track flights, stays, dining, and group splits.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            currency = currency,
                            onDelete = { onDeleteExpense(expense.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditBudgetDialog && activeTrip != null) {
        EditBudgetDialog(
            currentBudgetUsd = activeTrip.totalBudgetUsd,
            currency = currency,
            onDismiss = { showEditBudgetDialog = false },
            onConfirm = { newDisplay ->
                onUpdateBudget(newDisplay)
                showEditBudgetDialog = false
            }
        )
    }

    if (showAddTravelerDialog) {
        AddTravelerDialog(
            onDismiss = { showAddTravelerDialog = false },
            onConfirm = { name ->
                onAddTraveler(name)
                showAddTravelerDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currency = currency,
            travelers = travelers,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, amt, cat, date, paidBy, splitAmong ->
                onAddExpense(title, amt, cat, date, paidBy, splitAmong)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
private fun BudgetHealthCard(
    trip: TripEntity,
    totalSpentUsd: Double,
    currency: AppCurrency,
    onEditBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetUsd = trip.totalBudgetUsd.coerceAtLeast(1.0)
    val remainingUsd = trip.totalBudgetUsd - totalSpentUsd
    val rawRatio = (totalSpentUsd / budgetUsd).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = rawRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "budget_progress"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            rawRatio > 1.0f -> DangerRed
            rawRatio >= 0.80f -> WarningAmber
            else -> DeepOceanTeal
        },
        label = "budget_status_color"
    )

    val statusLabel = when {
        rawRatio > 1.0f -> "Over Budget"
        rawRatio >= 0.80f -> "Near Limit (${(rawRatio * 100).toInt()}%)"
        else -> "Healthy (${(rawRatio * 100).toInt()}% used)"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                Column {
                    Text(
                        text = "Budget Health",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (rawRatio > 1.0f) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit budget",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { onEditBudget() }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatUsd(trip.totalBudgetUsd, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatUsd(totalSpentUsd, currency),
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmSunsetCoral,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (remainingUsd >= 0) "Remaining" else "Over By",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatUsd(kotlin.math.abs(remainingUsd), currency),
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Animated Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor)
                )
            }
        }
    }
}

@Composable
private fun CurrencyConverterMiniWidget(
    defaultFrom: AppCurrency,
    modifier: Modifier = Modifier
) {
    var amountInput by remember { mutableStateOf("100") }
    var fromCurrency by remember(defaultFrom) { mutableStateOf(defaultFrom) }
    var toCurrency by remember {
        mutableStateOf(if (defaultFrom == AppCurrency.INR) AppCurrency.USD else AppCurrency.INR)
    }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val numericAmount = amountInput.toDoubleOrNull() ?: 0.0
    val converted = CurrencyFormatter.convert(numericAmount, fromCurrency, toCurrency)
    val unitRate = CurrencyFormatter.convert(1.0, fromCurrency, toCurrency)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CurrencyExchange,
                    contentDescription = null,
                    tint = VibrantSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Live Currency Converter",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        amountInput = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.1f)
                )

                // From Currency Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { fromExpanded = true }
                            .padding(0.dp)
                    ) {
                        Text(
                            text = fromCurrency.code,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        AppCurrency.entries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.code} (${c.symbol})") },
                                onClick = {
                                    fromCurrency = c
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val tmp = fromCurrency
                        fromCurrency = toCurrency
                        toCurrency = tmp
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = "Swap currencies",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // To Currency Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.clickable { toExpanded = true }
                    ) {
                        Text(
                            text = toCurrency.code,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        AppCurrency.entries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.code} (${c.symbol})") },
                                onClick = {
                                    toCurrency = c
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyFormatter.formatRaw(converted, toCurrency, showDecimals = true),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1 ${fromCurrency.code} ≈ ${"%.2f".format(unitRate)} ${toCurrency.code}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    expenses: List<ExpenseEntity>,
    totalSpentUsd: Double,
    currency: AppCurrency,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        DeepOceanTeal,
        VibrantSkyBlue,
        WarmSunsetCoral,
        SafeEmerald,
        Color(0xFF8B5CF6)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Category-Wise Breakdown",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            ExpenseCategory.entries.forEachIndexed { idx, cat ->
                val catSumUsd = expenses
                    .filter { it.category.equals(cat.label, ignoreCase = true) }
                    .sumOf { it.amountUsd }
                val ratio = if (totalSpentUsd > 0.0) (catSumUsd / totalSpentUsd).toFloat() else 0f
                val animatedRatio by animateFloatAsState(
                    targetValue = ratio.coerceIn(0f, 1f),
                    animationSpec = tween(550),
                    label = "cat_$idx"
                )
                val barColor = palette[idx % palette.size]

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = categoryIcon(cat),
                                contentDescription = null,
                                tint = barColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${CurrencyFormatter.formatUsd(catSumUsd, currency)} (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupSettlementCard(
    travelers: List<TravelerEntity>,
    splitSummary: SplitSummary,
    currency: AppCurrency,
    onAddTravelerClick: () -> Unit,
    onRemoveTraveler: (TravelerEntity) -> Unit,
    onCopySettlements: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                        imageVector = Icons.Rounded.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Group Expense Splitter",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                TextButton(
                    onClick = onAddTravelerClick,
                    modifier = Modifier.testTag("add_traveler_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Traveler")
                }
            }

            // Travelers Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(travelers, key = { it.id }) { traveler ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(traveler.name) },
                        trailingIcon = if (travelers.size > 1) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove ${traveler.name}",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveTraveler(traveler) }
                                )
                            }
                        } else null
                    )
                }
            }

            // Net Balance Per Person
            if (splitSummary.balances.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(splitSummary.balances) { bal ->
                        val isPositive = bal.netBalanceUsd >= 0.0
                        val badgeColor = if (kotlin.math.abs(bal.netBalanceUsd) < 0.01) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else if (isPositive) {
                            SafeEmerald
                        } else {
                            WarmSunsetCoral
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = bal.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Paid ${CurrencyFormatter.formatUsd(bal.totalPaidUsd, currency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val netText = when {
                                    kotlin.math.abs(bal.netBalanceUsd) < 0.01 -> "Settled"
                                    isPositive -> "+${CurrencyFormatter.formatUsd(bal.netBalanceUsd, currency, true)}"
                                    else -> "-${CurrencyFormatter.formatUsd(-bal.netBalanceUsd, currency, true)}"
                                }
                                Text(
                                    text = netText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Who Owes Whom Instructions
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Who Owes Whom",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(
                            onClick = onCopySettlements,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy settlement instructions",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (splitSummary.settlements.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SafeEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Everyone is settled up!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        splitSummary.settlements.forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = s.from,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmSunsetCoral
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowForward,
                                        contentDescription = "owes",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = "owes ${s.to}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = CurrencyFormatter.formatUsd(
                                            s.amountUsd,
                                            currency,
                                            showDecimals = true
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    expense: ExpenseEntity,
    currency: AppCurrency,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = ExpenseCategory.fromLabel(expense.category)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(category),
                    contentDescription = category.label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} • ${expense.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Paid by ${expense.paidByName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatUsd(expense.amountUsd, currency, showDecimals = true),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete expense",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun categoryIcon(cat: ExpenseCategory) = when (cat) {
    ExpenseCategory.FLIGHTS_TRANSIT -> Icons.Rounded.Flight
    ExpenseCategory.HOTELS_STAYS -> Icons.Rounded.Hotel
    ExpenseCategory.FOOD_DINING -> Icons.Rounded.Restaurant
    ExpenseCategory.SIGHTSEEING_TICKETS -> Icons.Rounded.LocalActivity
    ExpenseCategory.SHOPPING_MISC -> Icons.Rounded.ShoppingBag
}

@Composable
private fun EditBudgetDialog(
    currentBudgetUsd: Double,
    currency: AppCurrency,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember {
        mutableStateOf(
            CurrencyFormatter.toDisplayAmount(currentBudgetUsd, currency).toLong().toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Trip Budget") },
        text = {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Total Budget (${currency.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = budgetText.toDoubleOrNull()
                    if (value != null && value > 0) onConfirm(value)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddTravelerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fellow Traveler") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Traveler Name") },
                placeholder = { Text("e.g., Sam, Ananya, Rohan") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddExpenseDialog(
    currency: AppCurrency,
    travelers: List<TravelerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amountDisplay: Double, category: String, date: String, paidBy: String, splitAmong: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD_DINING.label) }
    var dateLabel by remember { mutableStateOf("Today") }
    var paidBy by remember { mutableStateOf(travelers.firstOrNull()?.name ?: "You") }
    val splitParticipants = remember(travelers) {
        mutableStateListOf<String>().apply { addAll(travelers.map { it.name }) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Trip Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g., Sunset Ferry Tickets") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = dateLabel,
                        onValueChange = { dateLabel = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.label,
                            onClick = { selectedCategory = cat.label },
                            label = { Text(cat.label) }
                        )
                    }
                }

                Text("Paid By", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(travelers) { traveler ->
                        FilterChip(
                            selected = paidBy == traveler.name,
                            onClick = { paidBy = traveler.name },
                            label = { Text(traveler.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                if (travelers.size > 1) {
                    Text("Split Equally Among", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(travelers) { traveler ->
                            val included = splitParticipants.contains(traveler.name)
                            FilterChip(
                                selected = included,
                                onClick = {
                                    if (included && splitParticipants.size > 1) {
                                        splitParticipants.remove(traveler.name)
                                    } else if (!included) {
                                        splitParticipants.add(traveler.name)
                                    }
                                },
                                label = { Text(traveler.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amt = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (title.isNotBlank() && amt > 0.0) {
                        onConfirm(
                            title,
                            amt,
                            selectedCategory,
                            dateLabel,
                            paidBy,
                            splitParticipants.toList()
                        )
                    }
                },
                enabled = title.isNotBlank() && amt > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = WarmSunsetCoral)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
