/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.IntentCompat
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import dev.zacsweers.metro.Inject
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.CallType.ExternalUrl
import io.element.android.features.call.impl.DefaultElementCallEntryPoint
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.pip.PictureInPictureEvents
import io.element.android.features.call.impl.pip.PictureInPicturePresenter
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.PipView
import io.element.android.features.call.impl.services.CallForegroundService
import io.element.android.features.call.impl.utils.CallIntentDataParser
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.androidutils.browser.ConsoleMessageLogger
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import timber.log.Timber

private val loggerTag = LoggerTag("ElementCallActivity")

class ElementCallActivity :
    AppCompatActivity(),
    CallScreenNavigator,
    PipView {
    @Inject lateinit var callIntentDataParser: CallIntentDataParser
    @Inject lateinit var presenterFactory: CallScreenPresenter.Factory
    @Inject lateinit var appPreferencesStore: AppPreferencesStore
    @Inject lateinit var enterpriseService: EnterpriseService
    @Inject lateinit var pictureInPicturePresenter: PictureInPicturePresenter
    @Inject lateinit var buildMeta: BuildMeta
    @Inject lateinit var audioFocus: AudioFocus
    @Inject lateinit var consoleMessageLogger: ConsoleMessageLogger

    private lateinit var presenter: Presenter<CallScreenState>

    private var requestPermissionCallback: RequestPermissionCallback? = null

    private val requestPermissionsLauncher = registerPermissionResultLauncher()

    private val webViewTarget = mutableStateOf<CallType?>(null)

    private var eventSink: ((CallScreenEvents) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings<CallBindings>().inject(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        setCallType(intent)
        // If presenter is not created at this point, it means we have no call to display, the Activity is finishing, so return early
        if (!::presenter.isInitialized) {
            return
        }

        pictureInPicturePresenter.setPipView(this)

        Timber.d("Created ElementCallActivity with call type: ${webViewTarget.value}")

        setContent {
            val pipState = pictureInPicturePresenter.present()
            ListenToAndroidEvents(pipState)
            val colors by remember(webViewTarget.value?.getSessionId()) {
                enterpriseService.semanticColorsFlow(sessionId = webViewTarget.value?.getSessionId())
            }.collectAsState(SemanticColorsLightDark.default)
            ElementThemeApp(
                appPreferencesStore = appPreferencesStore,
                compoundLight = colors.light,
                compoundDark = colors.dark,
                buildMeta = buildMeta,
            ) {
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
                    onConsoleMessage = {
                        consoleMessageLogger.log("ElementCall", it)
                    },
                    requestPermissions = { permissions, callback ->
                        requestPermissionCallback = callback
                        requestPermissionsLauncher.launch(permissions)
                    }
                )
            }
        }
    }

    private fun setCallIsActive() {
        audioFocus.requestAudioFocus(
            requester = AudioFocusRequester.ElementCall,
            onFocusLost = {
                // If the audio focus is lost, we do not stop the call.
                Timber.tag(loggerTag.value).w("Audio focus lost")
            }
        )
        CallForegroundService.start(this)
    }

    @Composable
    private fun ListenToAndroidEvents(pipState: PictureInPictureState) {
        val pipEventSink by rememberUpdatedState(pipState.eventSink)
        DisposableEffect(Unit) {
            val listener = Runnable {
                if (requestPermissionCallback != null) {
                    Timber.tag(loggerTag.value).w("Ignoring onUserLeaveHint event because user is asked to grant permissions")
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
                    Timber.tag(loggerTag.value).d("Exiting PiP mode: Hangup the call")
                    eventSink?.invoke(CallScreenEvents.Hangup)
                }
            }
            addOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            onDispose {
                removeOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setCallType(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioFocus.releaseAudioFocus()
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
            IntentCompat.getParcelableExtra(intent, DefaultElementCallEntryPoint.EXTRA_CALL_TYPE, CallType::class.java)
                ?: intent.dataString?.let(::parseUrl)?.let(::ExternalUrl)
        }
        val currentCallType = webViewTarget.value
        if (currentCallType == null) {
            if (callType == null) {
                Timber.tag(loggerTag.value).d("Re-opened the activity but we have no url to load or a cached one, finish the activity")
                finish()
            } else {
                Timber.tag(loggerTag.value).d("Set the call type and create the presenter")
                webViewTarget.value = callType
                presenter = presenterFactory.create(callType, this)
            }
        } else {
            if (callType == null) {
                Timber.tag(loggerTag.value).d("Coming back from notification, do nothing")
            } else if (callType != currentCallType) {
                Timber.tag(loggerTag.value).d("User starts another call, restart the Activity")
                setIntent(intent)
                recreate()
            } else {
                // Starting the same call again, should not happen, the UI is preventing this. But maybe when using external links.
                Timber.tag(loggerTag.value).d("Starting the same call again, do nothing")
            }
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
