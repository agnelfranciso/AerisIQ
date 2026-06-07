package com.example.aerisiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerisiq.ui.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header with Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "About AerisIQ",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 1: The Platform Mission
        Text(
            text = "PLATFORM MISSION",
            color = PrimaryBlue,
            fontSize = 12.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI-Powered Quality of Life & Risk Management",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AerisIQ is an AI-powered safety intelligence platform designed to elevate individual Quality of Life (QoL) and enhance daily disaster risk management. Rather than operating as a simple notification tool, our core engine actively scans, parses, and overlays multiple safety data layers to help residents build high situational awareness in any district in India.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 2: How It Works
        Text(
            text = "HOW IT WORKS",
            color = PrimaryBlue,
            fontSize = 12.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Consolidated Threat Intelligence",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AerisIQ works by fetching official disaster warnings from government feeds (NDMA CAP / Sachet) in real-time, verifying coordinates, and mapping them against live hyper-local meteorological conditions from Open-Meteo. By processing these combined indicators through our optimized local AI model (Qwen 2.5 0.5B), we deliver objective, clear threat breakdowns and immediate safety steps tailored to your exact coordinates.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 3: Developer Story
        Text(
            text = "THE DEVELOPER STORY",
            color = PrimaryBlue,
            fontSize = 12.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Agnel Francis Olakkengil",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AerisIQ was conceptualized, designed, and developed by Agnel Francis Olakkengil, an engineering student from Thrissur, Kerala. Driven by a desire to create a practical, helpful platform for the Indian public, Agnel built AerisIQ to bridge the gap between technical, raw government XML alerts and simple, actionable daily safety directives. This app is dedicated to helping people live safer, better prepared lives.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Section 4: Crucial Disclaimer
        Text(
            text = "CRUCIAL SAFETY DISCLAIMER",
            color = ColorDanger,
            fontSize = 12.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorDanger.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ColorDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Advisory Suggestions Only",
                        color = ColorDanger,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AerisIQ does not replace official emergency broadcast channels. The safety recommendations, checklists, and AI analyses provided here are advisory suggestions designed for preparedness and general awareness. They are suggestions, not absolute directives. In the event of an imminent threat or active disaster, you MUST follow all local government authorities, NDMA instructions, and civil defense orders.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
