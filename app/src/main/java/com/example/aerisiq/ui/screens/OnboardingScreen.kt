package com.example.aerisiq.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerisiq.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE) }

    val completeOnboarding = { locationEnabled: Boolean ->
        prefs.edit()
            .putBoolean("onboarding_complete", true)
            .putBoolean("location_services_enabled", locationEnabled)
            .apply()
        onOnboardingComplete()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isFineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (isFineLocationGranted) {
                completeOnboarding(true)
            } else {
                Toast.makeText(context, "Location permission not granted. Running in manual location mode.", Toast.LENGTH_LONG).show()
                completeOnboarding(false)
            }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
                .statusBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingSlide(
                        title = "Real-Time Risk Intelligence",
                        description = "AerisIQ parses official public feeds instantly, prioritizing your local safety decision-making over raw, complex meteorological numbers.",
                        icon = Icons.Default.SpaceDashboard
                    )
                    1 -> OnboardingSlide(
                        title = "Local AI Threat Analysis",
                        description = "Powered by the Qwen 2.5 local AI model, safety analysis is computed 100% locally on your device for absolute speed and privacy.",
                        icon = Icons.Default.Memory
                    )
                    2 -> OnboardingSlide(
                        title = "Hyperlocal District Mapping",
                        description = "Enable precise location tracking to automatically detect hazard alerts and regional advisory checklists matched to your district.",
                        icon = Icons.Default.MyLocation
                    )
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pager Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryBlue else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }

                if (pagerState.currentPage == 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { completeOnboarding(false) }
                        ) {
                            Text(
                                "Manual Setup",
                                color = Color.Gray,
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                "Grant Location",
                                color = Color.White,
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Next",
                            color = Color.White,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide(title: String, description: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expressive Glassmorphism Icon container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .background(DarkSurfaceTranslucent, RoundedCornerShape(40.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(40.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(72.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            fontSize = 26.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(18.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
