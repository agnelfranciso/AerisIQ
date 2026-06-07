package com.example.aerisiq.ui.screens

import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
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

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    val sachetAlerts by viewModel.sachetAlerts.collectAsState()
    val sachetLocation by viewModel.sachetLocation.collectAsState()
    val isFetching by viewModel.isFetchingAlerts.collectAsState()
    val powerStatus by viewModel.powerStatus.collectAsState()
    val isFetchingPower by viewModel.isFetchingPower.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                "Active Alerts",
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                sachetLocation,
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (isFetching) {
            item {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        } else if (sachetAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceTranslucent)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Active Alerts", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, fontFamily = GoogleSansFlex)
                        Text("Your area is currently safe.", color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
                    }
                }
            }
        } else {
            items(sachetAlerts) { alert ->
                val cardColor = when (alert.aiColor.uppercase()) {
                    "RED" -> ColorDanger
                    "ORANGE" -> ColorWarningOrange
                    "YELLOW" -> ColorCaution
                    "BLUE" -> PrimaryBlue
                    "GREEN" -> ColorSafe
                    else -> when (alert.severity.lowercase()) {
                        "extreme" -> ColorDanger
                        "severe" -> ColorWarningOrange
                        "moderate" -> ColorCaution
                        else -> ColorSafe
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor.copy(alpha = 0.15f))
                        .padding(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkBackground)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = cardColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                alert.severity.uppercase(),
                                color = cardColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                fontFamily = GoogleSansFlex,
                                letterSpacing = 1.5.sp
                            )
                        }
                        
                        // Timeline text
                        val onsetTime = alert.onset.ifEmpty { alert.effective }
                        val expiresTime = alert.expires
                        val timelineText = when {
                            onsetTime.isNotEmpty() && expiresTime.isNotEmpty() -> {
                                val cleanOnset = onsetTime.take(16).replace("T", " ")
                                val cleanExpires = expiresTime.take(16).replace("T", " ")
                                "Active: $cleanOnset to $cleanExpires"
                            }
                            onsetTime.isNotEmpty() -> {
                                "Active from: ${onsetTime.take(16).replace("T", " ")}"
                            }
                            alert.sent.isNotEmpty() -> {
                                "Sent: ${alert.sent.take(25)}"
                            }
                            else -> ""
                        }
                        
                        if (timelineText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                timelineText,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            alert.headline,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp
                        )
                        if (alert.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Description", color = Color.Gray, fontSize = 12.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.description, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
                        }
                        if (alert.instruction.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Official Instruction", color = Color.Gray, fontSize = 12.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.instruction, color = Color(0xFF81D4FA), fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Source: NDMA Sachet CAP Feed", color = Color.Gray, fontSize = 11.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { viewModel.refreshData() },
                    modifier = Modifier.fillMaxWidth(),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue))
                ) {
                    Text("Refresh Alerts", color = PrimaryBlue, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // --- CurrentUndo Power Status Card (Kerala only) ---
        if (isFetchingPower) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A2E)).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFFFD700), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Checking power status...", color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else if (powerStatus != null) {
            item {
                val ps = powerStatus!!
                val isOutage = ps.status == "outage"
                val powerColor = if (isOutage) Color(0xFFFF6F00) else Color(0xFF4CAF50)
                val powerBg = if (isOutage) Color(0xFF2A1800) else Color(0xFF0D2A0D)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Power Status", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = GoogleSansFlex)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(powerBg).padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isOutage) "⚡ POWER OUTAGE REPORTED" else "✅ POWER AVAILABLE",
                                color = powerColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, fontFamily = GoogleSansFlex
                            )
                            Text(ps.pincode, color = Color.Gray, fontSize = 12.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                        }
                        if (ps.area.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${ps.area}, ${ps.district}", color = Color.Gray, fontSize = 12.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.White.copy(alpha = 0.06f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                            Column {
                                Text("${ps.outageCount}", color = Color(0xFFFF6F00), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, fontFamily = GoogleSansFlex)
                                Text("Outage\nreports", color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("${ps.restoredCount}", color = Color(0xFF4CAF50), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, fontFamily = GoogleSansFlex)
                                Text("Restored\nreports", color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("${ps.windowMinutes}m", color = PrimaryBlue, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, fontFamily = GoogleSansFlex)
                                Text("Time\nwindow", color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (ps.lastOutageAt != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Last outage: ${ps.lastOutageAt.take(16).replace("T", " ")} UTC",
                                color = Color.Gray, fontSize = 11.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Source: CurrentUndo community reports", color = Color.Gray.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }
    }
}
