/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.content.res.Configuration
import android.webkit.JavascriptInterface
import android.webkit.WebView
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Documentation about the `controls` command can be found here:
 * https://github.com/element-hq/element-call/blob/livekit/docs/controls.md#picture-in-picture
 */
class WebViewPipController(
    private val webView: WebView,
    updatePipOrientation: (orientation: Int?) -> Unit,
) : PipController {
    init {
        // Register the JavaScript interface ahead of time to ensure it's available when the WebView content is loaded and tries to call it
        webView.addJavascriptInterface(PipBridge(updatePipOrientation), "androidPipBridge")
    }

    override suspend fun canEnterPip(): Boolean {
        return suspendCoroutine { continuation ->
            webView.evaluateJavascript("controls.canEnterPip()") { result ->
                // Note if the method is not available, it will return "null"
                continuation.resume(result == "true" || result == "null")
            }
        }
    }

    override fun enterPip() {
        Timber.d("Adding callback in controls.onPipMediaOrientationUpdate")
        webView.evaluateJavascript("controls.onPipMediaOrientationUpdate = (orientation) => { androidPipBridge.setPipOrientation(orientation); };", null)

        webView.evaluateJavascript("controls.enablePip()", null)
    }

    override fun exitPip() {
        webView.evaluateJavascript("controls.disablePip()", null)
    }

}

private class PipBridge(
    private val onOrientationChange: (orientation: Int?) -> Unit,
) {
    @JavascriptInterface
    fun setPipOrientation(orientation: String?) {
        Timber.d("Picture-in-picture orientation changed in webview, orientation: $orientation")
        // This callback is used to update the picture-in-picture orientation in the WebView when it changes
        val orientation = when (orientation) {
            "portrait" -> Configuration.ORIENTATION_PORTRAIT
            "landscape" -> Configuration.ORIENTATION_LANDSCAPE
            else -> return
        }
        onOrientationChange(orientation)
    }
}
