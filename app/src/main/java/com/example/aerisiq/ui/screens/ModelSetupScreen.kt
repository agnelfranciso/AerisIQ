package com.example.aerisiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aerisiq.ai.AiManager
import com.example.aerisiq.ai.ModelDownloader

@Composable
fun ModelSetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val downloader = remember { ModelDownloader(context) }
    val aiManager = remember { AiManager.getInstance(context) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    // If the model is already downloaded, proceed.
    // If actively downloading, track progress here.
    LaunchedEffect(isDownloading) {
        if (downloader.isModelDownloaded()) {
            try {
                aiManager.initializeModel()
                onSetupComplete()
            } catch (e: Exception) {
                errorMsg = e.message
            }
        } else {
            val id = downloader.getActiveDownloadId()
            if (id != -1L) {
                isDownloading = true
                while (!downloader.isModelDownloaded()) {
                    kotlinx.coroutines.delay(1000)
                    progress = downloader.getDownloadProgress(id)
                    if (progress >= 1f || progress.isNaN()) {
                        kotlinx.coroutines.delay(2000)
                        if (downloader.isModelDownloaded()) {
                            try { aiManager.initializeModel() } catch (e: Exception) {}
                            onSetupComplete()
                            break
                        }
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Setup",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AI Intelligence Setup",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AerisIQ requires the local Qwen 2.5 AI model to interpret real-time safety and risk data directly on your device. The ~546 MB model will download securely.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (errorMsg != null) {
                Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isDownloading) {
                Text("Downloading the Qwen 2.5 engine...", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                // Official Material 3 Expressive Flat Thick Progress Indicator
                LinearProgressIndicator(
                    progress = if (progress.isNaN() || progress < 0f) 0f else progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(progress * 100).toInt().coerceIn(0, 100)}%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            } else {
                Button(
                    onClick = {
                        downloader.downloadModel()
                        isDownloading = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Start Download", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
