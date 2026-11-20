package com.example.model

enum class TravelVibe(val label: String) {
    ALL("All Vibes"),
    BEACH("Beach & Island"),
    MOUNTAIN("Mountain & Adventure"),
    CULTURAL("Cultural & Heritage"),
    CITY("City Break"),
    SPIRITUAL("Spiritual")
}

data class LocalPhrase(
    val english: String,
    val localText: String,
    val pronunciation: String
)

data class LocalSurvivalGuide(
    val languageName: String,
    val phrases: List<LocalPhrase>,
    val visaTip: String,
    val simTip: String,
    val policeNumber: String,
    val ambulanceNumber: String,
    val touristHelpline: String
)

data class LocalTipsInfo(
    val taxiFarePerKmUsd: Double,
    val metroOrBusFareUsd: Double,
    val autoOrLocalFareUsd: Double,
    val localTransportSummary: String,
    val scamAlerts: List<String>
)

data class DestinationGuide(
    val id: String,
    val name: String,
    val country: String,
    val isIndia: Boolean,
    val vibe: TravelVibe,
    val imageUrl: String,
    val baseTransitUsd: Double,
    val dailyAvgUsd: Double,
    val bestSeason: String,
    val transportTip: String,
    val centerLat: Double,
    val centerLng: Double,
    val highlights: List<String>,
    val localTips: LocalTipsInfo,
    val survivalGuide: LocalSurvivalGuide
) {
    fun estimatedTotalUsd(days: Int, startingCity: String): Double {
        val cityLower = startingCity.trim().lowercase()
        val originFactor = when {
            cityLower.isBlank() -> 1.0
            isIndia && (cityLower.contains("delhi") || cityLower.contains("mumbai") ||
                cityLower.contains("ranchi") || cityLower.contains("bengaluru") ||
                cityLower.contains("kolkata") || cityLower.contains("hyderabad")) -> 0.55
            !isIndia && (cityLower.contains("new york") || cityLower.contains("london") ||
                cityLower.contains("san francisco") || cityLower.contains("singapore")) -> 0.90
            else -> 1.05
        }
        return (baseTransitUsd * originFactor) + (dailyAvgUsd * days.coerceAtLeast(1))
    }
}

