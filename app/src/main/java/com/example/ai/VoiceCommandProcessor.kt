package com.example.ai

import com.example.model.AppCurrency
import com.example.model.DestinationCatalog
import com.example.viewmodel.AppTab

sealed interface ParsedVoiceCommand {
    data class OpenTab(val tab: AppTab) : ParsedVoiceCommand
    data class SetCurrency(val currency: AppCurrency) : ParsedVoiceCommand
    data class SetDarkMode(val darkMode: Boolean) : ParsedVoiceCommand
    data class AddExpense(val title: String, val amount: Double) : ParsedVoiceCommand
    data class AddPackingItem(val title: String) : ParsedVoiceCommand
    data class PlanDestination(val destinationId: String, val destinationName: String) : ParsedVoiceCommand
    data class PlanRoute(val startingCity: String, val destinationId: String, val destinationName: String) : ParsedVoiceCommand
    data object SyncWeatherGear : ParsedVoiceCommand
    data class AskTravelAssistant(val query: String) : ParsedVoiceCommand
}

object VoiceCommandProcessor {

    fun parse(rawInput: String): ParsedVoiceCommand {
        val text = rawInput.trim()
        val lower = text.lowercase()

        // 1. Theme commands
        when {
            lower.contains("dark mode") || lower.contains("night mode") -> {
                return ParsedVoiceCommand.SetDarkMode(true)
            }
            lower.contains("light mode") || lower.contains("day mode") -> {
                return ParsedVoiceCommand.SetDarkMode(false)
            }
        }

        // 2. Currency commands
        when {
            lower.contains("inr") || lower.contains("rupee") || lower.contains("rupees") -> {
                return ParsedVoiceCommand.SetCurrency(AppCurrency.INR)
            }
            lower.contains("usd") || lower.contains("dollar") -> {
                return ParsedVoiceCommand.SetCurrency(AppCurrency.USD)
            }
            lower.contains("eur") || lower.contains("euro") -> {
                return ParsedVoiceCommand.SetCurrency(AppCurrency.EUR)
            }
            lower.contains("gbp") || lower.contains("pound") -> {
                return ParsedVoiceCommand.SetCurrency(AppCurrency.GBP)
            }
        }

        // 3. Weather gear sync
        if ((lower.contains("sync") || lower.contains("weather")) &&
            (lower.contains("gear") || lower.contains("packing") || lower.contains("checklist"))
        ) {
            return ParsedVoiceCommand.SyncWeatherGear
        }

        // 4. Add expense command: e.g. "add expense dinner 500" or "spent 250 on taxi"
        if (lower.startsWith("add expense") || lower.startsWith("log expense") || lower.contains("spent ")) {
            val numberMatch = Regex("""(\d+(?:\.\d+)?)""").find(lower)
            val amount = numberMatch?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            if (amount != null && amount > 0.0) {
                val cleanedTitle = text
                    .replace(Regex("(?i)^(add|log)\\s+expense\\s*(for|of)?\\s*"), "")
                    .replace(Regex("(?i)^spent\\s*"), "")
                    .replace(numberMatch.value, "")
                    .replace(Regex("(?i)\\b(on|for|rupees|inr|usd|dollars|eur|gbp)\\b"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .ifBlank { "Voice Expense" }
                return ParsedVoiceCommand.AddExpense(title = cleanedTitle, amount = amount)
            }
        }

        // 5. Add packing item command: e.g. "add packing item power bank" or "pack sunscreen"
        if (lower.startsWith("add packing") || lower.startsWith("pack ")) {
            val itemName = text
                .replace(Regex("(?i)^(add\\s+packing(\\s+item)?|pack)\\s*"), "")
                .trim()
            if (itemName.isNotEmpty()) {
                return ParsedVoiceCommand.AddPackingItem(title = itemName)
            }
        }

        // 6. Start-to-Destination route command: e.g. "Kolkata to Jaipur" or "Plan trip from Kolkata to Jaipur"
        val routeMatch = Regex("""^(.+?)\s+(?:to|->|→)\s+(.+)$""", RegexOption.IGNORE_CASE).find(text)
        if (routeMatch != null) {
            val originRaw = routeMatch.groupValues[1]
                .replace(Regex("(?i)^(?:(?:plan|show|open|create|start|travel|go|a|my|the|trip|from)\\s+)+"), "")
                .trim()
            val destRaw = routeMatch.groupValues[2]
                .replace(Regex("(?i)\\s+(details|itinerary|trip|plan|guide)$"), "")
                .trim()
            val fillerOrigins = setOf("", "trip", "travel", "go", "me", "us", "destination")
            val matchedGuide = DestinationCatalog.destinations.firstOrNull { guide ->
                destRaw.lowercase().contains(guide.name.lowercase())
            }
            if (matchedGuide != null && originRaw.lowercase() !in fillerOrigins) {
                return ParsedVoiceCommand.PlanRoute(
                    startingCity = originRaw,
                    destinationId = matchedGuide.id,
                    destinationName = matchedGuide.name
                )
            }
        }

        // 7. Plan destination command: e.g. "plan trip to Goa" or "go to Manali"
        if (lower.contains("plan") || lower.contains("trip to") || lower.contains("details")) {
            val matchedGuide = DestinationCatalog.destinations.firstOrNull { guide ->
                lower.contains(guide.name.lowercase())
            }
            if (matchedGuide != null) {
                return ParsedVoiceCommand.PlanDestination(
                    destinationId = matchedGuide.id,
                    destinationName = matchedGuide.name
                )
            }
        }

        // 7. Navigation commands
        when {
            lower.contains("explore") || lower.contains("home") || lower.contains("destination") -> {
                return ParsedVoiceCommand.OpenTab(AppTab.EXPLORE)
            }
            lower.contains("itinerary") || lower.contains("my trip") || lower.contains("trips") || lower.contains("route") -> {
                return ParsedVoiceCommand.OpenTab(AppTab.MY_TRIPS)
            }
            lower.contains("budget") || lower.contains("split") || lower.contains("expense") -> {
                return ParsedVoiceCommand.OpenTab(AppTab.BUDGET_SPLIT)
            }
            lower.contains("vault") || lower.contains("checklist") || lower.contains("survival") || lower.contains("packing") -> {
                return ParsedVoiceCommand.OpenTab(AppTab.VAULT_CHECKLIST)
            }
        }

        return ParsedVoiceCommand.AskTravelAssistant(text)
    }
}
