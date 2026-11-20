package com.example.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ActivityEntity
import com.example.domain.AffiliateAndMapsLinks
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun LeafletMapCard(
    destinationName: String,
    centerLat: Double,
    centerLng: Double,
    activities: List<ActivityEntity>,
    currency: AppCurrency,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val markersJson = remember(activities, currency) {
        JSONArray().apply {
            activities.forEachIndexed { idx, act ->
                put(
                    JSONObject().apply {
                        put("index", idx + 1)
                        put("name", act.placeName)
                        put("slot", act.timeSlot)
                        put("category", act.category)
                        put("cost", CurrencyFormatter.formatUsd(act.estimatedCostUsd, currency))
                        put("lat", act.latitude)
                        put("lng", act.longitude)
                    }
                )
            }
        }.toString()
    }

    val htmlContent = remember(centerLat, centerLng, markersJson) {
        buildLeafletHtml(centerLat, centerLng, markersJson)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Interactive Route Map",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$destinationName • ${activities.size} pinned stops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        AffiliateAndMapsLinks.openGoogleMapsRoute(
                            context = context,
                            destinationName = destinationName,
                            stops = activities
                        )
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Directions,
                        contentDescription = "Open route in Google Maps",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Google Maps",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                    }
                                }
                                false
                            }
                            loadDataWithBaseURL(
                                "https://unpkg.com/",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            "https://unpkg.com/",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun buildLeafletHtml(
    centerLat: Double,
    centerLng: Double,
    markersJson: String
): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
          <style>
            html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #E2E8F0; font-family: -apple-system, BlinkMacSystemFont, sans-serif; }
            .pin-badge {
              background: #0F766E;
              color: #FFFFFF;
              border: 2.5px solid #FFFFFF;
              border-radius: 50%;
              width: 28px;
              height: 28px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-weight: 700;
              font-size: 13px;
              box-shadow: 0 3px 8px rgba(15, 23, 42, 0.35);
            }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var stops = $markersJson;
            var map = L.map('map', { zoomControl: true }).setView([$centerLat, $centerLng], 12);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '&copy; OpenStreetMap'
            }).addTo(map);

            if (stops && stops.length > 0) {
              var latlngs = [];
              stops.forEach(function(stop) {
                var latlng = [stop.lat, stop.lng];
                latlngs.push(latlng);
                var icon = L.divIcon({
                  className: '',
                  html: '<div class="pin-badge">' + stop.index + '</div>',
                  iconSize: [28, 28],
                  iconAnchor: [14, 14]
                });
                var popupHtml = '<strong>' + stop.index + '. ' + stop.name + '</strong><br/>' +
                  '<span style="color:#0284C7;font-size:12px;">' + stop.slot + ' • ' + stop.category + '</span><br/>' +
                  '<span style="color:#F97316;font-weight:600;font-size:12px;">Est. ' + stop.cost + '</span>';
                L.marker(latlng, { icon: icon }).addTo(map).bindPopup(popupHtml);
              });
              if (latlngs.length > 1) {
                L.polyline(latlngs, { color: '#0F766E', weight: 3.5, dashArray: '6, 6', opacity: 0.85 }).addTo(map);
                map.fitBounds(latlngs, { padding: [32, 32] });
              } else {
                map.setView(latlngs[0], 13);
              }
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
