/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.webkit.WebView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WebViewPipController(
    private val webView: WebView,
) : PipController {
    override suspend fun canEnterPip(): Boolean {
        return suspendCoroutine { continuation ->
            webView.evaluateJavascript("controls.canEnterPip()") { result ->
                // Note if the method is not available, it will return "null"
                continuation.resume(result == "true" || result == "null")
            }
        }
    }

    override fun enterPip() {
        webView.evaluateJavascript("controls.enablePip()", null)
    }

    override fun exitPip() {
        webView.evaluateJavascript("controls.disablePip()", null)
    }
}
