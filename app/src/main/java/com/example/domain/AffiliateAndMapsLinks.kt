package com.example.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.ActivityEntity

object AffiliateAndMapsLinks {

    fun openBookingComRates(context: Context, query: String) {
        val encoded = Uri.encode(query.trim())
        val url = "https://www.booking.com/searchresults.html?ss=$encoded"
        openUrl(context, url)
    }

    fun openSkyscannerRates(context: Context, destination: String) {
        val encoded = Uri.encode(destination.trim())
        val url = "https://www.skyscanner.net/transport/flights/?q=$encoded"
        openUrl(context, url)
    }

    fun openGoogleMapsNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
        val encodedLabel = Uri.encode(label.ifBlank { "Trip Stop" })
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val genericGeo = Intent(Intent.ACTION_VIEW, geoUri)
            try {
                context.startActivity(genericGeo)
            } catch (_: Exception) {
                val webUri = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                openUrl(context, webUri)
            }
        }
    }

    fun openGoogleMapsRoute(
        context: Context,
        destinationName: String,
        stops: List<ActivityEntity>
    ) {
        if (stops.isEmpty()) {
            openUrl(
                context,
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(destinationName)}"
            )
            return
        }
        if (stops.size == 1) {
            val s = stops.first()
            openGoogleMapsNavigation(context, s.latitude, s.longitude, s.placeName)
            return
        }
        val origin = "${stops.first().latitude},${stops.first().longitude}"
        val dest = "${stops.last().latitude},${stops.last().longitude}"
        val waypoints = stops.drop(1).dropLast(1)
            .take(8)
            .joinToString("|") { "${it.latitude},${it.longitude}" }

        val url = buildString {
            append("https://www.google.com/maps/dir/?api=1")
            append("&origin=$origin")
            append("&destination=$dest")
            if (waypoints.isNotEmpty()) {
                append("&waypoints=${Uri.encode(waypoints)}")
            }
            append("&travelmode=driving")
        }
        openUrl(context, url)
    }

    fun openIntercityGoogleMapsRoute(
        context: Context,
        originCity: String,
        destinationName: String
    ) {
        val origin = originCity.trim()
        val dest = destinationName.trim()
        val url = if (origin.isNotBlank() && !origin.equals(dest, ignoreCase = true)) {
            "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(dest)}&travelmode=driving"
        } else {
            "https://www.google.com/maps/search/?api=1&query=${Uri.encode(dest)}"
        }
        openUrl(context, url)
    }

    fun dialEmergencyNumber(context: Context, phoneNumber: String) {
        val cleaned = phoneNumber.substringBefore("/").trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned"))
        runCatching { context.startActivity(intent) }
    }

    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching { context.startActivity(intent) }
    }
}
