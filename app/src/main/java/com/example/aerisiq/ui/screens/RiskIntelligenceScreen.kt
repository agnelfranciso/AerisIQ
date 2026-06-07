package com.example.aerisiq.ui.screens

import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aerisiq.ui.theme.*
import com.example.aerisiq.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.filled.Warning

@Composable
fun RiskIntelligenceScreen() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    val sachetAlert by viewModel.sachetAlert.collectAsState()
    val sachetLocation by viewModel.sachetLocation.collectAsState()
    val aiInsightText by viewModel.aiInsightText.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isFetching by viewModel.isFetchingAlerts.collectAsState()
    val completedChecklistAlertId by viewModel.completedChecklistAlertId.collectAsState()
    val scrollState = rememberScrollState()

    val weather by viewModel.weatherInfo.collectAsState()
    val weatherStr = weather ?: ""
    val precipValue = Regex("Precipitation: ([0-9.]+)mm").find(weatherStr)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    val windValue = Regex("Wind Speed: ([0-9.]+) km/h").find(weatherStr)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    val localCondition = Regex("Conditions: ([^,]+)").find(weatherStr)?.groupValues?.get(1) ?: ""
    val isLocalRaining = precipValue > 0.0 || localCondition.lowercase().contains("rain") || localCondition.lowercase().contains("shower") || localCondition.lowercase().contains("drizzle")

    // Derive dynamic risk scores combining official alert severity/keywords and real-time local weather metrics
    val severity = sachetAlert?.severity?.lowercase() ?: "unknown"
    val desc = sachetAlert?.description ?: ""

    // 1. Flood Risk: Combines NDMA heavy rain warnings with live local precipitation metrics
    val floodBase = when {
        desc.contains("flood", ignoreCase = true) || desc.contains("very heavy rain", ignoreCase = true) || desc.contains("வெള്ളപ്പൊക്കം", ignoreCase = true) || desc.contains("ഉരുൾപൊട്ടൽ", ignoreCase = true) -> 0.80f
        desc.contains("heavy rain", ignoreCase = true) || desc.contains("മഴ", ignoreCase = true) -> 0.55f
        desc.contains("rain", ignoreCase = true) -> 0.35f
        else -> 0.10f
    }
    val floodWeatherModifier = (precipValue.toFloat() * 0.15f).coerceAtMost(0.20f) + (if (isLocalRaining) 0.05f else 0f)
    val floodRisk = (floodBase + floodWeatherModifier).coerceIn(0.05f, 0.98f)

    // 2. Wind Risk: Scale based on active cyclone advisories plus local telemetry wind speed
    val windBase = when {
        desc.contains("cyclone", ignoreCase = true) || desc.contains("gale", ignoreCase = true) || desc.contains("gusty winds", ignoreCase = true) || desc.contains("കാറ്റ്", ignoreCase = true) -> 0.75f
        desc.contains("thunderstorm", ignoreCase = true) || desc.contains("storm", ignoreCase = true) || desc.contains("മിന്നൽ", ignoreCase = true) -> 0.50f
        else -> 0.10f
    }
    val windWeatherModifier = (windValue.toFloat() / 40f).coerceAtMost(0.25f)
    val windRisk = (windBase + windWeatherModifier).coerceIn(0.05f, 0.98f)

    // 3. Travel Risk: Combines alert priority tier with current visual/traction conditions (rain/wind)
    val travelBase = when (severity) {
        "extreme" -> 0.85f
        "severe" -> 0.65f
        "moderate" -> 0.40f
        else -> 0.15f
    }
    val travelModifier = (if (isLocalRaining) 0.10f else 0f) + (if (windValue > 15f) 0.08f else 0f)
    val travelRisk = (travelBase + travelModifier).coerceIn(0.05f, 0.98f)

    val overallScore = ((floodRisk + windRisk + travelRisk) / 3f * 100).toInt().coerceIn(0, 100)

    val scoreColor = when {
        overallScore >= 70 -> Color(0xFFD32F2F)
        overallScore >= 45 -> Color(0xFFF57C00)
        overallScore >= 20 -> Color(0xFFFBC02D)
        else -> Color(0xFF388E3C)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
            .statusBarsPadding()
            .padding(16.dp)
            .padding(bottom = 100.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Risk Intelligence", color = Color.White, fontSize = 26.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
        Text(sachetLocation, color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(20.dp))

        if (isFetching) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (sachetAlert == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceTranslucent)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active alerts — risk levels are nominal for your area.", color = Color(0xFF4CAF50), fontSize = 15.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
            }
            // Show Live Weather Card even when there is no alert
            if (!weather.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OpenMeteoWeatherCard(weather!!)
            }
        } else {
            // Overall Risk Score Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Text("Overall Risk Score", color = Color.Gray, fontSize = 13.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            progress = overallScore / 100f,
                            color = scoreColor,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 7.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("$overallScore / 100", color = scoreColor, fontSize = 32.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
                            Text(
                                when { overallScore >= 70 -> "HIGH RISK" ; overallScore >= 45 -> "MODERATE" ; overallScore >= 20 -> "LOW-MODERATE" ; else -> "SAFE" },
                                color = scoreColor, fontSize = 12.sp, letterSpacing = 1.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk Breakdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Text("Risk Breakdown", color = Color.White, fontSize = 16.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    DynamicRiskItem("Flood / Heavy Rain Risk", floodRisk)
                    Spacer(modifier = Modifier.height(12.dp))
                    DynamicRiskItem("Wind / Storm Risk", windRisk)
                    Spacer(modifier = Modifier.height(12.dp))
                    DynamicRiskItem("Travel Disruption Risk", travelRisk)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Intelligence Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D1B2A))
                    .padding(20.dp)
            ) {
                val isRedAlert = sachetAlert?.let { it.aiColor.uppercase() == "RED" || it.severity.lowercase() == "extreme" } == true
                val isChecklistCompleted = sachetAlert?.let { completedChecklistAlertId == it.identifier } == true

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRedAlert && !isChecklistCompleted) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isRedAlert && !isChecklistCompleted) ColorDanger else PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Risk Analysis",
                            color = if (isRedAlert && !isChecklistCompleted) ColorDanger else PrimaryBlue,
                            fontSize = 14.sp,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRedAlert && !isChecklistCompleted) {
                        Text(
                            "Safety Checklist Required\n\nAn extreme threat level is active in your area. For your physical safety, you must complete the AerisIQ Disaster Management Checklist on the home Dashboard to unlock live AI risk intelligence.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontFamily = GoogleSansFlex,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing alert data...", color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                        }
                    } else if (aiInsightText.isNotEmpty()) {
                        Text(aiInsightText, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, fontFamily = GoogleSansFlex, lineHeight = 22.sp, fontWeight = FontWeight.Normal)

                        if (aiSuggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recommended Actions", color = Color.Gray, fontSize = 12.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            aiSuggestions.forEach { suggestion ->
                                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                    Text("•", color = PrimaryBlue, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp, top = 2.dp), fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                                    Text(suggestion, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontFamily = GoogleSansFlex, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
                                }
                            }
                        }
                    } else {
                        Text("AI insight will appear after alerts are loaded.", color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
                    }
                }
            }

            // Live Open-Meteo Weather Card below intelligence card
            if (!weather.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OpenMeteoWeatherCard(weather!!)
            }
        }
    }
}

