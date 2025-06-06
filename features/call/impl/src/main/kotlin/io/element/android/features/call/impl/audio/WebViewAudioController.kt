/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.media.AudioDeviceInfo
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * See [Element Call controls docs.](https://github.com/element-hq/element-call/blob/livekit/docs/controls.md#audio-devices)
 */
class WebViewAudioController(
    private val coroutineScope: CoroutineScope,
    private val webView: WebView,
) {
    private val jsonSerializer = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    fun init(webViewAudioDeviceSelectedCallback: AndroidWebViewAudioBridge) {
        Timber.d("Setting androidNativeBridge javascript interface in webview")
        webView.addJavascriptInterface(webViewAudioDeviceSelectedCallback, "androidNativeBridge")
    }

    /**
     * Assigns the callback in the WebView to be called when the user selects an audio device.
     *
     * It should be called with some delay after [init].
     */
    fun setWebViewAndroidNativeBridge() {
        Timber.d("Adding callback in controls.onAudioPlaybackStarted")
        webView.evaluateJavascript("controls.onAudioPlaybackStarted = () => { console.log('SET TRACK READY SET'); androidNativeBridge.onTrackReady(); };", null)
        Timber.d("Adding callback in controls.onOutputDeviceSelect")
        webView.evaluateJavascript("controls.onOutputDeviceSelect = (id) => { androidNativeBridge.setOutputDevice(id); };", null)
    }

    /**
     * Sets the available audio devices in the WebView.
     *
     * @param devices The list of audio devices to set. If not provided, it will use the current list of audio devices.
     */
    fun setAvailableAudioDevices(
        devices: List<AudioDeviceInfo>,
    ) {
        val mappedDevice = devices.map(SerializableAudioDevice::fromAudioDeviceInfo)
        Timber.d("Updating available audio devices")
        val deviceList = jsonSerializer.encodeToString(mappedDevice)
        coroutineScope.launch(Dispatchers.Main) {
            webView.evaluateJavascript("controls.setAvailableOutputDevices($deviceList);", {
                Timber.d("Audio: setAvailableOutputDevices result: $it")
            })
        }
    }

    /**
     * Updates the WebView's UI to reflect the selected audio device.
     *
     * @param deviceId The id of the selected audio device.
     */
    fun updateSelectedAudioDevice(deviceId: String) {
        coroutineScope.launch(Dispatchers.Main) {
            webView.evaluateJavascript("controls.setOutputDevice('$deviceId');", null)
        }
    }
}
