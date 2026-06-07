package com.example.aerisiq.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationEngine(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.w("AerisIQ", "No location permission")
            return null
        }

        return try {
            // Try last known location first (fast)
            val last = fusedLocationClient.lastLocation.await()
            if (last != null) {
                Log.d("AerisIQ", "Got last location: ${last.latitude}, ${last.longitude}")
                return last
            }

            // No cached location — request a fresh one with timeout
            Log.d("AerisIQ", "No last location, requesting fresh fix...")
            requestFreshLocation()
        } catch (e: Exception) {
            Log.e("AerisIQ", "LocationEngine exception: ${e.message}")
            null
        }
    }

    private suspend fun requestFreshLocation(): Location? = withTimeoutOrNull(10_000L) {
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000L)
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation
                    Log.d("AerisIQ", "Fresh location: ${loc?.latitude}, ${loc?.longitude}")
                    cont.resume(loc)
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    request, callback, Looper.getMainLooper()
                )
                cont.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(callback)
                }
            } catch (e: SecurityException) {
                Log.e("AerisIQ", "SecurityException: ${e.message}")
                cont.resume(null)
            }
        }
    }
}
