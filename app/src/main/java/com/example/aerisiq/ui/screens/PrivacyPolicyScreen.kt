package com.example.aerisiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerisiq.ui.theme.*

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Privacy Policy",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Effective Date: June 7, 2026\nLast Updated: June 7, 2026",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = GoogleSansFlex,
            modifier = Modifier.padding(start = 4.dp, bottom = 24.dp)
        )

        // Card 1: Offline Processing
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1. 100% Local Offline AI Execution",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AerisIQ values your privacy. Unlike other weather assistants, our intelligence core (Qwen 2.5 0.5B) executes 100% locally on your device. Your conversations, safety briefings, and alert descriptions are parsed locally and are never transmitted to outside cloud servers. The entire computational footprint of the Large Language Model remains securely sandbox-contained within your device memory.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 2: Location Data
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "2. Local Coordinates & Geological Mapping",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "To fetch local weather and disaster reports, your coordinates are temporarily mapped to geological details (State and District) using a secure reverse geocoding API (e.g., BigDataCloud). If you do not intend to share this location telemetry, you can instantly turn off location access in the Settings menu and set your location manually using the District selector on the Dashboard home screen. We do not store or track your history of physical movements.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 3: Storage & Preferences
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "3. Persistent Preferences Storage",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Preferences like safety checklists completed, manual locations selected, model weight download states, and filter parameters are written locally to your device's SharedPreferences database. We do not use third-party analytics trackers, advertising networks, or identifiers to profile your behavior.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 4: Zero Cloud Telemetry & Logs
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "4. Zero Cloud Logs & Data Retention",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "The application does not run any background logging tasks that transmit application logs, crash reports, stack traces, or usage heatmaps to external servers. Any debug logs generated are output directly to the Android System logcat facility, accessible only physically or through secure debugging protocols on your hardware.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 5: External API Interfaces
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "5. direct Client-Side Connectivity",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "All outgoing network connections (to NDMA and Open-Meteo endpoints) are initiated directly from your device to the official public server endpoints. We do not operate any middleman proxy servers, data scraping warehouses, or analytical redirectors. Your network calls are subject to the respective privacy policies of the government NDMA and Open-Meteo portals.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 6: User Purge Control
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "6. Complete Data Deletion & Purging",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Because we do not sync or store your data online, you have absolute control over your information. Clearing the application's data via Android Storage settings or uninstalling AerisIQ completely and instantly purges all local databases, downloaded AI weights, checklists, and configurations from the device memory. No traces remain on any remote systems.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
