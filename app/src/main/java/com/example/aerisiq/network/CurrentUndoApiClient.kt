package com.example.aerisiq.network

import android.util.Log
import com.example.aerisiq.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class PowerStatus(
    val pincode: String,
    val district: String,
    val area: String,
    val status: String,          // "outage" | "available" | "unknown"
    val outageCount: Int,
    val restoredCount: Int,
    val lastOutageAt: String?,
    val lastRestoredAt: String?,
    val windowMinutes: Int
)

class CurrentUndoApiClient {

    private val client = OkHttpClient()
    private val apiKey = BuildConfig.CURRENTUNDO_API_KEY  // Set via local.properties — never committed to git
    private val baseUrl = "https://currentundo.com/api/v1"

    suspend fun getPowerStatus(pincode: String): PowerStatus? = withContext(Dispatchers.IO) {
        if (pincode.isBlank()) return@withContext null

        val request = Request.Builder()
            .url("$baseUrl/status?pincode=$pincode")
            .header("Authorization", "Bearer $apiKey")
            .header("User-Agent", "AerisIQ/1.0 (Android)")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                Log.d("AerisIQ", "CurrentUndo status for $pincode → HTTP ${response.code}")
                if (!response.isSuccessful) {
                    Log.e("AerisIQ", "CurrentUndo error: ${response.code}")
                    return@withContext null
                }
                val body = response.body?.string() ?: return@withContext null
                Log.d("AerisIQ", "CurrentUndo response: $body")

                val json = JSONObject(body)
                return@withContext PowerStatus(
                    pincode = json.optString("pincode", pincode),
                    district = json.optString("district", ""),
                    area = json.optString("area", ""),
                    status = json.optString("status", "unknown"),
                    outageCount = json.optInt("outageCount", 0),
                    restoredCount = json.optInt("restoredCount", 0),
                    lastOutageAt = json.optString("lastOutageAt").takeIf { it.isNotEmpty() && it != "null" },
                    lastRestoredAt = json.optString("lastRestoredAt").takeIf { it.isNotEmpty() && it != "null" },
                    windowMinutes = json.optInt("windowMinutes", 30)
                )
            }
        } catch (e: Exception) {
            Log.e("AerisIQ", "CurrentUndo exception: ${e.message}")
            return@withContext null
        }
    }
}
