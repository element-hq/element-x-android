/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.webkit.WebView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Documentation about the `controls` command can be found here:
 * https://github.com/element-hq/element-call/blob/livekit/docs/controls.md#picture-in-picture
 */
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
