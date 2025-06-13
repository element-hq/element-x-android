/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.pip.PictureInPictureEvents
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.PictureInPictureStateProvider
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.features.call.impl.utils.InvalidAudioDeviceReason
import io.element.android.features.call.impl.utils.WebViewAudioManager
import io.element.android.features.call.impl.utils.WebViewPipController
import io.element.android.features.call.impl.utils.WebViewWidgetMessageInterceptor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

typealias RequestPermissionCallback = (Array<String>) -> Unit

interface CallScreenNavigator {
    fun close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenView(
    state: CallScreenState,
    pipState: PictureInPictureState,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun handleBack() {
        if (pipState.supportPip) {
            pipState.eventSink.invoke(PictureInPictureEvents.EnterPictureInPicture)
        } else {
            state.eventSink(CallScreenEvents.Hangup)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!pipState.isInPictureInPicture) {
                TopAppBar(
                    title = { Text(stringResource(R.string.element_call)) },
                    navigationIcon = {
                        BackButton(
                            imageVector = if (pipState.supportPip) CompoundIcons.ArrowLeft() else CompoundIcons.Close(),
                            onClick = ::handleBack,
                        )
                    }
                )
            }
        }
    ) { padding ->
        BackHandler {
            handleBack()
        }
        if (state.webViewError != null) {
            ErrorDialog(
                content = buildString {
                    append(stringResource(CommonStrings.error_unknown))
                    state.webViewError.takeIf { it.isNotEmpty() }?.let { append("\n\n").append(it) }
                },
                onSubmit = { state.eventSink(CallScreenEvents.Hangup) },
            )
        } else {
            var webViewAudioManager by remember { mutableStateOf<WebViewAudioManager?>(null) }
            val coroutineScope = rememberCoroutineScope()

            var invalidAudioDeviceReason by remember { mutableStateOf<InvalidAudioDeviceReason?>(null) }
            invalidAudioDeviceReason?.let {
                InvalidAudioDeviceDialog(invalidAudioDeviceReason = it) {
                    invalidAudioDeviceReason = null
                }
            }

            CallWebView(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize(),
                url = state.urlState,
                userAgent = state.userAgent,
                onPermissionsRequest = { request ->
                    val androidPermissions = mapWebkitPermissions(request.resources)
                    val callback: RequestPermissionCallback = { request.grant(it) }
                    requestPermissions(androidPermissions.toTypedArray(), callback)
                },
                onCreateWebView = { webView ->
                    val interceptor = WebViewWidgetMessageInterceptor(
                        webView = webView,
                        onUrlLoaded = { url ->
                            if (webViewAudioManager?.isInCallMode?.get() == false) {
                                Timber.d("URL $url is loaded, starting in-call audio mode")
                                webViewAudioManager?.onCallStarted()
                            } else {
                                Timber.d("Can't start in-call audio mode since the app is already in it.")
                            }
                        },
                        onError = { state.eventSink(CallScreenEvents.OnWebViewError(it)) },
                    )
                    webViewAudioManager = WebViewAudioManager(
                        webView = webView,
                        coroutineScope = coroutineScope,
                        onInvalidAudioDeviceAdded = { invalidAudioDeviceReason = it },
                    )
                    state.eventSink(CallScreenEvents.SetupMessageChannels(interceptor))
                    val pipController = WebViewPipController(webView)
                    pipState.eventSink(PictureInPictureEvents.SetPipController(pipController))
                },
                onDestroyWebView = {
                    // Reset audio mode
                    webViewAudioManager?.onCallStopped()
                }
            )
            when (state.urlState) {
                AsyncData.Uninitialized,
                is AsyncData.Loading ->
                    ProgressDialog(text = stringResource(id = CommonStrings.common_please_wait))
                is AsyncData.Failure -> {
                    Timber.e(state.urlState.error, "WebView failed to load URL: ${state.urlState.error.message}")
                    ErrorDialog(
                        content = state.urlState.error.message.orEmpty(),
                        onSubmit = { state.eventSink(CallScreenEvents.Hangup) },
                    )
                }
                is AsyncData.Success -> Unit
            }
        }
    }
}

@Composable
private fun InvalidAudioDeviceDialog(
    invalidAudioDeviceReason: InvalidAudioDeviceReason,
    onDismiss: () -> Unit,
) {
    ErrorDialog(
        content = when (invalidAudioDeviceReason) {
            InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED -> {
                stringResource(R.string.call_invalid_audio_device_bluetooth_devices_disabled)
            }
        },
        onSubmit = onDismiss,
    )
}

@Composable
private fun CallWebView(
    url: AsyncData<String>,
    userAgent: String,
    onPermissionsRequest: (PermissionRequest) -> Unit,
    onCreateWebView: (WebView) -> Unit,
    onDestroyWebView: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("WebView - can't be previewed")
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    onCreateWebView(this)
                    setup(userAgent, onPermissionsRequest)
                }
            },
            update = { webView ->
                if (url is AsyncData.Success && webView.url != url.data) {
                    webView.loadUrl(url.data)
                }
            },
            onRelease = { webView ->
                onDestroyWebView(webView)
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(settings) {
        javaScriptEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        @Suppress("DEPRECATION")
        databaseEnabled = true
        loadsImagesAutomatically = true
        userAgentString = userAgent
    }

    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            onPermissionsRequested(request)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            val priority = when (consoleMessage.messageLevel()) {
                ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
                ConsoleMessage.MessageLevel.WARNING -> Log.WARN
                else -> Log.DEBUG
            }

            val message = buildString {
                append(consoleMessage.sourceId())
                append(":")
                append(consoleMessage.lineNumber())
                append(" ")
                append(consoleMessage.message())
            }

            if (message.contains("password=")) {
                // Avoid logging any messages that contain "password" to prevent leaking sensitive information
                return true
            }

            Timber.tag("WebView").log(
                priority = priority,
                message = buildString {
                    append(consoleMessage.sourceId())
                    append(":")
                    append(consoleMessage.lineNumber())
                    append(" ")
                    append(consoleMessage.message())
                },
            )
            return true
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CallScreenViewPreview(
    @PreviewParameter(CallScreenStateProvider::class) state: CallScreenState,
) = ElementPreview {
    CallScreenView(
        state = state,
        pipState = aPictureInPictureState(),
        requestPermissions = { _, _ -> },
    )
}

@PreviewsDayNight
@Composable
internal fun CallScreenPipViewPreview(
    @PreviewParameter(PictureInPictureStateProvider::class) state: PictureInPictureState,
) = ElementPreview {
    CallScreenView(
        state = aCallScreenState(),
        pipState = state,
        requestPermissions = { _, _ -> },
    )
}

@PreviewsDayNight
@Composable
internal fun InvalidAudioDeviceDialogPreview() = ElementPreview {
    InvalidAudioDeviceDialog(invalidAudioDeviceReason = InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED) {}
}
