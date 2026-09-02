/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.services

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import io.element.android.features.ptt.impl.input.PttInputCoordinator
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
 * audio, so this service just holds the process in the foreground (as a special-use FGS while warm,
 * elevated to the microphone type once RECORD_AUDIO is granted — see [desiredServiceType])
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
    @Inject lateinit var inputCoordinator: PttInputCoordinator

    private var overlayButton: PttOverlayButton? = null

    // Retry the overlay button when the app returns to foreground — e.g. after the user grants the
    // "draw over other apps" permission in settings. show() is a no-op if it's already up or still
    // ungranted, so this just makes the grant take effect without leaving and rejoining.
    private val foregroundObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            overlayButton?.show()
            // The user may have just granted RECORD_AUDIO (e.g. from the transmit prompt or settings);
            // re-apply the foreground type so a warm special-use session is elevated to microphone.
            startAsForeground()
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
        // Catch hardware/accessory PTT buttons (media-key accessories today; OEM/BLE sources plug in
        // via the same coordinator) for the life of the session.
        inputCoordinator.start()
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
        runCatchingExceptions {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, desiredServiceType())
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to start PTT session host foreground service")
        }
    }

    /**
     * The foreground-service type for the current state. A warm connection (connected/listening, not
     * transmitting) does not use the mic, so on Android 14+ it runs as `specialUse` — which, unlike the
     * `microphone` type, does not require RECORD_AUDIO to start. Once RECORD_AUDIO is granted the service
     * uses the `microphone` type so transmitting works while backgrounded. Re-evaluated on ON_RESUME.
     */
    private fun desiredServiceType(): Int {
        val micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> 0
            micGranted -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            // Android 11–13: the microphone type can be declared without RECORD_AUDIO granted (the
            // mic access itself is still gated at capture time), so no special-use fallback is needed.
            else -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
        inputCoordinator.stop()
        overlayButton?.hide()
        overlayButton = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
