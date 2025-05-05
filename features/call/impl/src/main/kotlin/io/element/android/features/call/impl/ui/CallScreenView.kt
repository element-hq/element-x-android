/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioManager.OnCommunicationDeviceChangedListener
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.pip.PictureInPictureEvents
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.PictureInPictureStateProvider
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.features.call.impl.utils.WebViewPipController
import io.element.android.features.call.impl.utils.WebViewWidgetMessageInterceptor
import io.element.android.libraries.androidutils.compat.disableExternalAudioDevice
import io.element.android.libraries.androidutils.compat.enableExternalAudioDevice
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber
import java.util.concurrent.Executors

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
                    title = {},
                    navigationIcon = {
                        BackButton(
                            imageVector = if (pipState.supportPip) CompoundIcons.ArrowLeft() else CompoundIcons.Close(),
                            onClick = ::handleBack,
                        )
                    },
                    actions = {
                        AudioDeviceSelector()
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
                onWebViewCreate = { webView ->
                    val interceptor = WebViewWidgetMessageInterceptor(
                        webView = webView,
                        onError = { state.eventSink(CallScreenEvents.OnWebViewError(it)) },
                    )
                    state.eventSink(CallScreenEvents.SetupMessageChannels(interceptor))
                    val pipController = WebViewPipController(webView)
                    pipState.eventSink(PictureInPictureEvents.SetPipController(pipController))
                }
            )
            when (state.urlState) {
                AsyncData.Uninitialized,
                is AsyncData.Loading ->
                    ProgressDialog(text = stringResource(id = CommonStrings.common_please_wait))
                is AsyncData.Failure ->
                    ErrorDialog(
                        content = state.urlState.error.message.orEmpty(),
                        onSubmit = { state.eventSink(CallScreenEvents.Hangup) },
                    )
                is AsyncData.Success -> Unit
            }
        }
    }
}

@Composable
private fun CallWebView(
    url: AsyncData<String>,
    userAgent: String,
    onPermissionsRequest: (PermissionRequest) -> Unit,
    onWebViewCreate: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("WebView - can't be previewed")
        }
    } else {
        Column(modifier = modifier) {
            var audioDeviceCallback: AudioDeviceCallback? by remember { mutableStateOf(null) }

            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    audioDeviceCallback = context.setupAudioConfiguration()
                    WebView(context).apply {
                        onWebViewCreate(this)
                        setup(userAgent, onPermissionsRequest)
                    }
                },
                update = { webView ->
                    if (url is AsyncData.Success && webView.url != url.data) {
                        webView.loadUrl(url.data)
                    }
                },
                onRelease = { webView ->
                    // Reset audio mode
                    webView.context.releaseAudioConfiguration(audioDeviceCallback)
                    webView.destroy()
                }
            )
        }
    }
}

@Composable
private fun AudioDeviceSelector(
    modifier: Modifier = Modifier,
) {
    // For now don't display the audio device selector in unsupported Android versions
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService<AudioManager>() }

    val audioDevices = remember { mutableStateListOf<AudioDeviceInfo>() }
    var expanded by remember { mutableStateOf(false) }
    val isInEditMode = LocalInspectionMode.current
    var selected by remember(audioDevices) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isInEditMode) {
            audioManager?.communicationDevice
        } else {
            null
        }
        mutableStateOf(device)
    }

    if (!LocalInspectionMode.current) {
        DisposableEffect(Unit) {
            audioDevices.addAll(audioManager?.loadCommunicationAudioDevices().orEmpty())

            val onCommunicationDeviceChangedListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                OnCommunicationDeviceChangedListener { selected = audioManager?.communicationDevice }
                    .also { audioManager?.addOnCommunicationDeviceChangedListener(Executors.newSingleThreadExecutor(), it) }
            } else {
                null
            }

            val audioDeviceCallback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    audioDevices.clear()
                    audioDevices.addAll(audioManager?.loadCommunicationAudioDevices().orEmpty())
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    audioDevices.clear()
                    audioDevices.addAll(audioManager?.loadCommunicationAudioDevices().orEmpty())
                }
            }
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, null)

            onDispose {
                audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    onCommunicationDeviceChangedListener?.let { audioManager?.removeOnCommunicationDeviceChangedListener(it) }
                }
            }
        }
    }

    Box(modifier.padding(horizontal = 16.dp)) {
        TextButton(
            text = "Audio device",
            onClick = {
                expanded = !expanded
            },
            leadingIcon = IconSource.Vector(CompoundIcons.ChevronDown()),
        )

        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            for (device in audioDevices) {
                DropdownMenuItem(text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(device.description())
                        if (selected == device) {
                            Icon(imageVector = CompoundIcons.Check(), contentDescription = null)
                        }
                    }
                }, onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Workaround for Android 12, otherwise changing the audio device doesn't work
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
                            audioManager?.mode = AudioManager.MODE_NORMAL
                        }
                        audioManager?.setCommunicationDevice(device)
                        selected = device
                        expanded = false
                    } else {
                        when (device.type) {
                            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                                audioManager?.isSpeakerphoneOn = true
                                selected = device
                            }
                            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                                audioManager?.isSpeakerphoneOn = false
                                selected = device
                            }
                            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                                audioManager?.isBluetoothScoOn = true
                                selected = device
                            }
                            AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_USB_HEADSET -> {
                                Timber.d("Audio device selected but it's not compatible, type: ${device.type}")
                                // TODO use MediaRouter maybe?

                            }
                            else -> {
                                Timber.d("Audio device selected but it's not compatible, type: ${device.type}")
                            }
                        }
                        expanded = false
                    }
                })
            }
        }
    }
}

private fun AudioManager.loadCommunicationAudioDevices(): List<AudioDeviceInfo> {
    val wantedDeviceTypes = listOf(
        // Paired bluetooth device with microphone
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        // USB devices which can play or record audio
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
        // Wired audio devices
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        // The built-in speaker of the device
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
        // The built-in earpiece of the device
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
    )
    val devices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        availableCommunicationDevices
    } else {
        getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }
    return devices.filter { device ->
        wantedDeviceTypes.contains(device.type)
    }
}

fun AudioDeviceInfo.description(): String {
    val type = when (type) {
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth"
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB accessory"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in speaker"
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Built-in earpiece"
        else -> "Unknown device type: $type"
    }
    return if (isBuiltIn()) {
        type
    } else {
        val name = if (productName.length > 10) {
            productName.substring(0, 10) + "…"
        } else {
            productName
        }
        "$name - $type"
    }
}

private fun AudioDeviceInfo.isBuiltIn(): Boolean = when (type) {
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
    AudioDeviceInfo.TYPE_BUILTIN_MIC,
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> true
    else -> false}

private fun Context.setupAudioConfiguration(): AudioDeviceCallback? {
    val audioManager = getSystemService<AudioManager>() ?: return null
    // Set 'voice call' mode so volume keys actually control the call volume
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }
    audioManager.enableExternalAudioDevice()
    return object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            Timber.d("Audio devices added")
            audioManager.enableExternalAudioDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            Timber.d("Audio devices removed")
            audioManager.enableExternalAudioDevice()
        }
    }.also {
        audioManager.registerAudioDeviceCallback(it, null)
    }
}

private fun Context.releaseAudioConfiguration(audioDeviceCallback: AudioDeviceCallback?) {
    val audioManager = getSystemService<AudioManager>() ?: return
    audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    audioManager.disableExternalAudioDevice()
    audioManager.mode = AudioManager.MODE_NORMAL
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
