/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WidgetMessage(
    @SerialName("api") val direction: Direction,
    @SerialName("widgetId") val widgetId: String,
    @SerialName("requestId") val requestId: String,
    @SerialName("action") val action: Action,
    @SerialName("data") val data: JsonElement? = null,
) {
    @Serializable
    enum class Direction {
        @SerialName("fromWidget")
        FromWidget,

        @SerialName("toWidget")
        ToWidget
    }

    @Serializable
    enum class Action {
        @SerialName("im.vector.hangup")
        HangUp,

        @SerialName("io.element.close")
        Close,

        @SerialName("send_event")
        SendEvent,
    }
}
