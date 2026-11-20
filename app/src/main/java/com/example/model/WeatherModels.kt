package com.example.model

import com.example.data.local.PackingCategory

enum class WeatherConditionType(val label: String) {
    SUNNY("Sunny"),
    PARTLY_CLOUDY("Partly Cloudy"),
    RAINY("Rainy"),
    COLD_SNOW("Cold / Alpine"),
    BREEZY("Breezy")
}

data class DayWeatherForecast(
    val dayLabel: String,
    val condition: WeatherConditionType,
    val minTempC: Int,
    val maxTempC: Int
)

data class SuggestedPackingItem(
    val title: String,
    val category: String,
    val reason: String
)

data class WeatherForecastSummary(
    val destinationName: String,
    val days: List<DayWeatherForecast>,
    val isLiveData: Boolean
) {
    val minWeekTempC: Int get() = days.minOfOrNull { it.minTempC } ?: 20
    val maxWeekTempC: Int get() = days.maxOfOrNull { it.maxTempC } ?: 28
    val primaryCondition: WeatherConditionType
        get() = days.groupBy { it.condition }.maxByOrNull { it.value.size }?.key
            ?: WeatherConditionType.SUNNY

    val headerBadgeText: String
        get() = "${minWeekTempC}°–${maxWeekTempC}°C • ${primaryCondition.label} (5d)"

    val suggestedPackingItems: List<SuggestedPackingItem>
        get() {
            val suggestions = mutableListOf<SuggestedPackingItem>()
            val hasRain = days.any { it.condition == WeatherConditionType.RAINY }
            val isCold = minWeekTempC <= 12 || days.any { it.condition == WeatherConditionType.COLD_SNOW }
            val isHot = maxWeekTempC >= 27

            if (hasRain) {
                suggestions += SuggestedPackingItem(
                    title = "Compact Waterproof Poncho / Rain Shell",
                    category = PackingCategory.CLOTHING.label,
                    reason = "Showers expected in 5-day forecast"
                )
                suggestions += SuggestedPackingItem(
                    title = "Foldable Travel Umbrella & Dry Pouch",
                    category = PackingCategory.TECH.label,
                    reason = "Protects phone & camera in rain"
                )
            }
            if (isCold) {
                suggestions += SuggestedPackingItem(
                    title = "Thermal Base Layer & Insulated Gloves",
                    category = PackingCategory.CLOTHING.label,
                    reason = "Low temperatures near ${minWeekTempC}°C"
                )
                suggestions += SuggestedPackingItem(
                    title = "Cold-Weather Lip Balm & Moisturizer",
                    category = PackingCategory.HEALTH.label,
                    reason = "Prevents windburn in cool mountain air"
                )
            }
            if (isHot) {
                suggestions += SuggestedPackingItem(
                    title = "SPF 50+ Broad-Spectrum Sunscreen",
                    category = PackingCategory.HEALTH.label,
                    reason = "Highs up to ${maxWeekTempC}°C"
                )
                suggestions += SuggestedPackingItem(
                    title = "UV400 Sunglasses & Breathable Hat",
                    category = PackingCategory.CLOTHING.label,
                    reason = "Strong daytime sun protection"
                )
                suggestions += SuggestedPackingItem(
                    title = "Electrolyte Hydration Sachets",
                    category = PackingCategory.HEALTH.label,
                    reason = "Stay hydrated in warm weather"
                )
            }
            if (!hasRain && !isCold && !isHot) {
                suggestions += SuggestedPackingItem(
                    title = "Light Packable Windbreaker",
                    category = PackingCategory.CLOTHING.label,
                    reason = "Comfortable ${minWeekTempC}°–${maxWeekTempC}°C evenings"
                )
                suggestions += SuggestedPackingItem(
                    title = "All-Day Cushioned Walking Shoes",
                    category = PackingCategory.CLOTHING.label,
                    reason = "Ideal sightseeing forecast"
                )
            }
            return suggestions
        }
}
