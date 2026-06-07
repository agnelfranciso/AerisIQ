package com.example.aerisiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aerisiq.ui.theme.ColorCaution
import com.example.aerisiq.ui.theme.GoogleSansFlex

@Composable
fun KeralaEliteScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kerala Elite Mode", style = MaterialTheme.typography.headlineLarge, fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold)
        Text("Premium Student & Regional Safety", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("School Holiday Likelihood", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ernakulam District: HIGH (85%)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, fontFamily = GoogleSansFlex)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: This is an AI prediction based on historical data and current rainfall intensity. NOT official confirmation.", style = MaterialTheme.typography.bodySmall, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Official Announcements", style = MaterialTheme.typography.titleMedium, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Collectorate Ernakulam", style = MaterialTheme.typography.labelLarge, color = ColorCaution, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold)
                Text("Control room opened at Taluk offices. Public advised to avoid hilly terrains.", style = MaterialTheme.typography.bodyMedium, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal)
            }
        }
    }
}
