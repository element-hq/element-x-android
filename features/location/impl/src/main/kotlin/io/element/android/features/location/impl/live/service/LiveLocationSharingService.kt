/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.IBinder
import androidx.core.app.ServiceCompat
import dev.zacsweers.metro.Inject
import io.element.android.features.location.impl.di.LocationBindings
import io.element.android.features.location.impl.live.notification.LiveLocationSharingNotificationCreator
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.DesiredAccuracy
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import io.element.android.features.location.api.Location as ApiLocation

class LiveLocationSharingService : Service() {

    companion object {
        private const val UPDATE_INTERVAL_IN_SECOND = 60
        private const val MIN_DISTANCE_METERS = 10f
    }

    @Inject lateinit var coordinator: LiveLocationSharingCoordinator
    @Inject lateinit var notificationCreator: LiveLocationSharingNotificationCreator

    @Inject lateinit var appForegroundStateService: AppForegroundStateService
    @AppCoroutineScope
    @Inject lateinit var appCoroutineScope: CoroutineScope
    private lateinit var coroutineScope: CoroutineScope

    override fun onBind(p0: Intent?): IBinder? = null

    @OptIn(FlowPreview::class)
    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        Timber.d("LiveLocationSharingService onCreate")
        runCatching {
            bindings<LocationBindings>().inject(this)
            appForegroundStateService.updateIsSharingLiveLocation(true)
            coroutineScope = appCoroutineScope.childScope(Dispatchers.Default, "LiveLocationSharingService")
            Timber.d("LiveLocationSharingService creating location provider with updateInterval=${UPDATE_INTERVAL_IN_SECOND}s, minDistance=${MIN_DISTANCE_METERS}m")
            val locationProvider = AndroidLocationProvider(
                context = applicationContext,
                updateInterval = UPDATE_INTERVAL_IN_SECOND.seconds,
                minDistanceMeters = MIN_DISTANCE_METERS,
                desiredAccuracy = DesiredAccuracy.Balanced,
                coroutineScope = coroutineScope
            )
            val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.LIVE_LOCATION)
            Timber.d("LiveLocationSharingService starting foreground service with notificationId=$notificationId")
            ServiceCompat.startForeground(
                /* service = */ this@LiveLocationSharingService,
                /* id = */ notificationId,
                /* notification = */ notificationCreator.createNotification(),
                /* foregroundServiceType = */ FOREGROUND_SERVICE_TYPE_LOCATION
            )
            Timber.d("LiveLocationSharingService listening to location updates")
            locationProvider.location
                .filterNotNull()
                .map { location ->
                    ApiLocation(
                        lat = location.position.latitude,
                        lon = location.position.longitude,
                        accuracy = location.accuracy.toFloat(),
                    )
                }
                .onEach(coordinator::dispatch)
                .launchIn(coroutineScope)
        }.onFailure {
            Timber.e(it, "Failed to start live location sharing service")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("LiveLocationSharingService onStartCommand startId=$startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("LiveLocationSharingService onDestroy")
        if (::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
        appForegroundStateService.updateIsSharingLiveLocation(false)
        super.onDestroy()
    }
}
