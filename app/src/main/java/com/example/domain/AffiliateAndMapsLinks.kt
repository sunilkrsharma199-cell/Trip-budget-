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
        val navUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val webUri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude(${Uri.encode(label)})"
            )
            openUrl(context, webUri.toString())
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
