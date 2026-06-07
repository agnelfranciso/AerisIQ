package com.example.aerisiq.ui.navigation

sealed class Route(val route: String) {
    object Onboarding : Route("onboarding")
    object Setup : Route("setup")
    object Home : Route("home")
    object Alerts : Route("alerts")
    object RiskIntel : Route("risk_intel")
    object KeralaElite : Route("kerala_elite")
    object Infrastructure : Route("infrastructure")
    object Insights : Route("insights")
    object Settings : Route("settings")
    object About : Route("about")
    object PrivacyPolicy : Route("privacy_policy")
    object TermsConditions : Route("terms_conditions")
}
