/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.webkit.PermissionRequest
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.zacsweers.metro.Inject
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.call.api.CallData
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.di.PttCallBindings
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.ui.CallScreenEvent
import io.element.android.features.call.impl.ui.CallScreenNavigator
import io.element.android.features.call.impl.ui.CallScreenPresenter
import io.element.android.features.call.impl.ui.CallScreenView
import io.element.android.features.call.impl.ui.RequestPermissionCallback
import io.element.android.libraries.androidutils.browser.ConsoleMessageLogger
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import timber.log.Timber

private val loggerTag = LoggerTag("OverlayPttCallService")
private const val CHANNEL_ID = "call_foreground_service_channel"

/**
 * Headless host for a Push-to-Talk session: a foreground service that mounts the Element Call
 * [CallScreenView] (WebView / RTC) into an invisible 1x1 [WindowManager] overlay. Because the
 * WebView stays attached to a live window (not a backgrounded/stopped activity), the WebRTC audio
 * keeps flowing while the user is in the room or the screen is locked.
 *
 * Requires the SYSTEM_ALERT_WINDOW permission. Reuses the Element Call machinery;
 * [io.element.android.features.call.impl.ui.ElementCallActivity] is left untouched.
 */
class OverlayPttCallService :
    Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner,
    CallScreenNavigator {
    companion object {
        const val EXTRA_CALL_TYPE = "EXTRA_PTT_CALL_TYPE"
        private const val ACTION_HANGUP = "io.element.android.features.call.impl.services.PTT_HANGUP"

        fun start(context: Context, callData: CallData) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Timber.tag(loggerTag.value).w("Microphone permission not granted, cannot start PTT session")
                return
            }
            val intent = Intent(context, OverlayPttCallService::class.java).apply {
                putExtra(EXTRA_CALL_TYPE, callData)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        /** Cleanly leave the session: signal Element Call to hang up, then the service tears down. */
        fun hangup(context: Context) {
            val intent = Intent(context, OverlayPttCallService::class.java).apply { action = ACTION_HANGUP }
            context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayPttCallService::class.java))
        }
    }

    @Inject lateinit var presenterFactory: CallScreenPresenter.Factory
    @Inject lateinit var appPreferencesStore: AppPreferencesStore
    @Inject lateinit var featureFlagService: FeatureFlagService
    @Inject lateinit var buildMeta: BuildMeta
    @Inject lateinit var audioFocus: AudioFocus
    @Inject lateinit var consoleMessageLogger: ConsoleMessageLogger

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    // CallScreenView uses BackHandler, which needs an OnBackPressedDispatcherOwner (an Activity
    // provides one; a service-hosted ComposeView must supply it). Back is a no-op for the overlay.
    override val onBackPressedDispatcher = OnBackPressedDispatcher()

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var callEventSink: ((CallScreenEvent) -> Unit)? = null

    // Headless: never show PiP, the WebView lives in an invisible overlay.
    private val disabledPipState = PictureInPictureState(
        supportPip = false,
        isInPictureInPicture = false,
        eventSink = {},
    )

    override fun onCreate() {
        super.onCreate()
        bindings<PttCallBindings>().inject(this)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HANGUP) {
            // Ask Element Call to leave the RTC session; the presenter then calls close() -> stopSelf().
            val sink = callEventSink
            if (sink != null) {
                sink(CallScreenEvent.Hangup)
            } else {
                stopSelf()
            }
            return START_NOT_STICKY
        }
        val callData = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_CALL_TYPE, CallData::class.java) }
        if (callData == null) {
            Timber.tag(loggerTag.value).w("No call data, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        if (overlayView == null) {
            mountOverlay(callData)
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val notificationManager = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(getString(R.string.call_foreground_service_channel_title_android).ifEmpty { "Ongoing call" })
            .build()
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(R.string.call_foreground_service_title_android))
            .setContentText(getString(R.string.call_foreground_service_message_android))
            .build()
        val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.ONGOING_CALL)
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        runCatchingExceptions {
            ServiceCompat.startForeground(this, notificationId, notification, serviceType)
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to start PTT foreground service")
        }
    }

    private fun mountOverlay(callData: CallData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !Settings.canDrawOverlays(this)) {
            Timber.tag(loggerTag.value).e("SYSTEM_ALERT_WINDOW not granted; cannot host the PTT overlay")
            stopSelf()
            return
        }
        audioFocus.requestAudioFocus(
            requester = AudioFocusRequester.ElementCall,
            onFocusLost = { Timber.tag(loggerTag.value).w("Audio focus lost") },
        )
        val presenter = presenterFactory.create(callData, this)
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayPttCallService)
            setViewTreeViewModelStoreOwner(this@OverlayPttCallService)
            setViewTreeSavedStateRegistryOwner(this@OverlayPttCallService)
            setViewTreeOnBackPressedDispatcherOwner(this@OverlayPttCallService)
            setContent {
                ElementThemeApp(
                    appPreferencesStore = appPreferencesStore,
                    featureFlagService = featureFlagService,
                    compoundLight = SemanticColorsLightDark.default.light,
                    compoundDark = SemanticColorsLightDark.default.dark,
                    buildMeta = buildMeta,
                ) {
                    val state = presenter.present()
                    SideEffect { callEventSink = state.eventSink }
                    CallScreenView(
                        state = state,
                        pipState = disabledPipState,
                        onConsoleMessage = { consoleMessageLogger.log("PttCall", it) },
                        requestPermissions = ::grantAlreadyGrantedPermissions,
                        // No Activity window token in a service overlay → Compose dialogs would crash.
                        hideDialogs = true,
                    )
                }
            }
        }
        overlayView = view
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            1,
            1,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
        runCatchingExceptions {
            windowManager.addView(view, params)
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to add PTT overlay window")
            stopSelf()
        }
    }

    // The WebView requests mic/cam; from a service we can only grant permissions already granted at
    // the OS level (RECORD_AUDIO for PTT). Interactive requests aren't possible from a service.
    private fun grantAlreadyGrantedPermissions(androidPermissions: Array<String>, callback: RequestPermissionCallback) {
        val webKitPermissions = androidPermissions.mapNotNull { permission ->
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                null
            } else {
                when (permission) {
                    Manifest.permission.RECORD_AUDIO -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
                    Manifest.permission.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                    else -> null
                }
            }
        }
        callback(webKitPermissions.toTypedArray())
    }

    override fun close() {
        stopSelf()
    }

    override fun onDestroy() {
        callEventSink = null
        overlayView?.let { view -> runCatchingExceptions { windowManager.removeView(view) } }
        overlayView = null
        audioFocus.releaseAudioFocus()
        viewModelStore.clear()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
