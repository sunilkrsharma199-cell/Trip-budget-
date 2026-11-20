package com.example.domain

import com.example.data.local.ActivityEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object RouteCalculator {

    fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatLegLabel(from: ActivityEntity, to: ActivityEntity): String {
        if (to.transitHint.isNotBlank()) return to.transitHint
        val km = distanceKm(from.latitude, from.longitude, to.latitude, to.longitude)
        return when {
            km < 0.15 -> "2 min walk • 120 m"
            km < 1.2 -> {
                val mins = (km * 12).roundToInt().coerceAtLeast(3)
                "$mins min walk • ${"%.1f".format(km)} km"
            }
            else -> {
                val mins = ((km / 24.0) * 60.0 + 4).roundToInt().coerceAtLeast(5)
                "$mins min drive • ${"%.1f".format(km)} km"
            }
        }
    }
}
