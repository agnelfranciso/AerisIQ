package com.example.aerisiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aerisiq.ui.theme.ColorDanger
import com.example.aerisiq.ui.theme.GoogleSansFlex

@Composable
fun InfrastructureScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Infrastructure & Utilities", style = MaterialTheme.typography.headlineLarge, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
        Text("Power and Grid Monitoring", style = MaterialTheme.typography.bodyLarge, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Recent Outages", style = MaterialTheme.typography.titleMedium, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(2) { index ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("KSEB Alert", style = MaterialTheme.typography.labelLarge, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                            Text(if (index == 0) "Ongoing" else "Resolved", color = if (index == 0) ColorDanger else MaterialTheme.colorScheme.primary, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (index == 0) "Emergency power disruption in Kakkanad due to line damage." else "Planned maintenance completed in Vyttila.",
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
