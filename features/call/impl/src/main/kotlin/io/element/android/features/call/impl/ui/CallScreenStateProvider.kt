/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class CallScreenStateProvider : PreviewParameterProvider<CallScreenState> {
    override val values: Sequence<CallScreenState>
        get() = sequenceOf(
            aCallScreenState(),
            aCallScreenState(urlState = AsyncData.Loading()),
            aCallScreenState(urlState = AsyncData.Failure(Exception("An error occurred"))),
            aCallScreenState(webViewError = "Error details from WebView"),
        )
}

internal fun aCallScreenState(
    urlState: AsyncData<String> = AsyncData.Success("https://call.element.io/some-actual-call?with=parameters"),
    webViewError: String? = null,
    userAgent: String = "",
    isCallActive: Boolean = true,
    isInWidgetMode: Boolean = false,
    eventSink: (CallScreenEvents) -> Unit = {},
): CallScreenState {
    return CallScreenState(
        urlState = urlState,
        webViewError = webViewError,
        userAgent = userAgent,
        isCallActive = isCallActive,
        isInWidgetMode = isInWidgetMode,
        eventSink = eventSink,
    )
}
