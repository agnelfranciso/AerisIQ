package com.example.aerisiq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.aerisiq.ui.navigation.Route
import com.example.aerisiq.ui.screens.*
import com.example.aerisiq.ui.theme.PrimaryBlue
import com.example.aerisiq.ui.theme.GoogleSansFlex

private fun getRoutePriority(route: String?): Int {
    return when (route) {
        Route.Onboarding.route -> -2
        Route.Setup.route -> -1
        Route.Home.route -> 0
        Route.Alerts.route -> 1
        Route.RiskIntel.route -> 2
        Route.KeralaElite.route -> 3
        Route.Infrastructure.route -> 3
        Route.Insights.route -> 3
        Route.Settings.route -> 4
        Route.About.route -> 5
        Route.PrivacyPolicy.route -> 5
        Route.TermsConditions.route -> 5
        Route.Contributor.route -> 5
        else -> 0
    }
}

@Composable
fun AerisIQApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("aerisiq_prefs", android.content.Context.MODE_PRIVATE) }
    val onboardingComplete = remember { prefs.getBoolean("onboarding_complete", false) }
    val modelDownloader = remember { com.example.aerisiq.ai.ModelDownloader(context) }
    val modelReady = remember { modelDownloader.isModelDownloaded() }

    // Determine start: skip onboarding if done, skip setup if model ready
    val startDestination = when {
        !onboardingComplete -> Route.Onboarding.route
        !modelReady -> Route.Setup.route
        else -> Route.Home.route
    }

    val showBottomBarAndFab = currentRoute != Route.Setup.route && 
                              currentRoute != Route.Onboarding.route && 
                              currentRoute != Route.Settings.route &&
                              currentRoute != Route.About.route &&
                              currentRoute != Route.PrivacyPolicy.route &&
                              currentRoute != Route.TermsConditions.route &&
                              currentRoute != Route.Contributor.route

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val initial = getRoutePriority(initialState.destination.route)
                val target = getRoutePriority(targetState.destination.route)
                val direction = if (target > initial) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(450, easing = EaseInOutCubic)
                ) + fadeIn(animationSpec = tween(450))
            },
            exitTransition = {
                val initial = getRoutePriority(initialState.destination.route)
                val target = getRoutePriority(targetState.destination.route)
                val direction = if (target > initial) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(450, easing = EaseInOutCubic)
                ) + fadeOut(animationSpec = tween(450))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(450, easing = EaseInOutCubic)
                ) + fadeIn(animationSpec = tween(450))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(450, easing = EaseInOutCubic)
                ) + fadeOut(animationSpec = tween(450))
            }
        ) {
            composable(Route.Onboarding.route) {
                OnboardingScreen(onOnboardingComplete = {
                    navController.navigate(Route.Setup.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Route.Setup.route) {
                ModelSetupScreen(onSetupComplete = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Setup.route) { inclusive = true }
                    }
                })
            }
            composable(Route.Home.route) { HomeScreen(onNavigateToSettings = { navController.navigate(Route.Settings.route) }) }
            composable(Route.Alerts.route) { AlertsScreen() }
            composable(Route.RiskIntel.route) { RiskIntelligenceScreen() }
            composable(Route.KeralaElite.route) { KeralaEliteScreen() }
            composable(Route.Infrastructure.route) { InfrastructureScreen() }
            composable(Route.Insights.route) { InsightsScreen() }
            composable(Route.Settings.route) {
                SettingsScreen(
                    onNavigateToAbout = { navController.navigate(Route.About.route) },
                    onNavigateToPrivacy = { navController.navigate(Route.PrivacyPolicy.route) },
                    onNavigateToTerms = { navController.navigate(Route.TermsConditions.route) },
                    onNavigateToContributor = { navController.navigate(Route.Contributor.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Route.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.TermsConditions.route) {
                TermsConditionsScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Contributor.route) {
                ContributorScreen(onBack = { navController.popBackStack() })
            }
        }

        if (showBottomBarAndFab) {
            // Floating Pill Navigation
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startDestId = navController.graph.findStartDestination().id
                    PillNavItem(
                        icon = if (currentRoute == Route.Home.route) Icons.Filled.SpaceDashboard else Icons.Outlined.SpaceDashboard,
                        label = "Home",
                        selected = currentRoute == Route.Home.route,
                        onClick = {
                            navController.navigate(Route.Home.route) {
                                popUpTo(startDestId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    PillNavItem(
                        icon = if (currentRoute == Route.Alerts.route) Icons.Filled.Campaign else Icons.Outlined.Campaign,
                        label = "Alerts",
                        selected = currentRoute == Route.Alerts.route,
                        onClick = {
                            navController.navigate(Route.Alerts.route) {
                                popUpTo(startDestId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    PillNavItem(
                        icon = if (currentRoute == Route.RiskIntel.route) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                        label = "Intel",
                        selected = currentRoute == Route.RiskIntel.route,
                        onClick = {
                            navController.navigate(Route.RiskIntel.route) {
                                popUpTo(startDestId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PillNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "iconScale"
    )
    val backgroundModifier = if (selected) {
        Modifier.background(PrimaryBlue.copy(alpha = 0.9f), shape = CircleShape)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .then(backgroundModifier)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .animateContentSize(animationSpec = tween(300, easing = EaseInOutCubic))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = GoogleSansFlex
            )
        }
    }
}
