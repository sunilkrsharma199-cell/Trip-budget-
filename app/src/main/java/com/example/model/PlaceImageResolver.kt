package com.example.model

import com.example.data.local.ActivityCategory

data class OriginDestinationRoute(
    val originCity: String,
    val destinationName: String
)

object PlaceImageResolver {

    fun parseOriginAndDestination(rawText: String, fallbackOrigin: String = ""): OriginDestinationRoute {
        val cleaned = rawText.trim()
        val toRegex = Regex("""^(.+?)\s+(?:to|->|→)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val match = toRegex.find(cleaned)
        return if (match != null) {
            val origin = match.groupValues[1]
                .replace(Regex("(?i)^(trip|plan|travel|go|from)\\s+"), "")
                .trim()
            val dest = match.groupValues[2]
                .replace(Regex("(?i)\\s+(details|itinerary|trip|plan|guide)$"), "")
                .trim()
            OriginDestinationRoute(
                originCity = origin.ifBlank { fallbackOrigin.trim() },
                destinationName = dest.ifBlank { cleaned }
            )
        } else {
            OriginDestinationRoute(
                originCity = fallbackOrigin.trim(),
                destinationName = cleaned
            )
        }
    }

    fun resolveActivityImageUrl(
        placeName: String,
        category: String,
        destinationName: String
    ): String {
        val lower = "$placeName $destinationName".lowercase()

        // Specific landmark photography (curated high-res Unsplash)
        val landmarkMatch = when {
            lower.contains("amber") || lower.contains("amer fort") ->
                "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=900&q=80"
            lower.contains("hawa mahal") ->
                "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=80"
            lower.contains("jal mahal") ->
                "https://images.unsplash.com/photo-1603262110263-fb0112e7cc33?auto=format&fit=crop&w=900&q=80"
            lower.contains("city palace") || lower.contains("jantar mantar") ->
                "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=900&q=80"
            lower.contains("nahargarh") || lower.contains("fort") ->
                "https://images.unsplash.com/photo-1590766940554-634a7ed41450?auto=format&fit=crop&w=900&q=80"
            lower.contains("patrika") || lower.contains("birla") || lower.contains("albert") ->
                "https://images.unsplash.com/photo-1667849521034-0086c275e6da?auto=format&fit=crop&w=900&q=80"
            lower.contains("chokhi") || lower.contains("johari") || lower.contains("bapu") || lower.contains("bazaar") ->
                "https://images.unsplash.com/photo-1596422846543-75c6fc197f07?auto=format&fit=crop&w=900&q=80"
            lower.contains("tegallalang") || lower.contains("ubud") || lower.contains("campuhan") ->
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=900&q=80"
            lower.contains("uluwatu") || lower.contains("jimbaran") || lower.contains("beach") || lower.contains("palolem") ->
                "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80"
            lower.contains("waterfall") || lower.contains("tirta") ->
                "https://images.unsplash.com/photo-1518548419970-58e3b4079ab2?auto=format&fit=crop&w=900&q=80"
            lower.contains("solang") || lower.contains("atal") || lower.contains("manali") || lower.contains("sissu") ->
                "https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=900&q=80"
            lower.contains("hidimba") || lower.contains("temple") ->
                "https://images.unsplash.com/photo-1609947017136-9daf32a5eb16?auto=format&fit=crop&w=900&q=80"
            lower.contains("fushimi") || lower.contains("kyoto") || lower.contains("arashiyama") || lower.contains("gion") ->
                "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=900&q=80"
            lower.contains("eiffel") || lower.contains("louvre") || lower.contains("paris") || lower.contains("seine") ->
                "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=900&q=80"
            lower.contains("jungfrau") || lower.contains("interlaken") || lower.contains("brienz") || lower.contains("lauterbrunnen") ->
                "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=900&q=80"
            lower.contains("kerala") || lower.contains("alleppey") || lower.contains("munnar") || lower.contains("kochi") ->
                "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=900&q=80"
            else -> null
        }
        if (landmarkMatch != null) return landmarkMatch

        // Category fallback photography
        return when (ActivityCategory.fromLabel(category)) {
            ActivityCategory.FOOD ->
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=900&q=80"
            ActivityCategory.STAY ->
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80"
            ActivityCategory.TRANSIT ->
                "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=900&q=80"
            ActivityCategory.SIGHTSEEING ->
                DestinationCatalog.findGuideByName(destinationName).imageUrl
        }
    }
}
