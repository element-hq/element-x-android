/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.feral.FeralPushConnectionMonitor
import io.element.android.libraries.pushproviders.feral.FeralPushRegistration
import io.element.android.libraries.pushproviders.feral.FeralPushStore
import io.element.android.libraries.pushproviders.feral.R
import io.element.android.libraries.pushproviders.feral.connection.FeralPushConnection
import io.element.android.libraries.pushproviders.feral.connection.FeralPushConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushConnectionService", LoggerTag.PushLoggerTag)

private const val NOTIFICATION_ID = 1042
private const val CHANNEL_ID = "feral_push_connection"

@ContributesTo(AppScope::class)
interface FeralPushConnectionServiceBindings {
    fun inject(service: FeralPushConnectionService)
}

/**
 * Foreground service (type remoteMessaging) owning one [FeralPushConnection] per session registered
 * with the Feral provider. It follows [FeralPushStore.registrations]: connections are added, replaced
 * or dropped as registrations change, and the service stops itself when none remains.
 */
class FeralPushConnectionService : Service() {
    @Inject lateinit var feralPushStore: FeralPushStore
    @Inject lateinit var connectionFactory: FeralPushConnectionFactory
    @Inject lateinit var monitor: FeralPushConnectionMonitor

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connections = mutableMapOf<SessionId, RunningConnection>()
    private var isOnForeground = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private class RunningConnection(
        val registration: FeralPushRegistration,
        val connection: FeralPushConnection,
        val job: Job,
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bindings<FeralPushConnectionServiceBindings>().inject(this)
        Timber.tag(loggerTag.value).i("Creating the Feral connection service")
        ensureNotificationChannelExists()
        startInForeground()
        if (!isOnForeground) return
        monitor.setServiceRunning(true)
        registerNetworkCallback()
        scope.launch {
            feralPushStore.registrations.collect { registrations -> sync(registrations) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isOnForeground) {
            Timber.tag(loggerTag.value).w("Not running in foreground, stopping to avoid a crash")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.tag(loggerTag.value).i("Destroying the Feral connection service")
        unregisterNetworkCallback()
        scope.cancel()
        synchronized(connections) { connections.clear() }
        monitor.setServiceRunning(false)
        if (isOnForeground) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
        super.onDestroy()
    }

    private fun startInForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(R.string.feral_push_notification_title))
            .setContentText(getString(R.string.feral_push_notification_text))
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
        } else {
            0
        }
        // Can throw ForegroundServiceStartNotAllowedException (API 31+) when started from the background
        // without an exemption: give up for now, FeralPushInitializer / the boot receiver retry later.
        runCatchingExceptions { ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType) }
            .onSuccess { isOnForeground = true }
            .onFailure {
                isOnForeground = false
                Timber.tag(loggerTag.value).e(it, "Unable to start in foreground")
            }
    }

    private fun ensureNotificationChannelExists() {
        NotificationManagerCompat.from(this).createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
                    .setName(getString(R.string.feral_push_channel_name))
                    .setVibrationEnabled(false)
                    .setSound(null, null)
                    .setShowBadge(false)
                    .build()
            )
        )
    }

    private fun sync(registrations: List<FeralPushRegistration>) {
        if (registrations.isEmpty()) {
            Timber.tag(loggerTag.value).i("No registration left, stopping")
            stopSelf()
            return
        }
        synchronized(connections) {
            val wanted = registrations.associateBy { it.session }
            // Drop sessions that are gone, or whose topic / secret changed (the cursor may change freely).
            connections.entries.toList().forEach { (sessionId, running) ->
                val target = wanted[sessionId]
                if (target == null || !running.registration.sameConnectionAs(target)) {
                    Timber.tag(loggerTag.value).i("Closing the connection of $sessionId")
                    running.job.cancel()
                    connections.remove(sessionId)
                    monitor.setConnected(sessionId, false)
                }
            }
            wanted.values.filter { it.session !in connections }.forEach { registration ->
                Timber.tag(loggerTag.value).i("Opening the connection of ${registration.session}")
                val connection = connectionFactory.create(
                    registration = registration,
                    onConnectedChanged = { connected -> monitor.setConnected(registration.session, connected) },
                )
                val job = scope.launch { connection.run() }
                connections[registration.session] = RunningConnection(registration, connection, job)
            }
        }
    }

    private fun FeralPushRegistration.sameConnectionAs(other: FeralPushRegistration): Boolean {
        return topic == other.topic && clientSecret == other.clientSecret
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService<ConnectivityManager>() ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.tag(loggerTag.value).d("Network available")
                synchronized(connections) { connections.values.forEach { it.connection.onNetworkAvailable() } }
            }
        }
        runCatchingExceptions { connectivityManager.registerDefaultNetworkCallback(callback) }
            .onSuccess { networkCallback = callback }
            .onFailure { Timber.tag(loggerTag.value).w(it, "Unable to register the network callback") }
    }

    private fun unregisterNetworkCallback() {
        val callback = networkCallback ?: return
        networkCallback = null
        runCatchingExceptions { getSystemService<ConnectivityManager>()?.unregisterNetworkCallback(callback) }
    }
}