private @Composable
fun OpenMeteoWeatherCard(weather: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Live Weather Metrics",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = GoogleSansFlex
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val items = weather.split(", ")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { item ->
                            val parts = item.split(": ")
                            if (parts.size == 2) {
                                val label = parts[0].trim()
                                val value = parts[1].trim()
                                val (icon, tint) = when {
                                    label.contains("Temp", ignoreCase = true) -> Pair(Icons.Default.Thermostat, Color(0xFFFF8A65))
                                    label.contains("Humidity", ignoreCase = true) -> Pair(Icons.Default.WaterDrop, Color(0xFF64B5F6))
                                    label.contains("Wind", ignoreCase = true) -> Pair(Icons.Default.Air, Color(0xFF81C784))
                                    label.contains("Precipitation", ignoreCase = true) -> Pair(Icons.Default.Umbrella, Color(0xFF4FC3F7))
                                    label.contains("Conditions", ignoreCase = true) -> {
                                        if (value.contains("clear", ignoreCase = true)) {
                                            Pair(Icons.Default.WbSunny, Color(0xFFFFD54F))
                                        } else {
                                            Pair(Icons.Default.Cloud, Color(0xFF90A4AE))
                                        }
                                    }
                                    else -> Pair(Icons.Default.Cloud, Color.Gray)
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(tint.copy(alpha = 0.15f), CircleShape)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = label,
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontFamily = GoogleSansFlex,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = value,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontFamily = GoogleSansFlex,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(item, color = Color.White, fontSize = 14.sp, fontFamily = GoogleSansFlex)
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Source: Open-Meteo Realtime Forecast API", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp, fontFamily = GoogleSansFlex)
        }
    }
}

@Composable
fun DynamicRiskItem(name: String, value: Float) {
    val color = when {
        value >= 0.7f -> Color(0xFFD32F2F)
        value >= 0.45f -> Color(0xFFF57C00)
        value >= 0.2f -> Color(0xFFFBC02D)
        else -> Color(0xFF388E3C)
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
            Text("${(value * 100).toInt()}%", color = color, fontSize = 13.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}
