package com.example.aerisiq.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader

data class CapAlert(
    val identifier: String,
    val headline: String,
    val description: String,
    val severity: String,
    val instruction: String,
    val areaDesc: String,
    val sent: String = "",
    val effective: String = "",
    val onset: String = "",
    val expires: String = "",
    var aiColor: String = ""
)

class SachetApiClient(context: Context) {
    private val cacheSize = 10L * 1024 * 1024 // 10 MiB
    private val cache = Cache(File(context.cacheDir, "sachet_cap_cache"), cacheSize)
    
    private val client = OkHttpClient.Builder()
        .cache(cache)
        .build()

    // Returns Pair(State, District)
    // Returns Triple(State, District, Pincode)
    suspend fun getLocationFromCoordinates(lat: Double, lng: Double): Triple<String, String, String>? = withContext(Dispatchers.IO) {
        val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lng&localityLanguage=en"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AerisIQ/1.0 (Android)")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)

                var state = ""
                if (json.has("principalSubdivision") && json.getString("principalSubdivision").isNotEmpty()) {
                    state = json.getString("principalSubdivision")
                }

                // Extract pincode from postcode field
                val pincode = json.optString("postcode", "").trim()
                Log.d("AerisIQ", "Geocoding pincode: $pincode")

                var district = ""
                // NDMA Sachet requires District Level (Admin Level 5 in India)
                if (json.has("localityInfo")) {
                    val info = json.getJSONObject("localityInfo")
                    if (info.has("administrative")) {
                        val adminArray = info.getJSONArray("administrative")
                        for (i in 0 until adminArray.length()) {
                            val adminObj = adminArray.getJSONObject(i)
                            if (adminObj.has("adminLevel") && adminObj.getInt("adminLevel") == 5) {
                                district = adminObj.getString("name")
                                district = district.replace(" district", "", ignoreCase = true).trim()
                                break
                            }
                        }
                    }
                }

                // Fallback to locality/city if admin level 5 wasn't found
                if (district.isEmpty()) {
                    if (json.has("locality") && json.getString("locality").isNotEmpty()) {
                        district = json.getString("locality")
                    } else if (json.has("city") && json.getString("city").isNotEmpty()) {
                        district = json.getString("city")
                    }
                }

                if (state.isNotEmpty() && district.isNotEmpty()) {
                    return@withContext Triple(state, district, pincode)
                }

                if (district.isNotEmpty()) return@withContext Triple("Unknown", district, pincode)
                if (state.isNotEmpty()) return@withContext Triple(state, "Unknown", pincode)

                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("AerisIQ", "Geocoding exception: ${e.message}")
            return@withContext null
        }
    }

    // Parse alerts directly from RSS — no secondary FetchXMLFile calls needed
    private suspend fun fetchAlertsFromRss(rssUrl: String): List<CapAlert> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(rssUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
            .build()
        val alerts = mutableListOf<CapAlert>()

        try {
            client.newCall(request).execute().use { response ->
                Log.d("AerisIQ", "RSS response: ${response.code} for $rssUrl")
                if (!response.isSuccessful) {
                    Log.e("AerisIQ", "RSS failed: ${response.code}")
                    return@withContext emptyList()
                }
                val xml = response.body?.string() ?: return@withContext emptyList()
                Log.d("AerisIQ", "RSS fetched OK, length=${xml.length}")

                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xml))

                var eventType = parser.eventType
                var currentText = StringBuilder()
                // Per item accumulators
                var itemTitle = ""
                var itemDesc = ""
                var itemGuid = ""
                var itemSent = ""
                var inItem = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            val tag = parser.name
                            currentText.clear()
                            if (tag == "item") {
                                inItem = true
                                itemTitle = ""
                                itemDesc = ""
                                itemGuid = ""
                                itemSent = ""
                            }
                        }
                        XmlPullParser.TEXT -> currentText.append(parser.text)
                        XmlPullParser.END_TAG -> {
                            val tag = parser.name
                            val text = currentText.toString().trim()
                            if (inItem) {
                                when {
                                    tag == "title" && itemTitle.isEmpty() -> itemTitle = text
                                    tag == "description" && itemDesc.isEmpty() -> itemDesc = text
                                    tag == "guid" || tag.endsWith(":guid") -> itemGuid = text
                                    tag == "pubDate" -> {
                                        // Store pubDate temporarily in sent
                                        itemSent = text
                                    }
                                }
                            }
                            if (tag == "item") {
                                inItem = false
                                if (itemTitle.isNotEmpty() || itemDesc.isNotEmpty()) {
                                    // Parse severity from title (e.g. "Severe: Heavy Rain..." or just from keywords)
                                    val severity = when {
                                        itemTitle.contains("Extreme", ignoreCase = true) -> "Extreme"
                                        itemTitle.contains("Severe", ignoreCase = true) -> "Severe"
                                        itemTitle.contains("Moderate", ignoreCase = true) -> "Moderate"
                                        itemTitle.contains("Minor", ignoreCase = true) -> "Minor"
                                        itemDesc.contains("Very Heavy", ignoreCase = true) -> "Severe"
                                        itemDesc.contains("Heavy", ignoreCase = true) -> "Moderate"
                                        else -> "Unknown"
                                    }
                                    alerts.add(CapAlert(
                                        identifier = itemGuid,
                                        headline = itemTitle,
                                        description = itemDesc,
                                        severity = severity,
                                        instruction = "",
                                        areaDesc = itemDesc, // description often contains district names
                                        sent = itemSent
                                    ))
                                    Log.d("AerisIQ", "Parsed RSS item: title=${itemTitle.take(60)} sent=$itemSent")
                                }
                                itemTitle = ""
                                itemDesc = ""
                                itemGuid = ""
                                itemSent = ""
                            }
                            currentText.clear()
                        }
                    }
                    eventType = parser.next()
                }
                Log.d("AerisIQ", "RSS parsed ${alerts.size} total items")
                return@withContext alerts
            }
        } catch (e: Exception) {
            Log.e("AerisIQ", "RSS fetch exception: ${e.javaClass.simpleName}: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun fetchCapAlerts(
        lat: Double? = null,
        lng: Double? = null,
        overrideState: String? = null,
        overrideDistrict: String? = null
    ): List<CapAlert> = withContext(Dispatchers.IO) {
        var stateName = overrideState ?: "Unknown"
        var districtName = overrideDistrict ?: "Unknown"

        if (stateName == "Unknown" && districtName == "Unknown" && lat != null && lng != null) {
            val location = getLocationFromCoordinates(lat, lng)
            if (location != null) {
                stateName = location.first
                districtName = location.second
            }
        }

        Log.d("AerisIQ", "fetchCapAlerts: state=$stateName, district=$districtName")

        if (districtName == "Unknown") return@withContext emptyList()

        // Build the State RSS URL
        val rssUrl = if (stateName != "Unknown") {
            val formattedState = stateName.lowercase().replace(" ", "_")
            "https://sachet.ndma.gov.in/cap_public_website/rss/rss_$formattedState.xml"
        } else {
            "https://sachet.ndma.gov.in/cap_public_website/rss/rss_india.xml"
        }

        Log.d("AerisIQ", "Fetching RSS from $rssUrl")

        // Parse alerts DIRECTLY from the RSS
        var alerts = fetchAlertsFromRss(rssUrl)
        Log.d("AerisIQ", "Alerts from RSS: ${alerts.size}")

        // Fallback to India feed
        if (alerts.isEmpty() && stateName != "Unknown") {
            Log.d("AerisIQ", "Fallback to India RSS")
            alerts = fetchAlertsFromRss("https://sachet.ndma.gov.in/cap_public_website/rss/rss_india.xml")
        }

        if (alerts.isEmpty()) {
            Log.e("AerisIQ", "No alerts found in any RSS feed")
            return@withContext emptyList()
        }

        // Match district name (handle both English and Malayalam translation)
        val malayalamDistrict = when (districtName.lowercase()) {
            "thrissur" -> "തൃശ്ശൂർ"
            "ernakulam" -> "എറണാകുളം"
            "malappuram" -> "മലപ്പുറം"
            "kozhikode" -> "കോഴിക്കോട്"
            "wayanad" -> "വയനാട്"
            "kannur" -> "കണ്ണൂർ"
            "kasaragod" -> "കാസർഗോഡ്"
            "pathanamthitta" -> "പത്തനംതിട്ട"
            "kottayam" -> "കോട്ടയം"
            "idukki" -> "ഇടുക്കി"
            "palakkad" -> "പാലക്കാട്"
            "thiruvananthapuram" -> "തിരുവനന്തപുരം"
            "kollam" -> "കല്ലം"
            "alappuzha" -> "ആലപ്പുഴ"
            else -> ""
        }

        // Match first 10 alerts containing districtName (in English or Malayalam)
        val matchedRssAlerts = alerts.filter { alert ->
            val cleanHeadline = alert.headline.replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
            val cleanDescription = alert.description.replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
            cleanHeadline.contains(districtName, ignoreCase = true) ||
            cleanDescription.contains(districtName, ignoreCase = true) ||
            (malayalamDistrict.isNotEmpty() && (cleanHeadline.contains(malayalamDistrict) || cleanDescription.contains(malayalamDistrict)))
        }.take(10)

        // Fallback: If no direct district match is found, but the state is Kerala, let's show all Kerala alerts in the Alerts tab!
        val finalMatchedAlerts = if (matchedRssAlerts.isEmpty()) {
            Log.d("AerisIQ", "No direct district match for '$districtName'. Showing all available state alerts.")
            alerts.take(10)
        } else {
            matchedRssAlerts
        }

        Log.d("AerisIQ", "Matched ${finalMatchedAlerts.size} RSS alerts. Fetching full XMLs...")

        // Fetch full XML for the matched alerts in parallel
        val fullAlerts = finalMatchedAlerts.map { rssAlert ->
            async {
                if (rssAlert.identifier.isNotEmpty()) {
                    val xmlUrl = "https://sachet.ndma.gov.in/cap_public_website/FetchXMLFile?identifier=${rssAlert.identifier}"
                    val request = Request.Builder()
                        .url(xmlUrl)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        .build()
                    try {
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val xmlBody = response.body?.string()
                                if (xmlBody != null) {
                                    val parsed = parseCapXml(xmlBody)
                                    if (parsed != null) {
                                        return@async parsed
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AerisIQ", "Failed to fetch full XML for ${rssAlert.identifier}: ${e.message}")
                    }
                }
                // Fallback to RSS data if XML fetch fails
                rssAlert
            }
        }.awaitAll()

        // Helper to check if alert is active based on current time
        val currentTimeMs = System.currentTimeMillis()
        
        fun parseIsoDateTime(dateStr: String): Long {
            if (dateStr.isEmpty()) return 0L
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    return java.time.OffsetDateTime.parse(dateStr).toInstant().toEpochMilli()
                } else {
                    val clean = dateStr.replace("Z", "+0000").let {
                        if (it.length > 22 && it[it.length - 3] == ':') {
                            it.substring(0, it.length - 3) + it.substring(it.length - 2)
                        } else it
                    }
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
                    return sdf.parse(clean)?.time ?: 0L
                }
            } catch (e: Exception) {
                return 0L
            }
        }

        // Filter active alerts using effective/onset and expires
        val activeAlerts = fullAlerts.filter { alert ->
            val onsetTime = parseIsoDateTime(alert.onset.ifEmpty { alert.effective })
            val expiresTime = parseIsoDateTime(alert.expires)
            if (expiresTime > 0L && currentTimeMs > expiresTime) {
                Log.d("AerisIQ", "Filtering out expired alert ${alert.identifier}: expires=$expiresTime, current=$currentTimeMs")
                false
            } else {
                true
            }
        }

        val finalAlerts = if (activeAlerts.isNotEmpty()) activeAlerts else fullAlerts

        // Sort by sent time (newest first)
        val sortedAlerts = finalAlerts.sortedByDescending { alert ->
            parseIsoDateTime(alert.sent)
        }
        return@withContext sortedAlerts
    }

    suspend fun fetchCapAlert(lat: Double? = null, lng: Double? = null): CapAlert? {
        val list = fetchCapAlerts(lat, lng)
        return list.firstOrNull()
    }


    private fun parseCapXml(xml: String): CapAlert? {
        try {
            val cleanXml = xml.replace("cap:", "")
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(cleanXml))

            var eventType = parser.eventType
            var identifier = ""
            var headline = ""
            var description = ""
            var severity = ""
            var instruction = ""
            var areaDesc = ""

            var sent = ""
            var effective = ""
            var onset = ""
            var expires = ""

            var currentTag = ""
            var currentText = StringBuilder()

            // Temporary structures to hold information parsed from different <info> blocks
            // Map of language code -> CapAlert properties
            val infoBlocks = mutableListOf<ParsedInfoBlock>()
            var currentInfo = ParsedInfoBlock()
            var insideInfo = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        currentText.clear() // Reset text builder for new tag
                        if (currentTag == "info") {
                            insideInfo = true
                            currentInfo = ParsedInfoBlock()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        currentText.append(parser.text)
                    }
                    XmlPullParser.END_TAG -> {
                        val text = currentText.toString().trim()
                        if (text.isNotEmpty()) {
                            when (parser.name) {
                                "identifier" -> if (identifier.isEmpty()) identifier = text
                                "sent" -> if (sent.isEmpty()) sent = text
                            }
                            if (insideInfo) {
                                when (parser.name) {
                                    "language" -> currentInfo.language = text
                                    "headline" -> currentInfo.headline = text
                                    "description" -> currentInfo.description = text
                                    "severity" -> currentInfo.severity = text
                                    "instruction" -> currentInfo.instruction = text
                                    "areaDesc" -> currentInfo.areaDesc = text
                                    "effective" -> currentInfo.effective = text
                                    "onset" -> currentInfo.onset = text
                                    "expires" -> currentInfo.expires = text
                                }
                            }
                        }
                        if (parser.name == "info") {
                            insideInfo = false
                            infoBlocks.add(currentInfo)
                        }
                        currentTag = ""
                        currentText.clear()
                    }
                }
                eventType = parser.next()
            }

            if (infoBlocks.isNotEmpty()) {
                // Look for an English info block first (language code contains "en")
                val englishInfo = infoBlocks.firstOrNull { 
                    it.language.contains("en", ignoreCase = true) 
                } ?: infoBlocks.first() // Fallback to first block (which may be regional)

                return CapAlert(
                    identifier = identifier,
                    headline = englishInfo.headline,
                    description = englishInfo.description,
                    severity = englishInfo.severity,
                    instruction = englishInfo.instruction,
                    areaDesc = englishInfo.areaDesc,
                    sent = sent,
                    effective = englishInfo.effective.ifEmpty { effective },
                    onset = englishInfo.onset.ifEmpty { onset },
                    expires = englishInfo.expires.ifEmpty { expires }
                )
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getOpenMeteoWeather(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,showers,snowfall,weather_code,wind_speed_10m"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AerisIQ/1.0 (Android)")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                if (json.has("current")) {
                    val current = json.getJSONObject("current")
                    val temp = current.optDouble("temperature_2m", Double.NaN)
                    val apparentTemp = current.optDouble("apparent_temperature", Double.NaN)
                    val humidity = current.optInt("relative_humidity_2m", -1)
                    val wind = current.optDouble("wind_speed_10m", Double.NaN)
                    val precip = current.optDouble("precipitation", Double.NaN)
                    val code = current.optInt("weather_code", -1)
                    
                    val condition = when (code) {
                        0 -> "Clear sky"
                        1, 2, 3 -> "Partly cloudy/Overcast"
                        45, 48 -> "Fog"
                        51, 53, 55 -> "Drizzle"
                        61, 63 -> "Light/Moderate Rain"
                        65 -> "Heavy Rain"
                        80, 81 -> "Rain showers"
                        82 -> "Violent Rain showers"
                        95 -> "Thunderstorm"
                        96, 99 -> "Thunderstorm with hail"
                        else -> "Unknown conditions"
                    }
                    
                    val parts = mutableListOf<String>()
                    if (!temp.isNaN()) parts.add("Temp: ${temp}°C")
                    if (!apparentTemp.isNaN()) parts.add("Apparent Temp: ${apparentTemp}°C")
                    if (humidity != -1) parts.add("Humidity: ${humidity}%")
                    if (!wind.isNaN()) parts.add("Wind Speed: ${wind} km/h")
                    if (!precip.isNaN()) parts.add("Precipitation: ${precip}mm")
                    if (code != -1) parts.add("Conditions: $condition")
                    
                    return@withContext parts.joinToString(", ")
                }
            }
        } catch (e: Exception) {
            Log.e("AerisIQ", "OpenMeteo fetch failed: ${e.message}")
        }
        return@withContext null
    }

    private class ParsedInfoBlock {
        var language: String = ""
        var headline: String = ""
        var description: String = ""
        var severity: String = ""
        var instruction: String = ""
        var areaDesc: String = ""
        var effective: String = ""
        var onset: String = ""
        var expires: String = ""
    }
}
