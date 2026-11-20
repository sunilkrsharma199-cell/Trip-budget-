package com.example.domain

import android.content.Context
import android.content.Intent
import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingItemEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import org.json.JSONArray
import org.json.JSONObject

object TripExporter {

    fun buildTextSummary(
        trip: TripEntity,
        days: List<TripDayEntity>,
        activities: List<ActivityEntity>,
        travelers: List<TravelerEntity>,
        expenses: List<ExpenseEntity>,
        bookings: List<BookingEntity>,
        packingItems: List<PackingItemEntity>,
        currency: AppCurrency
    ): String {
        val totalSpentUsd = expenses.sumOf { it.amountUsd }
        val remainingUsd = trip.totalBudgetUsd - totalSpentUsd
        val split = ExpenseSplitter.calculate(travelers, expenses)

        return buildString {
            appendLine("✈️ ${trip.title} — ${trip.destinationName}, ${trip.country}")
            appendLine("📅 ${trip.startDate} – ${trip.endDate} | 👥 ${trip.travelerCount} Travelers")
            appendLine("💰 Budget: ${CurrencyFormatter.formatUsd(trip.totalBudgetUsd, currency)} | Spent: ${CurrencyFormatter.formatUsd(totalSpentUsd, currency)} | Remaining: ${CurrencyFormatter.formatUsd(remainingUsd, currency)}")
            appendLine()

            appendLine("🗺️ ITINERARY")
            days.sortedBy { it.dayNumber }.forEach { day ->
                appendLine("Day ${day.dayNumber}: ${day.title}")
                val dayActs = activities.filter { it.dayNumber == day.dayNumber }.sortedBy { it.sortOrder }
                if (dayActs.isEmpty()) {
                    appendLine("  • No activities planned yet")
                } else {
                    dayActs.forEach { act ->
                        appendLine("  • [${act.timeSlot}] ${act.placeName} (${act.category}) — ${CurrencyFormatter.formatUsd(act.estimatedCostUsd, currency)}")
                    }
                }
            }
            appendLine()

            if (split.settlements.isNotEmpty()) {
                appendLine("🤝 GROUP SETTLEMENTS")
                split.settlements.forEach { s ->
                    appendLine("  • ${s.from} owes ${s.to} ${CurrencyFormatter.formatUsd(s.amountUsd, currency, showDecimals = true)}")
                }
                appendLine()
            }

            if (bookings.isNotEmpty()) {
                appendLine("🧳 BOOKING VAULT")
                bookings.forEach { b ->
                    appendLine("  • ${b.type}: ${b.title} (${b.referenceCode}) — ${b.timingDetails}")
                }
                appendLine()
            }

            val packed = packingItems.count { it.isPacked }
            appendLine("🎒 PACKING PROGRESS: $packed/${packingItems.size} items packed")
            appendLine("Exported via TripBudget")
        }
    }

    fun buildJsonBackup(
        trip: TripEntity,
        days: List<TripDayEntity>,
        activities: List<ActivityEntity>,
        expenses: List<ExpenseEntity>,
        bookings: List<BookingEntity>
    ): String {
        val root = JSONObject().apply {
            put("title", trip.title)
            put("destination", trip.destinationName)
            put("country", trip.country)
            put("dates", "${trip.startDate} - ${trip.endDate}")
            put("budgetUsd", trip.totalBudgetUsd)
            put("days", JSONArray().apply {
                days.forEach { d ->
                    put(JSONObject().apply {
                        put("dayNumber", d.dayNumber)
                        put("title", d.title)
                    })
                }
            })
            put("activities", JSONArray().apply {
                activities.forEach { a ->
                    put(JSONObject().apply {
                        put("dayNumber", a.dayNumber)
                        put("placeName", a.placeName)
                        put("timeSlot", a.timeSlot)
                        put("category", a.category)
                        put("estimatedCostUsd", a.estimatedCostUsd)
                    })
                }
            })
            put("expenses", JSONArray().apply {
                expenses.forEach { e ->
                    put(JSONObject().apply {
                        put("title", e.title)
                        put("amountUsd", e.amountUsd)
                        put("category", e.category)
                        put("paidBy", e.paidByName)
                    })
                }
            })
            put("bookings", JSONArray().apply {
                bookings.forEach { b ->
                    put(JSONObject().apply {
                        put("type", b.type)
                        put("title", b.title)
                        put("reference", b.referenceCode)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun shareText(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Export TripBudget Plan"))
    }
}
