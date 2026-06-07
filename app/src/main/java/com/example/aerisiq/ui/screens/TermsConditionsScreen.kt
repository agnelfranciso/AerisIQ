package com.example.aerisiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Balance
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
fun TermsConditionsScreen(onBack: () -> Unit) {
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
                text = "Terms & Conditions",
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

        // Card 1: Nature of Service
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
                        text = "1. Nature of Service & Educational Purpose",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AerisIQ is an experimental, student-led application designed for educational and general risk-awareness purposes. It is NOT an official channel for emergency alerts, evacuation coordination, or government announcements. The data aggregated here is fetched from public RSS channels and APIs, including the National Disaster Management Authority (NDMA) Sachet CAP feed and Open-Meteo, which are prone to network lag, server downtime, and inaccuracies. Do not rely on AerisIQ as your primary or exclusive source of critical safety information.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 2: AI limitations & Checklists (Red Warning Style)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorDanger.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorDanger.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ColorDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "2. DO NOT BLINDLY FOLLOW (AI Limitations)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "The suggestions, actions, and briefings generated by the offline Large Language Model (Qwen 2.5) are suggestions compiled from general disaster guidelines. AI outputs may contain errors, omissions, or inaccuracies, and do not reflect local emergency regulations. By using the checklist gateway, you acknowledge that checklists are basic reminders; the act of completing a checklist does not guarantee safety, survival, or risk mitigation. Always prioritize physical sanity checks and official government directions.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 3: Disclaimer of Warranties
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "3. Absolute Disclaimer of Warranties",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, THE APPLICATION, THE OFFLINE AI ENGINE, THE RECOMMENDATIONS, AND ALL COMPILED WEATHER OR HAZARD DATA ARE PROVIDED ON AN 'AS IS' AND 'AS AVAILABLE' BASIS. THE DEVELOPERS EXPLICITLY DISCLAIM ALL WARRANTIES OF ANY KIND, WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OPERATIONAL TIMELINESS, NON-INFRINGEMENT, AND DATA ACCURACY. YOU USE THIS APP AT YOUR OWN RISK AND DISCRETION.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 4: Limit of Liability (Red Warning Style)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorDanger.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorDanger.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = ColorDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "4. WE ARE NOT RESPONSIBLE (Limitation of Liability)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "UNDER NO LEGAL THEORY (WHETHER IN CONTRACT, TORT, STRICT LIABILITY, NEGLIGENCE, OR OTHERWISE) SHALL AGNEL FRANCIS OLAKKENGIL, OR ANY ASSOCIATED DEVELOPERS, ADVISORS, AND CONTRIBUTORS, BE LIABLE TO YOU OR ANY THIRD PARTY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY, OR PUNITIVE DAMAGES. THIS EXCLUSION INCLUDES, BUT IS NOT LIMITED TO, PERSONAL INJURY, LOSS OF LIFE, EMOTIONAL DISTRESS, PROPERTY LOSS, PHYSICAL INJURY, CRITICAL INFRASTRUCTURE DAMAGE, POWER FAILURE DAMAGE, OR COMPROMISED RESCUE OPERATIONS, ARISING OUT OF OR IN CONNECTION WITH THE USE, INABILITY TO USE, MISUSE, DELAYS, FAILS, OR RELIANCE UPON THIS APP OR ITS WARNING FEEDS.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 5: Indemnification Clause
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "5. Comprehensive Indemnification",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You agree to defend, indemnify, and hold harmless Agnel Francis Olakkengil, and any co-developers, advisors, or representatives, from and against any and all claims, damages, liabilities, losses, judgments, settlements, demands, and costs (including reasonable legal fees and court expenses) arising from or relating to your use or misuse of AerisIQ, your reliance on its intelligence alerts, or your violation of these terms.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }

        // Card 6: Governing Law
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Balance, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "6. Governing Law & Dispute Resolution",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "These terms shall be governed by, construed, and enforced in accordance with the laws of the Republic of India, without regard to its conflict of law principles. You agree that any dispute, claim, or litigation arising out of your use of the application must be filed exclusively in the courts located in Thrissur, Kerala, India, and you hereby consent to the personal jurisdiction and venue of such courts. You explicitly waive any right to initiate or participate in class-action lawsuits or group arbitrations against the developers.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontFamily = GoogleSansFlex,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
