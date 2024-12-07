/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.element.android.features.location.api.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LocationForegroundService : Service() {

    companion object {
        private val _locationFlow = MutableStateFlow<Location?>(null)
        val locationFlow = _locationFlow.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // check for location permissions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, createNotification())
        }

        handleLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        // move to NotificationChannels
        // check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationChannelId = "location_channel"
            val channelName = "Location Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            NotificationChannel(locationChannelId, channelName, importance).apply {
                description = "Running service to find your location"
                with((this@LocationForegroundService.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)) {
                    createNotificationChannel(this@apply)
                }
            }
        }

        return NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Location Service")
            .setContentText("Running")
            .setSmallIcon(io.element.android.libraries.designsystem.R.drawable.ic_notification_small)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun handleLocationUpdates() {
        val locationRequest = LocationRequest.Builder(3 * 1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    val matrixLocation = Location(location.latitude, location.longitude, location.accuracy)
                    scope.launch {
                        Timber.e(matrixLocation.toGeoUri())
                        _locationFlow.emit(matrixLocation)
                    }
                }
            }
        }
        // check permissions
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
}
