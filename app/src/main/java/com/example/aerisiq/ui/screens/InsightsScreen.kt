package com.example.aerisiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aerisiq.ui.theme.GoogleSansFlex

@Composable
fun InsightsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Disaster Intelligence Learning", style = MaterialTheme.typography.headlineLarge, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(2) { index ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (index == 0) "Analysis: 2018 Kerala Floods" else "Analysis: Idukki Landslides Pattern",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "What caused it?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (index == 0) "Extremely heavy rainfall coinciding with full reservoir capacities." else "Prolonged rainfall on steep, destabilized terrain.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "What could reduce impact?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (index == 0) "Advanced reservoir management and early warning coordination." else "Avoiding construction in red-zone areas and maintaining vegetative cover.",
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
