package com.example.model

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val rateFromUsd: Double,
    val displayLabel: String,
    val isPrimaryAppCurrency: Boolean = true
) {
    USD("USD", "$", 1.0, "$ USD", true),
    INR("INR", "₹", 84.50, "₹ INR", true),
    EUR("EUR", "€", 0.92, "€ EUR", true),
    GBP("GBP", "£", 0.79, "£ GBP", true),
    JPY("JPY", "¥", 154.0, "¥ JPY", false),
    IDR("IDR", "Rp", 15850.0, "Rp IDR", false),
    CHF("CHF", "CHF", 0.88, "CHF", false);

    companion object {
        val primaryCurrencies: List<AppCurrency> = entries.filter { it.isPrimaryAppCurrency }

        fun fromCode(code: String?): AppCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}

object CurrencyFormatter {
    fun toDisplayAmount(amountUsd: Double, currency: AppCurrency): Double {
        return amountUsd * currency.rateFromUsd
    }

    fun toUsdAmount(displayAmount: Double, currency: AppCurrency): Double {
        if (currency.rateFromUsd == 0.0) return displayAmount
        return displayAmount / currency.rateFromUsd
    }

    fun convert(amount: Double, from: AppCurrency, to: AppCurrency): Double {
        val usd = toUsdAmount(amount, from)
        return toDisplayAmount(usd, to)
    }

    fun formatUsd(
        amountUsd: Double,
        currency: AppCurrency,
        showDecimals: Boolean = false
    ): String {
        val converted = toDisplayAmount(amountUsd, currency)
        return formatRaw(converted, currency, showDecimals)
    }

    fun formatRaw(
        amount: Double,
        currency: AppCurrency,
        showDecimals: Boolean = false
    ): String {
        val locale = if (currency == AppCurrency.INR) Locale("en", "IN") else Locale.US
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = if (showDecimals) 2 else 0
            maximumFractionDigits = if (showDecimals) 2 else 0
        }
        val sign = if (amount < 0) "-" else ""
        val absVal = kotlin.math.abs(amount)
        val rounded = if (showDecimals) absVal else absVal.roundToLong().toDouble()
        val formattedNumber = formatter.format(rounded)
        val prefix = if (currency == AppCurrency.CHF || currency == AppCurrency.IDR) {
            "${currency.symbol} "
        } else {
            currency.symbol
        }
        return "$sign$prefix$formattedNumber"
    }
}
