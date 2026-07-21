/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.impl.PttSessionController
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.designsystem.utils.CommonDrawables
import timber.log.Timber

private val loggerTag = LoggerTag("PttSessionHostService")
private const val CHANNEL_ID = "ptt_session_host_channel"
private const val NOTIFICATION_ID = 90_210

/**
 * Foreground service that keeps a native PTT session alive across backgrounding and lock-screen.
 *
 * Unlike the Element Call host (which needs a live WebView window), the native transport is plain
 * audio, so this service just holds the process in the foreground (FOREGROUND_SERVICE_TYPE_MICROPHONE)
 * while the app-scoped [PttSessionController] owns the actual transport session. Start/stop drive the
 * controller; the microphone/floor are controlled elsewhere (talk button, hardware keys).
 */
class PttSessionHostService : Service() {
    companion object {
        private const val EXTRA_CONFIG = "EXTRA_PTT_CONFIG"
        private const val ACTION_HANGUP = "io.element.android.features.ptt.impl.services.PTT_HANGUP"

        /** Start the session host for [config], bringing the process into the foreground. */
        fun start(context: Context, config: PttChannelConfig) {
            val intent = Intent(context, PttSessionHostService::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        /** Leave the session and tear the service down. */
        fun hangup(context: Context) {
            val intent = Intent(context, PttSessionHostService::class.java).apply { action = ACTION_HANGUP }
            context.startService(intent)
        }
    }

    @Inject lateinit var sessionController: PttSessionController

    private var mediaButtonController: PttMediaButtonController? = null
    private var overlayButton: PttOverlayButton? = null

    // Retry the overlay button when the app returns to foreground — e.g. after the user grants the
    // "draw over other apps" permission in settings. show() is a no-op if it's already up or still
    // ungranted, so this just makes the grant take effect without leaving and rejoining.
    private val foregroundObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            overlayButton?.show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        bindings<PttSessionHostBindings>().inject(this)
        startAsForeground()
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HANGUP) {
            sessionController.stopSession()
            stopSelf()
            return START_NOT_STICKY
        }
        val config = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_CONFIG, PttChannelConfig::class.java) }
        if (config == null) {
            Timber.tag(loggerTag.value).w("No PTT config in start intent; stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        sessionController.startSession(config)
        // Catch Bluetooth / wired PTT-accessory buttons for the life of the session.
        if (mediaButtonController == null) {
            mediaButtonController = PttMediaButtonController(
                context = this,
                onKeyDown = { sessionController.pressToTalk() },
                onKeyUp = { sessionController.releaseToTalk() },
            )
        }
        // Floating on-screen PTT button, for transmit while backgrounded without an accessory.
        if (overlayButton == null) {
            overlayButton = PttOverlayButton(
                context = this,
                onPressStart = { sessionController.pressToTalk() },
                onPressEnd = { sessionController.releaseToTalk() },
            ).also { it.show() }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val notificationManager = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("Push-to-talk")
            .build()
        notificationManager.createNotificationChannel(channel)
        val hangupIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PttSessionHostService::class.java).apply { action = ACTION_HANGUP },
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle("Push-to-talk active")
            .setContentText("Connected to the PTT channel")
            .setOngoing(true)
            .addAction(0, "Leave", hangupIntent)
            .build()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }
        runCatchingExceptions {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to start PTT session host foreground service")
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
        mediaButtonController?.release()
        mediaButtonController = null
        overlayButton?.hide()
        overlayButton = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
