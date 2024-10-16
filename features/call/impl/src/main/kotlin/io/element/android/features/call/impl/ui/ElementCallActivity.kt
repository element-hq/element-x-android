/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.webkit.PermissionRequest
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.IntentCompat
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.DefaultElementCallEntryPoint
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.pip.PictureInPictureEvents
import io.element.android.features.call.impl.pip.PictureInPicturePresenter
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.PipView
import io.element.android.features.call.impl.services.CallForegroundService
import io.element.android.features.call.impl.utils.CallIntentDataParser
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import timber.log.Timber
import javax.inject.Inject

class ElementCallActivity :
    AppCompatActivity(),
    CallScreenNavigator,
    PipView {
    @Inject lateinit var callIntentDataParser: CallIntentDataParser
    @Inject lateinit var presenterFactory: CallScreenPresenter.Factory
    @Inject lateinit var appPreferencesStore: AppPreferencesStore
    @Inject lateinit var pictureInPicturePresenter: PictureInPicturePresenter

    private lateinit var presenter: CallScreenPresenter

    private lateinit var audioManager: AudioManager

    private var requestPermissionCallback: RequestPermissionCallback? = null

    private var audiofocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private val requestPermissionsLauncher = registerPermissionResultLauncher()

    private var isDarkMode = false
    private val webViewTarget = mutableStateOf<CallType?>(null)

    private var eventSink: ((CallScreenEvents) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationContext.bindings<CallBindings>().inject(this)

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setCallType(intent)

        if (savedInstanceState == null) {
            updateUiMode(resources.configuration)
        }

        pictureInPicturePresenter.setPipView(this)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        setContent {
            val pipState = pictureInPicturePresenter.present()
            ListenToAndroidEvents(pipState)
            ElementThemeApp(appPreferencesStore) {
                val state = presenter.present()
                eventSink = state.eventSink
                LaunchedEffect(state.isCallActive, state.isInWidgetMode) {
                    // Note when not in WidgetMode, isCallActive will never be true, so consider the call is active
                    if (state.isCallActive || !state.isInWidgetMode) {
                        setCallIsActive()
                    }
                }
                CallScreenView(
                    state = state,
                    pipState = pipState,
                    requestPermissions = { permissions, callback ->
                        requestPermissionCallback = callback
                        requestPermissionsLauncher.launch(permissions)
                    }
                )
            }
        }
    }

    private fun setCallIsActive() {
        requestAudioFocus()
        CallForegroundService.start(this)
    }

    @Composable
    private fun ListenToAndroidEvents(pipState: PictureInPictureState) {
        val pipEventSink by rememberUpdatedState(pipState.eventSink)
        DisposableEffect(Unit) {
            val listener = Runnable {
                if (requestPermissionCallback != null) {
                    Timber.w("Ignoring onUserLeaveHint event because user is asked to grant permissions")
                } else {
                    pipEventSink(PictureInPictureEvents.EnterPictureInPicture)
                }
            }
            addOnUserLeaveHintListener(listener)
            onDispose {
                removeOnUserLeaveHintListener(listener)
            }
        }
        DisposableEffect(Unit) {
            val onPictureInPictureModeChangedListener = Consumer { _: PictureInPictureModeChangedInfo ->
                pipEventSink(PictureInPictureEvents.OnPictureInPictureModeChanged(isInPictureInPictureMode))
                if (!isInPictureInPictureMode && !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    Timber.d("Exiting PiP mode: Hangup the call")
                    eventSink?.invoke(CallScreenEvents.Hangup)
                }
            }
            addOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            onDispose {
                removeOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUiMode(newConfig)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setCallType(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAudioFocus()
        CallForegroundService.stop(this)
        pictureInPicturePresenter.setPipView(null)
    }

    override fun finish() {
        // Also remove the task from recents
        finishAndRemoveTask()
    }

    override fun close() {
        finish()
    }

    private fun setCallType(intent: Intent?) {
        val callType = intent?.let {
            IntentCompat.getParcelableExtra(it, DefaultElementCallEntryPoint.EXTRA_CALL_TYPE, CallType::class.java)
        }
        val intentUrl = intent?.dataString?.let(::parseUrl)
        when {
            // Re-opened the activity but we have no url to load or a cached one, finish the activity
            intent?.dataString == null && callType == null && webViewTarget.value == null -> finish()
            callType != null -> {
                webViewTarget.value = callType
                presenter = presenterFactory.create(callType, this)
            }
            intentUrl != null -> {
                val fallbackInputs = CallType.ExternalUrl(intentUrl)
                webViewTarget.value = fallbackInputs
                presenter = presenterFactory.create(fallbackInputs, this)
            }
            // Coming back from notification, do nothing
            else -> return
        }
    }

    private fun parseUrl(url: String?): String? = callIntentDataParser.parse(url)

    private fun registerPermissionResultLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val callback = requestPermissionCallback ?: return@registerForActivityResult
            val permissionsToGrant = mutableListOf<String>()
            permissions.forEach { (permission, granted) ->
                if (granted) {
                    val webKitPermission = when (permission) {
                        Manifest.permission.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        Manifest.permission.RECORD_AUDIO -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        else -> return@forEach
                    }
                    permissionsToGrant.add(webKitPermission)
                }
            }
            callback(permissionsToGrant.toTypedArray())
            requestPermissionCallback = null
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .build()
            audioManager.requestAudioFocus(request)
            audiofocusRequest = request
        } else {
            val listener = AudioManager.OnAudioFocusChangeListener { }
            audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
            )
            audioFocusChangeListener = listener
        }
    }

    @Suppress("DEPRECATION")
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audiofocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioFocusChangeListener?.let { audioManager.abandonAudioFocus(it) }
        }
    }

    private fun updateUiMode(configuration: Configuration) {
        val prevDarkMode = isDarkMode
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_YES
        isDarkMode = currentNightMode != 0
        if (prevDarkMode != isDarkMode) {
            if (isDarkMode) {
                window.setBackgroundDrawableResource(android.R.drawable.screen_background_dark)
            } else {
                window.setBackgroundDrawableResource(android.R.drawable.screen_background_light)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setPipParams() {
        setPictureInPictureParams(getPictureInPictureParams())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun enterPipMode(): Boolean {
        return if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            enterPictureInPictureMode(getPictureInPictureParams())
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPictureInPictureParams(): PictureInPictureParams {
        return PictureInPictureParams.Builder()
            // Portrait for calls seems more appropriate
            .setAspectRatio(Rational(3, 5))
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }
            .build()
    }

    override fun hangUp() {
        eventSink?.invoke(CallScreenEvents.Hangup)
    }
}

internal fun mapWebkitPermissions(permissions: Array<String>): List<String> {
    return permissions.mapNotNull { permission ->
        when (permission) {
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
            else -> null
        }
    }
}
