/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import org.matrix.rustcomponents.sdk.ClientProperties
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.WidgetSettings
import org.matrix.rustcomponents.sdk.generateWebviewUrl

fun MatrixWidgetSettings.toRustWidgetSettings() = WidgetSettings(
    widgetId = this.id,
    initAfterContentLoad = this.initAfterContentLoad,
    rawUrl = this.rawUrl,
)

fun MatrixWidgetSettings.Companion.fromRustWidgetSettings(widgetSettings: WidgetSettings) = MatrixWidgetSettings(
    id = widgetSettings.widgetId,
    initAfterContentLoad = widgetSettings.initAfterContentLoad,
    rawUrl = widgetSettings.rawUrl,
)

suspend fun MatrixWidgetSettings.generateWidgetWebViewUrl(
    room: RoomInterface,
    clientId: String,
    languageTag: String? = null,
    theme: String? = null
) = generateWebviewUrl(
    widgetSettings = this.toRustWidgetSettings(),
    room = room as Room,
    props = ClientProperties(
        clientId = clientId,
        languageTag = languageTag,
        theme = theme,
    )
)