object DestinationCatalog {
    val destinations: List<DestinationGuide> = listOf(
        DestinationGuide(
            id = "bali",
            name = "Bali",
            country = "Indonesia",
            isIndia = false,
            vibe = TravelVibe.BEACH,
            imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 380.0,
            dailyAvgUsd = 85.0,
            bestSeason = "Apr – Oct",
            transportTip = "Use Grab or Gojek bikes/cars instead of unmetered street taxis",
            centerLat = -8.4095,
            centerLng = 115.1889,
            highlights = listOf("Uluwatu Cliff Temple", "Tegallalang Rice Terraces", "Seminyak Sunset", "Nusa Penida"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 0.60,
                metroOrBusFareUsd = 0.30,
                autoOrLocalFareUsd = 1.80,
                localTransportSummary = "Grab/Gojek scooters start around $1.50; Bluebird metered taxis are reliable.",
                scamAlerts = listOf(
                    "Only exchange money at authorized Kuta/Ubud banks or PT Dirgahayu — avoid street booths offering unusually high rates.",
                    "Confirm Bluebird taxi meter is reset to IDR 7,000 before departing.",
                    "At temples like Uluwatu, secure sunglasses and phones from playful macaques."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Indonesian (Bahasa)",
                phrases = listOf(
                    LocalPhrase("Thank you", "Terima kasih", "teh-REE-mah KAH-seeh"),
                    LocalPhrase("Excuse me", "Permisi", "per-MEE-see"),
                    LocalPhrase("How much is this?", "Berapa harganya?", "beh-RAH-pah har-GAH-nyah"),
                    LocalPhrase("Please help me", "Tolong bantu saya", "TOH-long BAHN-too SAH-yah"),
                    LocalPhrase("Where is the restroom?", "Di mana toilet?", "dee MAH-nah TOY-let"),
                    LocalPhrase("Very delicious!", "Enak sekali!", "EH-nahk seh-KAH-lee")
                ),
                visaTip = "e-VOA (~IDR 500,000) available online before flying + complete All-Indonesia arrival QR & Bali Tourism Levy.",
                simTip = "Telkomsel offers the widest coverage across Ubud & Nusa Penida; register IMEI at DPS airport or grab an eSIM.",
                policeNumber = "110",
                ambulanceNumber = "118",
                touristHelpline = "112"
            )
        ),
        DestinationGuide(
            id = "kyoto",
            name = "Kyoto",
            country = "Japan",
            isIndia = false,
            vibe = TravelVibe.CULTURAL,
            imageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 680.0,
            dailyAvgUsd = 135.0,
            bestSeason = "Mar – May, Oct – Nov",
            transportTip = "Tap ICOCA/Suica card on Kyoto City buses & Karasuma subway",
            centerLat = 35.0116,
            centerLng = 135.7681,
            highlights = listOf("Fushimi Inari Taisha", "Arashiyama Bamboo Grove", "Kiyomizu-dera", "Gion District"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 2.60,
                metroOrBusFareUsd = 1.60,
                autoOrLocalFareUsd = 5.50,
                localTransportSummary = "Subway & city buses are ¥230 (~$1.60) flat; avoid taxis during cherry blossom rush.",
                scamAlerts = listOf(
                    "Respect private alleys in Gion — photography is prohibited on side streets with warning signs.",
                    "Always verify menu prices at touristy Izakayas for 'otoshi' table cover charges.",
                    "Buy Shinkansen tickets only via official SmartEX app or JR station counters."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Japanese",
                phrases = listOf(
                    LocalPhrase("Excuse me / Sorry", "すみません", "soo-mee-MAH-sen"),
                    LocalPhrase("Thank you very much", "ありがとうございます", "ah-ree-GAH-toh goh-zai-mas"),
                    LocalPhrase("How much?", "いくらですか？", "ee-KOO-rah des-kah"),
                    LocalPhrase("Where is the station?", "駅はどこですか？", "eh-kee wah DOH-koh des-kah"),
                    LocalPhrase("Delicious!", "おいしいです", "oy-SHEE des"),
                    LocalPhrase("Please help", "助けてください", "tah-soo-KEH-teh koo-dah-sai")
                ),
                visaTip = "Complete Visit Japan Web customs/immigration QR code pre-arrival; check passport e-Visa eligibility early.",
                simTip = "Pick up an IIJmio / Ubigi data eSIM or Kansai Airport Pocket Wi-Fi; add ICOCA card to mobile wallet.",
                policeNumber = "110",
                ambulanceNumber = "119",
                touristHelpline = "050-3816-2787"
            )
        ),
        DestinationGuide(
            id = "interlaken",
            name = "Interlaken",
            country = "Switzerland",
            isIndia = false,
            vibe = TravelVibe.MOUNTAIN,
            imageUrl = "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 760.0,
            dailyAvgUsd = 210.0,
            bestSeason = "Jun – Sep, Dec – Feb",
            transportTip = "Swiss Half Fare Card cuts mountain cable car & SBB train costs by 50%",
            centerLat = 46.6863,
            centerLng = 7.8632,
            highlights = listOf("Jungfraujoch Top of Europe", "Harder Kulm", "Lake Brienz Cruise", "Lauterbrunnen"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 4.20,
                metroOrBusFareUsd = 4.50,
                autoOrLocalFareUsd = 12.00,
                localTransportSummary = "SBB trains & PostBus run like clockwork; Swiss Half Fare Card saves big on cableways.",
                scamAlerts = listOf(
                    "Always validate regional train tickets before boarding; conductors issue steep on-the-spot fines.",
                    "Tap water from alpine village fountains is safe and free — skip overpriced bottled water.",
                    "Book Jungfraujoch weather-check morning slots before buying non-refundable peak passes."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Swiss German",
                phrases = listOf(
                    LocalPhrase("Hello", "Grüezi", "GROO-et-see"),
                    LocalPhrase("Thank you very much", "Merci vielmal", "mer-SEE feel-mahl"),
                    LocalPhrase("How much does it cost?", "Wie viel kostet das?", "vee feel KOS-tet dahs"),
                    LocalPhrase("Excuse me", "Entschuldigung", "ent-SHOOL-dee-goong"),
                    LocalPhrase("Where is the train station?", "Wo ist der Bahnhof?", "voh ist dair BAHN-hof"),
                    LocalPhrase("Please help", "Hilfe bitte", "HIL-feh BIT-teh")
                ),
                visaTip = "Schengen visa required for many non-EU travelers; carry printed SBB pass & hotel confirmations at border.",
                simTip = "Swisscom or Sunrise prepaid/eSIM offers strong 5G even up in Grindelwald & Lauterbrunnen valleys.",
                policeNumber = "117",
                ambulanceNumber = "144",
                touristHelpline = "112"
            )
        ),
        DestinationGuide(
            id = "paris",
            name = "Paris",
            country = "France",
            isIndia = false,
            vibe = TravelVibe.CITY,
            imageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 640.0,
            dailyAvgUsd = 175.0,
            bestSeason = "Apr – Jun, Sep – Oct",
            transportTip = "Load Navigo Easy pass for €2.15 Metro rides across central zones",
            centerLat = 48.8566,
            centerLng = 2.3522,
            highlights = listOf("Eiffel Tower & Trocadéro", "Louvre Museum", "Montmartre Sacré-Cœur", "Seine Cruise"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 1.90,
                metroOrBusFareUsd = 2.35,
                autoOrLocalFareUsd = 9.00,
                localTransportSummary = "Paris Metro (€2.15) is fastest; fixed official taxi fare from CDG airport is €56–€65.",
                scamAlerts = listOf(
                    "Decline 'friendship bracelet' or petition clipboards near Sacré-Cœur and Eiffel Tower.",
                    "Ignore street cup-and-ball games near Trocadéro — always rigged.",
                    "Keep phones zipped in front pockets on crowded RER B and Metro Line 1."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "French",
                phrases = listOf(
                    LocalPhrase("Hello / Good day", "Bonjour", "bohn-ZHOOR"),
                    LocalPhrase("Thank you very much", "Merci beaucoup", "mer-SEE boh-KOO"),
                    LocalPhrase("How much is this?", "Combien ça coûte ?", "kohm-BYEN sah koot"),
                    LocalPhrase("Excuse me", "Excusez-moi", "ex-kyoo-zay-MWAH"),
                    LocalPhrase("Where is the Metro?", "Où est le métro ?", "oo eh luh may-TROH"),
                    LocalPhrase("The bill, please", "L'addition, s'il vous plaît", "lah-dee-SYOHN seel voo pleh")
                ),
                visaTip = "Schengen Area entry rules apply; keep passport copy & Louvre/Eiffel timed-entry QR codes offline.",
                simTip = "Orange Holiday Europe or Bouygues eSIM works seamlessly across France & EU roaming.",
                policeNumber = "17",
                ambulanceNumber = "15",
                touristHelpline = "112"
            )
        ),
        DestinationGuide(
            id = "manali",
            name = "Manali",
            country = "India",
            isIndia = true,
            vibe = TravelVibe.MOUNTAIN,
            imageUrl = "https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 65.0,
            dailyAvgUsd = 42.0,
            bestSeason = "Mar – Jun, Oct – Feb",
            transportTip = "HRTC Volvo overnight buses from Delhi ISBT cost ~$18; rent bikes locally",
            centerLat = 32.2432,
            centerLng = 77.1892,
            highlights = listOf("Solang Valley", "Old Manali Cafes", "Hidimba Devi Temple", "Atal Tunnel & Sissu"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 0.35,
                metroOrBusFareUsd = 0.60,
                autoOrLocalFareUsd = 2.40,
                localTransportSummary = "HRTC local buses cost ₹30–₹50; union taxis have fixed point-to-point rate cards.",
                scamAlerts = listOf(
                    "Check official Manali Taxi Union rate board before hiring cabs to Solang Valley or Rohtang.",
                    "Confirm snow-suit and paragliding package inclusions upfront before paying deposits.",
                    "Carry light cash for Sissu/Atal Tunnel stalls where mobile data drops."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Hindi / Himachali",
                phrases = listOf(
                    LocalPhrase("Hello / Greetings", "नमस्ते (Namaste)", "nuh-mus-TAY"),
                    LocalPhrase("Thank you", "धन्यवाद (Dhanyavaad)", "dhun-yuh-VAAD"),
                    LocalPhrase("How much does it cost?", "कितने का है? (Kitne ka hai?)", "kit-NAY kaa hai"),
                    LocalPhrase("Excuse me", "माफ़ कीजिए (Maaf kijiye)", "maaf kee-jee-YAY"),
                    LocalPhrase("Where is the way to...?", "रास्ता कहाँ है? (Rasta kahan hai?)", "raas-taa kah-HAAN hai"),
                    LocalPhrase("The food is delicious", "बहुत स्वादिष्ट है (Bahut swadisht)", "buh-hoot swaa-DISHT hai")
                ),
                visaTip = "Rohtang Pass requires an online Himachal Govt permit for vehicles; carry original Aadhaar/Passport ID.",
                simTip = "Postpaid Jio or Airtel works best beyond Atal Tunnel & Sissu; prepaid SIMs from outside HP may have limited roaming in Lahaul.",
                policeNumber = "112",
                ambulanceNumber = "108",
                touristHelpline = "1363"
            )
        ),
        DestinationGuide(
            id = "jaipur",
            name = "Jaipur",
            country = "India",
            isIndia = true,
            vibe = TravelVibe.CULTURAL,
            imageUrl = "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 55.0,
            dailyAvgUsd = 48.0,
            bestSeason = "Oct – Mar",
            transportTip = "Use Uber Auto/Ola or Jaipur Metro Pink Line; buy Composite Monument Ticket",
            centerLat = 26.9124,
            centerLng = 75.7873,
            highlights = listOf("Amber Fort", "Hawa Mahal", "City Palace", "Nahargarh Fort Sunset"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 0.25,
                metroOrBusFareUsd = 0.25,
                autoOrLocalFareUsd = 1.40,
                localTransportSummary = "App-booked Uber/Ola Autos average ₹100–₹180 across Pink City forts.",
                scamAlerts = listOf(
                    "Politely decline auto drivers offering '₹20 city tours' that divert to commission gem/textile shops.",
                    "Purchase Amber & Nahargarh Fort tickets only at official counter or Rajasthan Tourism portal.",
                    "Verify weight & silver hallmarks before buying jewelry in Johari Bazaar."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Hindi / Rajasthani",
                phrases = listOf(
                    LocalPhrase("Royal Welcome / Hello", "खम्मा घणी (Khamma Ghani)", "KHUH-maa GHUH-nee"),
                    LocalPhrase("Thank you", "धन्यवाद / शुक्रिया", "dhun-yuh-VAAD / shuk-ree-YAA"),
                    LocalPhrase("How much?", "कितने रुपये? (Kitne rupaye?)", "kit-NAY roo-pay-YAY"),
                    LocalPhrase("Please use the meter", "मीटर से चलिए", "MEE-ter say chuh-lee-YAY"),
                    LocalPhrase("Please help me", "कृपया मदद करें", "krip-yaa muh-dud kuh-RAY"),
                    LocalPhrase("Very good!", "बहुत अच्छा (Bahut achha)", "buh-hoot uch-CHAA")
                ),
                visaTip = "International visitors: Indian e-Tourist Visa accepted at JAI airport; buy the 2-day Composite Monument Pass for major forts.",
                simTip = "Airtel & Jio 5G tourist SIMs available at Jaipur Airport arrival hall with passport & hotel address.",
                policeNumber = "112",
                ambulanceNumber = "108",
                touristHelpline = "1363"
            )
        ),
        DestinationGuide(
            id = "goa",
            name = "Goa",
            country = "India",
            isIndia = true,
            vibe = TravelVibe.BEACH,
            imageUrl = "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 85.0,
            dailyAvgUsd = 55.0,
            bestSeason = "Nov – Feb",
            transportTip = "Rent a yellow-plate scooter (~$5/day) or use official GoaMiles taxi app",
            centerLat = 15.2993,
            centerLng = 74.1240,
            highlights = listOf("Palolem Beach", "Fontainhas Latin Quarter", "Chapora Fort", "Dudhsagar Falls"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 0.45,
                metroOrBusFareUsd = 0.35,
                autoOrLocalFareUsd = 2.50,
                localTransportSummary = "GoaMiles is the official app-based cab service; scooters run ₹400–₹500/day.",
                scamAlerts = listOf(
                    "Only rent scooters with legal yellow-and-black commercial plates and inspect brakes + take video.",
                    "Unmetered beach taxis charge high flat fares — compare with GoaMiles first.",
                    "Avoid handing over original passport as rental deposit; carry a photocopy/DigiLocker ID."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Konkani / Hindi",
                phrases = listOf(
                    LocalPhrase("Thank you", "देव बरें करूं (Dev borem korum)", "dev boh-REM koh-room"),
                    LocalPhrase("Hello", "नमस्कार (Namaskar)", "nuh-mus-KAAR"),
                    LocalPhrase("How much money?", "कितलो पैसो? (Kitlo poiso?)", "kit-LOH poy-SOH"),
                    LocalPhrase("I need help", "म्हाका मदत जाय (Maka modot zai)", "maa-kaa moh-dot zai"),
                    LocalPhrase("Everything is good", "बरें आसा (Borem asa)", "boh-REM aa-saa"),
                    LocalPhrase("Bill, please", "बिल दियात (Bill diat)", "bill dee-YAAT")
                ),
                visaTip = "Carry valid Driver's License + IDP (for international riders) when renting scooters; helmets are mandatory for rider & pillion.",
                simTip = "Jio & Airtel 5G work strongly along North & South Goa beaches; activate UPI International / local cash for shacks.",
                policeNumber = "112",
                ambulanceNumber = "108",
                touristHelpline = "1363"
            )
        ),
        DestinationGuide(
            id = "kerala",
            name = "Kerala",
            country = "India",
            isIndia = true,
            vibe = TravelVibe.SPIRITUAL,
            imageUrl = "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 90.0,
            dailyAvgUsd = 50.0,
            bestSeason = "Sep – Mar",
            transportTip = "SWTD public backwater ferries in Alleppey cost under $0.50 per ride",
            centerLat = 9.4981,
            centerLng = 76.3388,
            highlights = listOf("Alleppey Backwaters", "Munnar Tea Gardens", "Fort Kochi", "Varkala Cliff"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 0.30,
                metroOrBusFareUsd = 0.30,
                autoOrLocalFareUsd = 1.50,
                localTransportSummary = "Kochi Metro + Water Metro and SWTD Alleppey ferries are scenic and ultra-affordable.",
                scamAlerts = listOf(
                    "Book DTPC-certified houseboats in Alleppey and confirm A/C hours & meals in writing.",
                    "In Fort Kochi, ask auto drivers to use the meter or agree on fare before boarding.",
                    "Buy whole spices from verified co-ops in Munnar/Thekkady rather than roadside commission stalls."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Malayalam",
                phrases = listOf(
                    LocalPhrase("Hello / Greetings", "നമസ്കാരം (Namaskaram)", "nuh-mus-KAA-rum"),
                    LocalPhrase("Thank you", "നന്ദി (Nanni)", "NUN-nee"),
                    LocalPhrase("How much is this?", "ഇതിനു എത്ര രൂപയാകും?", "ee-thee-noo eth-raa roo-paa"),
                    LocalPhrase("Please help", "ദയവായി സഹായിക്കൂ", "duh-yuh-vaa-yee suh-haa-yee-koo"),
                    LocalPhrase("Where is the boat jetty?", "ബോട്ട് ജെട്ടി എവിടെയാണ്?", "boat jet-tee eh-vee-day-yaa-noo"),
                    LocalPhrase("Very good / Tasty!", "വളരെ നന്നായി!", "vuh-luh-ray nun-naa-yee")
                ),
                visaTip = "e-Visa valid at Kochi (COK) & Trivandrum (TRV) airports; keep houseboat booking confirmation for Alleppey check-in.",
                simTip = "BSNL/Jio/Airtel have strong coverage across Kochi & Alleppey; Airtel & Jio work best in Munnar tea hills.",
                policeNumber = "112",
                ambulanceNumber = "108",
                touristHelpline = "1363"
            )
        )
    )

    fun findGuideByName(name: String): DestinationGuide {
        val lower = name.trim().lowercase()
        return destinations.firstOrNull {
            lower.contains(it.name.lowercase()) || lower.contains(it.country.lowercase())
        } ?: DestinationGuide(
            id = "custom",
            name = name.ifBlank { "Your Destination" },
            country = "Global",
            isIndia = false,
            vibe = TravelVibe.CITY,
            imageUrl = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1080&q=80",
            baseTransitUsd = 250.0,
            dailyAvgUsd = 95.0,
            bestSeason = "Year-round",
            transportTip = "Check local metro cards and verified ride-hailing apps on arrival",
            centerLat = 28.6139,
            centerLng = 77.2090,
            highlights = listOf("Old Town Walk", "Local Market", "Panoramic Viewpoint"),
            localTips = LocalTipsInfo(
                taxiFarePerKmUsd = 1.10,
                metroOrBusFareUsd = 1.20,
                autoOrLocalFareUsd = 3.50,
                localTransportSummary = "Prefer official transit cards and app-based ride-hailing with upfront pricing.",
                scamAlerts = listOf(
                    "Verify official taxi stands at airports and rail hubs before accepting rides.",
                    "Confirm menu & service charges before ordering at high-traffic tourist squares.",
                    "Keep digital copies of passports and PNRs in your TripBudget Booking Vault."
                )
            ),
            survivalGuide = LocalSurvivalGuide(
                languageName = "Local Travel Essentials",
                phrases = listOf(
                    LocalPhrase("Hello / Good morning", "Local greeting", "Greet warmly with a smile"),
                    LocalPhrase("Thank you", "Local thanks", "Always useful at cafes & shops"),
                    LocalPhrase("How much does this cost?", "Price inquiry", "Point & ask clearly before buying"),
                    LocalPhrase("Excuse me / Please", "Polite opener", "Helps when asking directions"),
                    LocalPhrase("Where is the station?", "Transit query", "Show map pin on your phone"),
                    LocalPhrase("I need help", "Assistance phrase", "Keep hotel card on hand")
                ),
                visaTip = "Verify passport validity (6+ months) and keep offline visa/hotel vouchers inside your Booking Vault.",
                simTip = "Download offline maps and consider a global travel eSIM (Airalo / Holafly) before departure.",
                policeNumber = "112",
                ambulanceNumber = "112",
                touristHelpline = "911 / 112"
            )
        )
    }
}
