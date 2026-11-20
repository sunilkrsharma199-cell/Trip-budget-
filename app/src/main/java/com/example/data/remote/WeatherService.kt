package com.example.data.remote

import com.example.model.DayWeatherForecast
import com.example.model.WeatherConditionType
import com.example.model.WeatherForecastSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

object WeatherService {

    suspend fun fetch5DayForecast(
        destinationName: String,
        latitude: Double,
        longitude: Double
    ): WeatherForecastSummary = withContext(Dispatchers.IO) {
        try {
            val endpoint =
                "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude" +
                    "&daily=temperature_2m_max,temperature_2m_min,weathercode&forecast_days=5&timezone=auto"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4500
                readTimeout = 4500
                requestMethod = "GET"
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val parsed = parseOpenMeteoResponse(destinationName, body)
                if (parsed.days.isNotEmpty()) return@withContext parsed
            } else {
                conn.disconnect()
            }
        } catch (_: Exception) {
            // Fall back to deterministic destination climate estimate when offline
        }

        fallbackForecast(destinationName, latitude)
    }

    private fun parseOpenMeteoResponse(
        destinationName: String,
        jsonText: String
    ): WeatherForecastSummary {
        val root = JSONObject(jsonText)
        val daily = root.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val maxArr = daily.getJSONArray("temperature_2m_max")
        val minArr = daily.getJSONArray("temperature_2m_min")
        val codeArr = daily.getJSONArray("weathercode")

        val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outFmt = SimpleDateFormat("EEE", Locale.US)

        val days = (0 until dates.length()).map { i ->
            val rawDate = dates.getString(i)
            val label = runCatching {
                val parsed = inFmt.parse(rawDate)
                if (parsed != null) outFmt.format(parsed) else "D${i + 1}"
            }.getOrDefault("D${i + 1}")

            DayWeatherForecast(
                dayLabel = label,
                condition = mapWmoCode(codeArr.optInt(i, 0)),
                minTempC = minArr.optDouble(i, 20.0).roundToInt(),
                maxTempC = maxArr.optDouble(i, 28.0).roundToInt()
            )
        }

        return WeatherForecastSummary(
            destinationName = destinationName,
            days = days,
            isLiveData = true
        )
    }

    private fun mapWmoCode(code: Int): WeatherConditionType = when (code) {
        0, 1 -> WeatherConditionType.SUNNY
        2, 3, 45, 48 -> WeatherConditionType.PARTLY_CLOUDY
        in 51..67, in 80..82, in 95..99 -> WeatherConditionType.RAINY
        in 71..77, 85, 86 -> WeatherConditionType.COLD_SNOW
        else -> WeatherConditionType.BREEZY
    }

    private fun fallbackForecast(destinationName: String, latitude: Double): WeatherForecastSummary {
        val lower = destinationName.lowercase()
        val days = when {
            lower.contains("manali") || lower.contains("interlaken") || kotlin.math.abs(latitude) > 45 -> listOf(
                DayWeatherForecast("D1", WeatherConditionType.COLD_SNOW, 3, 11),
                DayWeatherForecast("D2", WeatherConditionType.PARTLY_CLOUDY, 4, 12),
                DayWeatherForecast("D3", WeatherConditionType.SUNNY, 2, 13),
                DayWeatherForecast("D4", WeatherConditionType.BREEZY, 3, 10),
                DayWeatherForecast("D5", WeatherConditionType.SUNNY, 4, 14)
            )
            lower.contains("bali") || lower.contains("goa") || lower.contains("kerala") -> listOf(
                DayWeatherForecast("D1", WeatherConditionType.SUNNY, 25, 31),
                DayWeatherForecast("D2", WeatherConditionType.PARTLY_CLOUDY, 25, 30),
                DayWeatherForecast("D3", WeatherConditionType.RAINY, 24, 29),
                DayWeatherForecast("D4", WeatherConditionType.SUNNY, 26, 32),
                DayWeatherForecast("D5", WeatherConditionType.BREEZY, 25, 31)
            )
            else -> listOf(
                DayWeatherForecast("D1", WeatherConditionType.SUNNY, 16, 25),
                DayWeatherForecast("D2", WeatherConditionType.PARTLY_CLOUDY, 15, 24),
                DayWeatherForecast("D3", WeatherConditionType.SUNNY, 17, 26),
                DayWeatherForecast("D4", WeatherConditionType.BREEZY, 14, 23),
                DayWeatherForecast("D5", WeatherConditionType.SUNNY, 16, 25)
            )
        }
        return WeatherForecastSummary(
            destinationName = destinationName,
            days = days,
            isLiveData = false
        )
    }
}
