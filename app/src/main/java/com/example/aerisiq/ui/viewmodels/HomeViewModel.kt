package com.example.aerisiq.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aerisiq.ai.AiManager
import com.example.aerisiq.ai.ModelDownloader
import com.example.aerisiq.location.LocationEngine
import com.example.aerisiq.network.CapAlert
import com.example.aerisiq.network.CurrentUndoApiClient
import com.example.aerisiq.network.PowerStatus
import com.example.aerisiq.network.SachetApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val stateCoordinates = mapOf(
    "Andhra Pradesh" to Pair(15.9129, 79.7400),
    "Arunachal Pradesh" to Pair(28.2180, 94.7278),
    "Assam" to Pair(26.2006, 92.9376),
    "Bihar" to Pair(25.0961, 85.3131),
    "Chhattisgarh" to Pair(21.2787, 81.8661),
    "Goa" to Pair(15.2993, 74.1240),
    "Gujarat" to Pair(22.2587, 71.1924),
    "Haryana" to Pair(29.0588, 76.0856),
    "Himachal Pradesh" to Pair(31.1048, 77.1734),
    "Jharkhand" to Pair(23.6102, 85.2799),
    "Karnataka" to Pair(15.3173, 75.7139),
    "Kerala" to Pair(10.8505, 76.2711),
    "Madhya Pradesh" to Pair(22.9734, 78.6569),
    "Maharashtra" to Pair(19.7515, 75.7139),
    "Manipur" to Pair(24.6637, 93.9063),
    "Meghalaya" to Pair(25.4670, 91.3662),
    "Mizoram" to Pair(23.1645, 92.9376),
    "Nagaland" to Pair(26.1584, 94.5624),
    "Odisha" to Pair(20.9517, 85.0985),
    "Punjab" to Pair(31.1471, 75.3412),
    "Rajasthan" to Pair(27.0238, 74.2179),
    "Sikkim" to Pair(27.5330, 88.5122),
    "Tamil Nadu" to Pair(11.1271, 78.6569),
    "Telangana" to Pair(18.1124, 79.0193),
    "Tripura" to Pair(23.9408, 91.9882),
    "Uttar Pradesh" to Pair(26.8467, 80.9462),
    "Uttarakhand" to Pair(30.0668, 79.0193),
    "West Bengal" to Pair(22.9868, 87.8550),
    "Delhi" to Pair(28.7041, 77.1025),
    "Jammu & Kashmir" to Pair(33.7782, 76.5762),
    "Ladakh" to Pair(34.1526, 77.5771),
    "Puducherry" to Pair(11.9416, 79.8083)
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    
    private val _sachetAlert = MutableStateFlow<CapAlert?>(null)
    val sachetAlert: StateFlow<CapAlert?> = _sachetAlert

    private val _sachetAlerts = MutableStateFlow<List<CapAlert>>(emptyList())
    val sachetAlerts: StateFlow<List<CapAlert>> = _sachetAlerts
    
    private val _sachetLocation = MutableStateFlow("Initializing GPS...")
    val sachetLocation: StateFlow<String> = _sachetLocation
    
    private val _aiInsightText = MutableStateFlow("Initializing Intelligence Core...")
    val aiInsightText: StateFlow<String> = _aiInsightText
    
    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing
    
    private val _isFetchingAlerts = MutableStateFlow(true)
    val isFetchingAlerts: StateFlow<Boolean> = _isFetchingAlerts
    
    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress
    
    private val downloader = ModelDownloader(context)
    private val aiManager = AiManager.getInstance(context)
    private val sachetClient = SachetApiClient(context)
    private val locationEngine = LocationEngine(context)
    private val currentUndoClient = CurrentUndoApiClient()

    // Signals UI to request permission when location is unavailable
    private val _needsLocationPermission = MutableStateFlow(false)
    val needsLocationPermission: StateFlow<Boolean> = _needsLocationPermission

    private val _powerStatus = MutableStateFlow<PowerStatus?>(null)
    val powerStatus: StateFlow<PowerStatus?> = _powerStatus

    private val _isFetchingPower = MutableStateFlow(false)
    val isFetchingPower: StateFlow<Boolean> = _isFetchingPower
    
    private val _weatherInfo = MutableStateFlow<String?>(null)
    val weatherInfo: StateFlow<String?> = _weatherInfo
    
    private var lastWeatherInfo: String? = null

    private val _isManualLocationActive = MutableStateFlow(false)
    val isManualLocationActive: StateFlow<Boolean> = _isManualLocationActive

    private val _completedChecklistAlertId = MutableStateFlow("")
    val completedChecklistAlertId: StateFlow<String> = _completedChecklistAlertId

    fun markChecklistCompleted(alertId: String) {
        _completedChecklistAlertId.value = alertId
        context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("completed_checklist_alert_id", alertId)
            .apply()
    }
    
    init {
        val prefs = context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE)
        _completedChecklistAlertId.value = prefs.getString("completed_checklist_alert_id", "") ?: ""
        _isDownloaded.value = downloader.isModelDownloaded()
        if (!_isDownloaded.value) {
            monitorDownload()
        }
        // Initial load
        refreshData()
    }
    
    private fun monitorDownload() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = downloader.getActiveDownloadId()
            if (id != -1L) {
                while (!downloader.isModelDownloaded()) {
                    delay(1000)
                    val p = downloader.getDownloadProgress(id)
                    _downloadProgress.value = p
                    if (p >= 1f || p.isNaN()) {
                        delay(2000)
                        if (downloader.isModelDownloaded()) {
                            try {
                                aiManager.initializeModel()
                            } catch (e: Exception) {}
                            _isDownloaded.value = true
                            // Re-process insights since model is now downloaded
                            val alert = _sachetAlert.value
                            if (alert != null) {
                                processAiInsights(alert, _sachetLocation.value, lastWeatherInfo)
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    fun setManualLocation(state: String, district: String) {
        val prefs = context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString("manual_state", state)
            .putString("manual_district", district)
            .putBoolean("location_services_enabled", false)
            .apply()
        refreshData()
    }

    fun clearManualLocation() {
        val prefs = context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .remove("manual_state")
            .remove("manual_district")
            .putBoolean("location_services_enabled", true)
            .apply()
        refreshData()
    }
    
    fun refreshData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isFetchingAlerts.value = true
            _powerStatus.value = null

            val prefs = context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE)
            if (forceRefresh) {
                analysisPrefs.edit().clear().apply()
            }
            val locationServicesEnabled = prefs.getBoolean("location_services_enabled", true)
            var manualState = prefs.getString("manual_state", null)
            var manualDistrict = prefs.getString("manual_district", null)

            if (!locationServicesEnabled && (manualState == null || manualDistrict == null)) {
                manualState = "Kerala"
                manualDistrict = "Thrissur"
                prefs.edit()
                    .putString("manual_state", "Kerala")
                    .putString("manual_district", "Thrissur")
                    .apply()
            }

            val state: String
            val district: String
            val pincode: String
            val lat: Double
            val lng: Double

            if (manualState != null && manualDistrict != null) {
                state = manualState
                district = manualDistrict
                pincode = ""
                _isManualLocationActive.value = true
                _sachetLocation.value = "$district, $state"
                val coords = stateCoordinates[state] ?: Pair(20.5937, 78.9629)
                lat = coords.first
                lng = coords.second
            } else {
                _isManualLocationActive.value = false
                // Check permission first
                if (!locationEngine.hasLocationPermission()) {
                    Log.w("AerisIQ", "Location permission missing, requesting from UI")
                    _needsLocationPermission.value = true
                    _sachetLocation.value = "Location permission needed"
                    _isFetchingAlerts.value = false
                    return@launch
                }
                _needsLocationPermission.value = false

                // Get real GPS coordinates
                _sachetLocation.value = "Locating..."
                val location = withContext(Dispatchers.IO) { locationEngine.getCurrentLocation() }

                if (location == null) {
                    Log.e("AerisIQ", "Could not get device location")
                    _sachetLocation.value = "Location unavailable"
                    _isFetchingAlerts.value = false
                    return@launch
                }

                lat = location.latitude
                lng = location.longitude
                Log.d("AerisIQ", "Device location: $lat, $lng")

                // Reverse-geocode to get State + District + Pincode
                val geocoded = withContext(Dispatchers.IO) {
                    sachetClient.getLocationFromCoordinates(lat, lng)
                }
                state = geocoded?.first ?: "Unknown"
                district = geocoded?.second ?: "Unknown"
                pincode = geocoded?.third ?: ""
                _sachetLocation.value = if (district != "Unknown" && state != "Unknown") "$district, $state" else "India"
                Log.d("AerisIQ", "Geocoded: $district, $state, pincode=$pincode")
            }

            // Fetch matching alerts (using overrides if manual, or mock if simulating)
            val isSimulation = prefs.getBoolean("simulate_extreme_feed", false)
            val alertsList = if (isSimulation) {
                listOf(
                    CapAlert(
                        identifier = "mock_extreme_alert_001",
                        headline = "Extreme danger Warning: Flash Floods & Torrential Downpours Predicted for $district",
                        description = "Meteorological reports indicate an extreme weather front active in $district, $state. Torrential rainfall exceeding 200mm within 6 hours is highly likely, compounding waterlogging risks, flash floods, and localized landslides. Residents should avoid unnecessary travel.",
                        severity = "Extreme",
                        instruction = "Immediately move to higher ground if in flood-prone zones. Avoid driving through waterlogged roadways. Secure emergency resources and standby for instructions.",
                        areaDesc = district,
                        sent = "2026-06-06T12:00:00Z",
                        onset = "2026-06-06T12:00:00Z",
                        expires = "2026-06-07T12:00:00Z"
                    )
                )
            } else {
                withContext(Dispatchers.IO) {
                    if (manualState != null && manualDistrict != null) {
                        sachetClient.fetchCapAlerts(overrideState = state, overrideDistrict = district)
                    } else {
                        sachetClient.fetchCapAlerts(lat, lng)
                    }
                }
            }

            val severityFilterIndex = prefs.getInt("severity_filter_index", 1) // 0: All, 1: Moderate+, 2: Severe+
            val filteredAlerts = when (severityFilterIndex) {
                2 -> alertsList.filter { it.severity.lowercase() == "severe" || it.severity.lowercase() == "extreme" }
                1 -> alertsList.filter { it.severity.lowercase() != "minor" && it.severity.lowercase() != "unknown" }
                else -> alertsList
            }

            // Set dynamic color categorizations on all alerts before updating StateFlows
            filteredAlerts.forEach { alert ->
                val cachedColor = analysisPrefs.getString("color_v8_${alert.identifier}", null)
                if (cachedColor != null) {
                    alert.aiColor = cachedColor
                } else {
                    val combined = "${alert.headline} ${alert.description}".lowercase()
                    alert.aiColor = when {
                        combined.contains("flood") || combined.contains("earthquake") || combined.contains("വെള്ളപ്പൊക്കം") || combined.contains("ഭൂകമ്പം") -> "RED"
                        alert.severity.lowercase() == "extreme" || alert.severity.lowercase() == "severe" || combined.contains("cyclone") || combined.contains("landslide") || combined.contains("storm") || combined.contains("rain") || combined.contains("thunder") || combined.contains("മഴ") || combined.contains("ഉരുൾപൊട്ടൽ") -> "ORANGE"
                        alert.severity.lowercase() == "moderate" || combined.contains("minor") -> "YELLOW"
                        combined.contains("advisory") || combined.contains("informational") || alert.severity.lowercase() == "minor" || alert.severity.lowercase() == "unknown" -> "BLUE"
                        else -> "GREEN"
                    }
                }
            }

            _sachetAlerts.value = filteredAlerts
            val alert = filteredAlerts.firstOrNull()
            _sachetAlert.value = alert
            _isFetchingAlerts.value = false

            // Fetch live weather data from Open-Meteo
            Log.d("AerisIQ", "Fetching weather data from Open-Meteo")
            val wInfo = withContext(Dispatchers.IO) {
                sachetClient.getOpenMeteoWeather(lat, lng)
            }
            lastWeatherInfo = wInfo
            _weatherInfo.value = wInfo
            Log.d("AerisIQ", "Open-Meteo weather data: $wInfo")

            // Fetch power status from CurrentUndo if in Kerala and pincode available
            if (state.contains("Kerala", ignoreCase = true) && pincode.isNotEmpty()) {
                Log.d("AerisIQ", "Fetching CurrentUndo power status for pincode $pincode")
                _isFetchingPower.value = true
                val powerSt = withContext(Dispatchers.IO) {
                    currentUndoClient.getPowerStatus(pincode)
                }
                _powerStatus.value = powerSt
                _isFetchingPower.value = false
                Log.d("AerisIQ", "Power status: ${powerSt?.status} outages=${powerSt?.outageCount}")
            }

            if (alert != null) {
                processAiInsights(alert, "$district, $state", wInfo)
            } else {
                _aiInsightText.value = ""
                _aiSuggestions.value = emptyList()
            }
        }
    }

    /** Called from UI after user grants location permission */
    fun onLocationPermissionGranted() {
        _needsLocationPermission.value = false
        refreshData()
    }
    
    private val analysisPrefs = context.getSharedPreferences("aerisiq_analysis_cache", android.content.Context.MODE_PRIVATE)

    private fun processAiInsights(alert: CapAlert, location: String, weatherInfo: String?) {
        viewModelScope.launch {
            // First check persistent cache (using v8 prefix for dynamic color integration)
            val cachedText = analysisPrefs.getString("text_v8_${alert.identifier}", null)
            val cachedSugSet = analysisPrefs.getStringSet("sug_v8_${alert.identifier}", null)
            val cachedColor = analysisPrefs.getString("color_v8_${alert.identifier}", null)
            if (cachedText != null && cachedSugSet != null) {
                _aiInsightText.value = cachedText
                _aiSuggestions.value = cachedSugSet.toList().sortedBy { it }
                alert.aiColor = cachedColor ?: "GREEN"
                _sachetAlert.value = alert.copy() // Trigger state updates
                _isAnalyzing.value = false
                Log.d("AerisIQ", "Restored cached AI analysis (v8) for alert identifier: ${alert.identifier}")
                return@launch
            }

            if (!downloader.isModelDownloaded()) {
                _aiInsightText.value = "AI Model not fully downloaded yet. Monitoring Sachet feed."
                return@launch
            }
            
            _isAnalyzing.value = true
            try {
                _aiInsightText.value = "Analyzing threat vectors..."
                var parsedColor = ""
                val response = withContext(Dispatchers.IO) {
                    try {
                        aiManager.initializeModel()
                        val prompt = buildString {
                            append("You are AerisIQ, an expert disaster risk analyst for India.\n\n")
                            
                            val isRegionalLanguage = alert.headline.any { it.code > 127 }
                            if (isRegionalLanguage) {
                                append("IMPORTANT: The alert data below is in a regional language. First, translate it mentally to English to understand the event and description, then output your entire response ONLY in English.\n\n")
                            }

                            append("=== ACTIVE ALERT DATA ===\n")
                            append("Location: $location\n")
                            append("Headline: ${alert.headline}\n")
                            append("Severity Level: ${alert.severity}\n")
                            if (alert.description.isNotEmpty()) append("Description: ${alert.description}\n")
                            if (alert.instruction.isNotEmpty()) append("Official NDMA Instruction: ${alert.instruction}\n")
                            if (alert.areaDesc.isNotEmpty()) append("Affected Area: ${alert.areaDesc}\n")
                            if (!weatherInfo.isNullOrEmpty()) {
                                append("Live Local Weather Conditions: $weatherInfo\n")
                            }
                            append("\n=== TONAL GUIDELINES ===\n")
                            append("Maintain a calm, reassuring, professional, and purely objective tone. Do NOT use sensationalist, alarmist, or panic-inducing vocabulary (e.g. avoid words like 'deadly', 'catastrophic', 'devastating', 'flee', 'panic', exclamation marks). Focus strictly on factual risk assessment and sober guidance.\n\n")
                            append("=== YOUR TASK ===\n")
                            append("Write a clear, helpful safety report for a resident at $location in exactly three sections (no labels like '1.', '2.', or '3.', no titles, no headings):\n\n")
                            append("- First, write a single continuous paragraph (2-3 sentences) describing the alert situation, direct risk level, and power status. Integrate the live weather conditions as a contributing/compounding context (e.g. how rain or wind impacts the alert threat), but make sure it is only one part of the analysis paragraph, not the entire paragraph itself. Do not use bullet points or lists in this paragraph. CRITICAL: The paragraph must end as a complete sentence and must NOT end with a colon, transition, or introduction to listing weather metrics.\n")
                            append("- Then, write 3 to 5 short bullet points starting with '-' detailing immediate action steps for the resident's physical safety. CRITICAL: If the alert represents an Extreme or RED threat (such as floods, earthquakes, cyclones, or landslides), these bullet points MUST prioritize immediate, high-priority survival actions (e.g., 'Evacuate to designated relief camps or higher ground immediately', 'Turn off gas valves and main electricity breakers', 'Prepare a wet-proof emergency survival kit', 'Stay completely out of flowing flood waters'). Each bullet point must be a direct instruction on what the resident should DO. DO NOT include any statistics, temperatures, wind speeds, humidity percentages, or precipitation values in the bullet points under any circumstances.\n")
                            append("- Finally, write a single line at the very end of your response starting with 'Color: ' followed by exactly one of: RED, ORANGE, YELLOW, GREEN, or BLUE. Categorize as RED only for extremely immediate, catastrophic life-threatening disasters (specifically floods or earthquakes). Categorize as ORANGE for serious weather alerts requiring high vigilance (such as cyclones, storms, landslides, severe rains). Categorize as YELLOW for minor warning alerts requiring general caution. Categorize as BLUE for basic informational updates or simple weather advisories. Categorize as GREEN for safe/cleared conditions.")
                        }
                        val aiRes = aiManager.generateResponse(prompt)
                        val hasLetters = aiRes.any { it.isLetter() }
                        if (aiRes.startsWith("AI Error:") || aiRes.startsWith("Error:") || aiRes.length < 20 || !hasLetters) {
                            throw Exception("Response too short or invalid: '$aiRes'")
                        }
                        aiRes
                    } catch (e: Throwable) {
                        Log.e("AerisIQ", "AI Model execution failed, building rich fallback report: ${e.message}")
                        val headlineLower = alert.headline.lowercase()
                        val descLower = alert.description.lowercase()
                        val combinedText = "$headlineLower $descLower"
                        
                        val isRainOrFlood = combinedText.contains("rain") || combinedText.contains("flood") || 
                                             combinedText.contains("shower") || combinedText.contains("downpour") ||
                                             combinedText.contains("മഴ") || combinedText.contains("വെള്ളപ്പൊക്കം")
                        
                        val isThunderOrLightning = combinedText.contains("thunder") || combinedText.contains("lightning") || 
                                                   combinedText.contains("storm") || combinedText.contains("cyclone") ||
                                                   combinedText.contains("മിന്നൽ") || combinedText.contains("കാറ്റ്")
                                                   
                        val isLandslide = combinedText.contains("landslide") || combinedText.contains("mudslide") || 
                                          combinedText.contains("ഉരുൾപൊട്ടൽ")
                                          
                        val isHeat = combinedText.contains("heat") || combinedText.contains("high temp") || 
                                     combinedText.contains("ചൂട്")
 
                        val precipValue = Regex("Precipitation: ([0-9.]+)mm").find(weatherInfo ?: "")?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                        val windValue = Regex("Wind Speed: ([0-9.]+) km/h").find(weatherInfo ?: "")?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                        val localCondition = Regex("Conditions: ([^,]+)").find(weatherInfo ?: "")?.groupValues?.get(1) ?: ""
                        
                        val isLocalRaining = precipValue > 0.0 || localCondition.lowercase().contains("rain") || localCondition.lowercase().contains("shower") || localCondition.lowercase().contains("drizzle")
                        val isLocalWindy = windValue > 15.0
 
                        val briefing = buildString {
                            append("An active threat advisory from the National Disaster Management Authority (NDMA) is in effect for $location. ")
                            append("The broader district-wide threat level is categorized as ${alert.severity}. ")
                            if (alert.headline.isNotBlank()) {
                                append("The advisory indicates: ${alert.headline.trim()}. ")
                            }
                            
                            if (isRainOrFlood) {
                                if (isLocalRaining) {
                                    append("Local GPS-level weather reports indicate active precipitation ($precipValue mm), confirming that the threat is currently active and compounding in your immediate vicinity. ")
                                } else {
                                    append("Although the broader district alert is active, local weather sensors report stable dry conditions at your coordinates, indicating that the flood threat is currently dormant in your immediate vicinity. ")
                                }
                            }
                            if (isThunderOrLightning) {
                                if (localCondition.lowercase().contains("thunderstorm")) {
                                    append("Active electrical storms are observed in your local area, creating an immediate lightning risk. ")
                                } else {
                                    append("No active electrical storm is observed at your precise coordinates, but lightning risks remain high across the broader district. ")
                                }
                            }
                            if (isLandslide) {
                                if (isLocalRaining) {
                                    append("Active rain at your coordinates elevates the risk of saturated soil and localized landslides. ")
                                } else {
                                    append("Saturated soil conditions on slopes raise the risk of landslides in the region, though conditions at your immediate coordinates are currently stable. ")
                                }
                            }
                            if (isLocalWindy) {
                                append("Active gusty winds ($windValue km/h) are observed locally, compounding potential hazards. ")
                            }
                        }
                        
                        val recommendations = mutableListOf<String>()
                        if (isRainOrFlood) {
                            recommendations.add("- Avoid crossing or driving through waterlogged roadways and low-lying underpasses.")
                            recommendations.add("- Keep emergency supplies, including dry rations, clean drinking water, and first-aid kits, easily accessible.")
                            recommendations.add("- Move valuable items and electrical appliances to higher floors if residing in flood-prone zones.")
                        }
                        if (isThunderOrLightning) {
                            recommendations.add("- Seek shelter immediately in a sturdy building or fully closed metal vehicle.")
                            recommendations.add("- Unplug sensitive electronic devices and avoid using corded landline phones during active lightning.")
                            recommendations.add("- Stay away from windows, metal pipes, and water faucets until the electrical storm subsides.")
                        }
                        if (isLandslide) {
                            recommendations.add("- Monitor hillsides for signs of sliding land, falling rocks, or unusual soil movement.")
                            recommendations.add("- Avoid non-essential travel along mountain roads or high-slope passes during intense weather.")
                        }
                        if (isHeat) {
                            recommendations.add("- Limit direct exposure to sunlight during peak afternoon hours and drink plenty of fluids.")
                            recommendations.add("- Wear light-colored, loose-fitting cotton clothing and avoid strenuous outdoor activity.")
                        }
                        
                        if (alert.instruction.isNotBlank()) {
                            recommendations.add("- Follow official NDMA guidance: ${alert.instruction.trim()}")
                        }
                        
                        if (recommendations.size < 3) {
                            recommendations.add("- Stay indoors and monitor local news, radio, or AerisIQ updates for live threat briefings.")
                            recommendations.add("- Ensure backup power banks and mobile communication channels are fully charged.")
                            recommendations.add("- Keep emergency contact numbers for local disaster management teams on hand.")
                        }
 
                        val fallbackColor = when {
                            combinedText.contains("flood") || combinedText.contains("earthquake") || combinedText.contains("വെള്ളപ്പൊക്കം") || combinedText.contains("ഭൂകമ്പം") -> "RED"
                            alert.severity.lowercase() == "extreme" || alert.severity.lowercase() == "severe" || isRainOrFlood || isThunderOrLightning || isLandslide || combinedText.contains("cyclone") -> "ORANGE"
                            alert.severity.lowercase() == "moderate" || combinedText.contains("minor") -> "YELLOW"
                            alert.severity.lowercase() == "minor" || combinedText.contains("advisory") || combinedText.contains("informational") -> "BLUE"
                            else -> "GREEN"
                        }
                        
                        buildString {
                            append(briefing)
                            append("\n")
                            recommendations.forEach { rec ->
                                append(rec)
                                append("\n")
                            }
                            append("Color: $fallbackColor\n")
                        }
                    }
                }
                
                Log.d("AerisIQ", "processAiInsights: raw response from AI or fallback: $response")
                
                val lines = response.split("\n")
                val textParts = mutableListOf<String>()
                val bulletParts = mutableListOf<String>()
                
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("Color:", ignoreCase = true)) {
                        parsedColor = trimmed.substringAfter(":").trim().uppercase()
                    } else if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                        val cleaned = trimmed.removePrefix("-").removePrefix("*").trim()
                        // Filter out headers that LLM might place before lists
                        if (cleaned.isNotEmpty() && 
                            !cleaned.lowercase().endsWith(":") &&
                            !cleaned.lowercase().contains("action plan") && 
                            !cleaned.lowercase().contains("action steps") &&
                            !cleaned.lowercase().contains("recommended actions")) {
                            bulletParts.add(cleaned)
                        }
                    } else if (trimmed.isNotBlank() && 
                               !trimmed.startsWith("1.") && 
                               !trimmed.startsWith("2.") && 
                               !trimmed.lowercase().contains("part ") && 
                               !trimmed.lowercase().contains("situation analysis") && 
                               !trimmed.lowercase().contains("action plan:") && 
                               !trimmed.lowercase().contains("action steps:")) {
                        textParts.add(trimmed)
                    }
                }
                
                // Combine text parts to form a cohesive briefing paragraph
                val fullBriefing = textParts.joinToString(" ")
                    .replace("\\n", " ")
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                Log.d("AerisIQ", "processAiInsights: parsed fullBriefing = $fullBriefing")
                val briefingText = if (fullBriefing.length > 5) fullBriefing else "Active weather advisory is in effect for $location. Please review recommended safety actions."
                _aiInsightText.value = briefingText
                
                // Limit to maximum 5 short action suggestions
                val parsedSuggestions = bulletParts.take(5)
                val finalSuggestions = if (parsedSuggestions.isEmpty()) {
                    listOf("Follow the official instruction: ${alert.instruction}", "Monitor local updates on AerisIQ")
                } else {
                    parsedSuggestions
                }
                _aiSuggestions.value = finalSuggestions
 
                // Classify final color
                val finalColor = when (parsedColor) {
                    "RED", "ORANGE", "YELLOW", "GREEN", "BLUE" -> parsedColor
                    else -> {
                        val combined = "${alert.headline} ${alert.description}".lowercase()
                        when {
                            combined.contains("flood") || combined.contains("earthquake") || combined.contains("വെള്ളപ്പൊക്കം") || combined.contains("ഭൂകമ്പം") -> "RED"
                            alert.severity.lowercase() == "extreme" || alert.severity.lowercase() == "severe" || combined.contains("cyclone") || combined.contains("landslide") || combined.contains("storm") || combined.contains("rain") || combined.contains("thunder") || combined.contains("മഴ") || combined.contains("ഉരുൾപൊട്ടൽ") -> "ORANGE"
                            alert.severity.lowercase() == "moderate" || combined.contains("minor") -> "YELLOW"
                            combined.contains("advisory") || combined.contains("informational") || alert.severity.lowercase() == "minor" || alert.severity.lowercase() == "unknown" -> "BLUE"
                            else -> "GREEN"
                        }
                    }
                }
                alert.aiColor = finalColor
                _sachetAlert.value = alert.copy() // Trigger state update in Compose
 
                // Save to persistent SharedPreferences cache (using v8 prefix for color categorization)
                analysisPrefs.edit()
                    .putString("text_v8_${alert.identifier}", briefingText)
                    .putStringSet("sug_v8_${alert.identifier}", finalSuggestions.toSet())
                    .putString("color_v8_${alert.identifier}", finalColor)
                    .apply()
                Log.d("AerisIQ", "Saved AI analysis (v8) to cache for identifier: ${alert.identifier} with color: $finalColor")
                
            } catch (e: Exception) {
                _aiInsightText.value = "Intelligence Core Offline: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
