package com.example.aerisiq.ui.screens

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerisiq.network.CapAlert
import com.example.aerisiq.ui.theme.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aerisiq.ui.viewmodels.HomeViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import com.example.aerisiq.data.IndiaStatesData
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: HomeViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)

    val sachetAlert by viewModel.sachetAlert.collectAsState()
    val sachetLocation by viewModel.sachetLocation.collectAsState()
    val aiInsightText by viewModel.aiInsightText.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isFetchingAlerts by viewModel.isFetchingAlerts.collectAsState()
    val isDownloaded by viewModel.isDownloaded.collectAsState()
    val progress by viewModel.downloadProgress.collectAsState()
    val needsLocationPermission by viewModel.needsLocationPermission.collectAsState()
    val isManualLocation by viewModel.isManualLocationActive.collectAsState()

    // Custom Pull-To-Refresh State
    val density = androidx.compose.ui.platform.LocalDensity.current
    var pullDistance by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0 && scrollState.value == 0) {
                    pullDistance = (pullDistance + available.y * 0.5f).coerceAtMost(350f)
                    return Offset(0f, available.y)
                }
                if (available.y < 0 && pullDistance > 0) {
                    val consumed = if (pullDistance + available.y >= 0) available.y else -pullDistance
                    pullDistance += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pullDistance > 180f) {
                    viewModel.refreshData(forceRefresh = true)
                }
                coroutineScope.launch {
                    val anim = androidx.compose.animation.core.Animatable(pullDistance)
                    anim.animateTo(0f, androidx.compose.animation.core.tween(300)) {
                        pullDistance = this.value
                    }
                }
                return Velocity.Zero
            }
        }
    }

    var showLocationDialog by remember { mutableStateOf(false) }
    var showGpsResetConfirmDialog by remember { mutableStateOf(false) }

    // Extreme Threat Checklist States
    var showExtremeChecklistDialog by remember { mutableStateOf(false) }
    val completedChecklistAlertId by viewModel.completedChecklistAlertId.collectAsState()
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    // Reset checks when the alert changes
    LaunchedEffect(sachetAlert) {
        for (i in 0 until 6) {
            checkedStates[i] = false
        }
    }

    // Auto-launch checklist dialog when a RED (extreme) alert is loaded/analyzed
    LaunchedEffect(sachetAlert, isAnalyzing, completedChecklistAlertId) {
        val alert = sachetAlert
        if (alert != null && !isAnalyzing) {
            val isRedAlert = alert.aiColor.uppercase() == "RED" || alert.severity.lowercase() == "extreme"
            if (isRedAlert && completedChecklistAlertId != alert.identifier) {
                showExtremeChecklistDialog = true
            }
        }
    }

    // Runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    // Auto-launch permission dialog when ViewModel signals it's needed (only if NOT in manual location override)
    LaunchedEffect(needsLocationPermission, isManualLocation) {
        if (needsLocationPermission && !isManualLocation) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-launch manual location selector if permission is missing/denied and no manual location is active
    LaunchedEffect(sachetLocation, isManualLocation) {
        if ((sachetLocation == "Location permission needed" || sachetLocation == "Location unavailable") && !isManualLocation) {
            showLocationDialog = true
        }
    }

    if (showLocationDialog) {
        var selectedState by remember { mutableStateOf("") }
        var selectedDistrict by remember { mutableStateOf("") }
        
        var stateQuery by remember { mutableStateOf("") }
        var districtQuery by remember { mutableStateOf("") }
        
        val statesList = remember { IndiaStatesData.statesAndDistricts.keys.toList().sorted() }
        val filteredStates = remember(stateQuery) {
            if (stateQuery.isBlank()) statesList else statesList.filter { it.contains(stateQuery, ignoreCase = true) }
        }
        
        val districtsList = remember(selectedState) {
            IndiaStatesData.statesAndDistricts[selectedState]?.sorted() ?: emptyList()
        }
        val filteredDistricts = remember(districtQuery, selectedState) {
            if (districtQuery.isBlank()) districtsList else districtsList.filter { it.contains(districtQuery, ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Text(
                    "Monitor India Location",
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Search and select a state and district in India to inspect live threat intelligence briefings.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontFamily = GoogleSansFlex
                    )

                    // --- State Selection Area ---
                    if (selectedState.isEmpty()) {
                        OutlinedTextField(
                            value = stateQuery,
                            onValueChange = { stateQuery = it },
                            placeholder = { Text("Search State...", color = Color.Gray, fontFamily = GoogleSansFlex) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                            trailingIcon = {
                                if (stateQuery.isNotEmpty()) {
                                    IconButton(onClick = { stateQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            items(filteredStates) { stateName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedState = stateName
                                            stateQuery = ""
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(stateName, color = Color.White, fontFamily = GoogleSansFlex, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        // Display Selected State Row
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryBlue)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("State Selected", color = Color.Gray, fontSize = 11.sp, fontFamily = GoogleSansFlex)
                                        Text(selectedState, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = GoogleSansFlex)
                                    }
                                }
                                TextButton(onClick = {
                                    selectedState = ""
                                    selectedDistrict = ""
                                    stateQuery = ""
                                    districtQuery = ""
                                }) {
                                    Text("Change", color = PrimaryBlue, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- District Selection Area ---
                    if (selectedState.isNotEmpty()) {
                        if (selectedDistrict.isEmpty()) {
                            OutlinedTextField(
                                value = districtQuery,
                                onValueChange = { districtQuery = it },
                                placeholder = { Text("Search District...", color = Color.Gray, fontFamily = GoogleSansFlex) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                                trailingIcon = {
                                    if (districtQuery.isNotEmpty()) {
                                        IconButton(onClick = { districtQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                items(filteredDistricts) { distName ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedDistrict = distName
                                                districtQuery = ""
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(distName, color = Color.White, fontFamily = GoogleSansFlex, fontSize = 14.sp)
                                    }
                                }
                            }
                        } else {
                            // Display Selected District Row
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("District Selected", color = Color.Gray, fontSize = 11.sp, fontFamily = GoogleSansFlex)
                                            Text(selectedDistrict, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = GoogleSansFlex)
                                        }
                                    }
                                    TextButton(onClick = {
                                        selectedDistrict = ""
                                        districtQuery = ""
                                    }) {
                                        Text("Change", color = PrimaryBlue, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedState.isNotEmpty() && selectedDistrict.isNotEmpty()) {
                            viewModel.setManualLocation(selectedState, selectedDistrict)
                            showLocationDialog = false
                        }
                    },
                    enabled = selectedState.isNotEmpty() && selectedDistrict.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Apply", fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showGpsResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showGpsResetConfirmDialog = false },
            title = {
                Text(
                    "Reset to GPS Location",
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to stop monitoring the selected manual location and reset to your device's live GPS coordinates?",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontFamily = GoogleSansFlex
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearManualLocation()
                        showGpsResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Confirm", fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsResetConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val activeAlertForChecklist = sachetAlert
    if (showExtremeChecklistDialog && activeAlertForChecklist != null) {
        val checklistItems = listOf(
            "Look outside your home to see if anything is visibly wrong.",
            "If safe, inspect your immediate 300-500m radius for hazards.",
            "Communicate with your neighbours to exchange local intelligence.",
            "Contact your Ward Member or Panchayat President if anything is wrong.",
            "Check regional news channels and social media to gauge the danger.",
            "Closely follow local government and Disaster Management directives."
        )

        AlertDialog(
            onDismissRequest = { /* Prevent dismiss unless completed or canceled */ },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = ColorDanger, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (activeAlertForChecklist.identifier.startsWith("mock")) {
                                "Disaster Management (Simulated)"
                            } else {
                                "Disaster Management"
                            },
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "AerisIQ Safety Protocol Checklist",
                        color = ColorDanger,
                        fontSize = 12.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (activeAlertForChecklist.identifier.startsWith("mock")) {
                            "[SIMULATION] Complete these checklist items to practice the safety protocol and unlock mock AI insights:"
                        } else {
                            "To protect yourself and unlock risk intelligence, complete these safety steps:"
                        },
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontFamily = GoogleSansFlex
                    )
                    
                    checklistItems.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkedStates[index] = !checkedStates[index] }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { checkedStates[index] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ColorDanger,
                                    uncheckedColor = Color.Gray
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "${index + 1}. $item",
                                color = if (checkedStates[index]) Color.White else Color.LightGray,
                                fontSize = 13.sp,
                                fontFamily = GoogleSansFlex,
                                fontWeight = if (checkedStates[index]) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (checkedStates.all { it }) {
                            viewModel.markChecklistCompleted(activeAlertForChecklist.identifier)
                            showExtremeChecklistDialog = false
                        }
                    },
                    enabled = checkedStates.all { it },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorDanger,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Unlock AI Analysis", fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtremeChecklistDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
                .statusBarsPadding()
                .padding(16.dp)
                .padding(bottom = 100.dp) // Restore padding
                .verticalScroll(scrollState)
        ) {
            // Location Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showLocationDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isManualLocation) Icons.Default.Place else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isManualLocation) "Monitoring Override" else "Current Location (GPS)",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = sachetLocation, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = GoogleSansFlex)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (isManualLocation) {
                                showGpsResetConfirmDialog = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                            contentDescription = "Reset to GPS Location",
                            tint = if (isManualLocation) Color.Gray.copy(alpha = 0.5f) else PrimaryBlue
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isFetchingAlerts && sachetAlert == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fetching Live NDMA Alerts...", color = Color.Gray, fontSize = 14.sp, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
                    }
                }
            } else if (sachetAlert == null) {
                val safeBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        ColorSafe.copy(alpha = 0.15f),
                        DarkSurface
                    )
                )
                Card(
                    shape = ExpressiveShapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(safeBgBrush)
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No Alerts",
                                tint = ColorSafe,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Active Weather Alerts",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = GoogleSansFlex
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your area is currently safe.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Main Alert Card
                val alert = sachetAlert!!
                SachetAlertHero(alert, isAnalyzing)
                Spacer(modifier = Modifier.height(16.dp))
                
                val isRedAlert = alert.aiColor.uppercase() == "RED" || alert.severity.lowercase() == "extreme"
                val isChecklistCompleted = completedChecklistAlertId == alert.identifier

                if (!isDownloaded) {
                    AiDownloadCard(progress)
                } else if (isRedAlert && !isChecklistCompleted) {
                    Card(
                        shape = ExpressiveShapes.large,
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorDanger.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Locked",
                                tint = ColorDanger,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (alert.identifier.startsWith("mock")) "Safety Checklist Required (Simulated)" else "Safety Checklist Required",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = GoogleSansFlex
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (alert.identifier.startsWith("mock")) {
                                    "[SIMULATED THREAT] This is a simulated extreme warning for testing. Complete the checklist to proceed and inspect the mock risk intelligence analysis."
                                } else {
                                    "An extreme threat level is active in your area. For your physical safety, please complete the AerisIQ Disaster Management Checklist to unlock live AI risk intelligence."
                                },
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                fontFamily = GoogleSansFlex,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showExtremeChecklistDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorDanger)
                            ) {
                                Text(
                                    "Start Safety Checklist",
                                    color = Color.White,
                                    fontFamily = GoogleSansFlex,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    AiRiskAnalysis(aiInsightText, aiSuggestions, isAnalyzing)
                }
            }

            // Live Open-Meteo Weather Card below intelligence
            val weather by viewModel.weatherInfo.collectAsState()
            if (!weather.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OpenMeteoWeatherCard(weather!!)
            }
        }

        // Sliding Refresh Indicator at the top (Chrome-like)
        if (pullDistance > 0f || isFetchingAlerts) {
            val yOffset = with(density) { (pullDistance / 2.5f).coerceAtMost(80f).dp }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .offset(y = yOffset)
                        .size(40.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isFetchingAlerts) {
                            CircularProgressIndicator(
                                color = PrimaryBlue,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Pull to refresh",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private @Composable
fun OpenMeteoWeatherCard(weather: String) {
    Card(
        shape = ExpressiveShapes.large,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
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
fun SachetAlertHero(alert: CapAlert, isAnalyzing: Boolean) {
    val (themeColor, icon) = if (isAnalyzing) {
        Pair(Color.Gray, Icons.Default.Info)
    } else {
        when (alert.aiColor.uppercase()) {
            "RED" -> Pair(ColorDanger, Icons.Rounded.Warning)
            "ORANGE" -> Pair(ColorWarningOrange, Icons.Rounded.Warning)
            "YELLOW" -> Pair(ColorCaution, Icons.Rounded.Warning)
            "BLUE" -> Pair(PrimaryBlue, Icons.Default.Info)
            "GREEN" -> Pair(ColorSafe, Icons.Default.Info)
            else -> {
                val isSevere = alert.severity.lowercase() == "severe" || alert.severity.lowercase() == "extreme"
                if (isSevere) Pair(ColorDanger, Icons.Rounded.Warning) else Pair(PrimaryBlue, Icons.Default.Info)
            }
        }
    }
    
    val bgBrush = if (isAnalyzing) {
        Brush.verticalGradient(
            colors = listOf(
                DarkSurfaceTranslucent,
                DarkSurface
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                themeColor.copy(alpha = 0.25f),
                DarkSurface
            )
        )
    }

    Card(
        shape = ExpressiveShapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Alert",
                        tint = themeColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isAnalyzing) "ANALYZING THREAT SEVERITY..." else if (alert.identifier.startsWith("mock")) "OFFICIAL ALERT (SIMULATED)" else "OFFICIAL ALERT",
                        color = themeColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = GoogleSansFlex,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
                
                // Timeline display
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        timelineText,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    alert.headline,
                    color = DarkOnSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = GoogleSansFlex,
                    lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 1.1f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    alert.areaDesc,
                    color = DarkOnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AiRiskAnalysis(insightText: String, suggestions: List<String>, isAnalyzing: Boolean) {
    Card(
        shape = ExpressiveShapes.large,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Risk Intelligence",
                    color = ChartLineYellow,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = GoogleSansFlex
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ChartLineYellow,
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                insightText,
                color = DarkOnSurface,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Normal
            )
            
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Action Plan",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = GoogleSansFlex
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    suggestions.forEach { suggestion ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(PrimaryBlue, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                suggestion,
                                color = DarkOnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiDownloadCard(progress: Float) {
    Card(
        shape = ExpressiveShapes.large,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Intelligence Core Offline", color = DarkOnSurfaceVariant, style = MaterialTheme.typography.titleMedium, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Downloading engine to provide risk analysis...", color = DarkOnSurfaceVariant, style = MaterialTheme.typography.bodyMedium, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = if (progress.isNaN() || progress < 0f) 0f else progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = PrimaryBlue,
                trackColor = DarkSurfaceTranslucent,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(progress * 100).toInt().coerceIn(0, 100)}%", color = PrimaryBlue, style = MaterialTheme.typography.labelSmall, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
        }
    }
}
