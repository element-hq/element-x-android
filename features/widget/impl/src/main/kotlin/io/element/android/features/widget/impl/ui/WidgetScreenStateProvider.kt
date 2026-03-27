/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class WidgetScreenStateProvider : PreviewParameterProvider<WidgetScreenState> {
    override val values: Sequence<WidgetScreenState>
        get() = sequenceOf(
            aWidgetScreenState(),
            aWidgetScreenState(urlState = AsyncData.Loading()),
            aWidgetScreenState(urlState = AsyncData.Failure(Exception("An error occurred"))),
            aWidgetScreenState(webViewError = "Error details from WebView"),
        )
}

internal fun aWidgetScreenState(
    urlState: AsyncData<String> = AsyncData.Success("https://widget.element.io/some-widget?with=parameters"),
    webViewError: String? = null,
    userAgent: String = "",
    isWidgetLoaded: Boolean = true,
    isInWidgetMode: Boolean = false,
    widgetName: String = "Widget",
    eventSink: (WidgetScreenEvents) -> Unit = {},
): WidgetScreenState {
    return WidgetScreenState(
        urlState = urlState,
        webViewError = webViewError,
        userAgent = userAgent,
        isWidgetLoaded = isWidgetLoaded,
        isInWidgetMode = isInWidgetMode,
        widgetName = widgetName,
        eventSink = eventSink,
    )
}

