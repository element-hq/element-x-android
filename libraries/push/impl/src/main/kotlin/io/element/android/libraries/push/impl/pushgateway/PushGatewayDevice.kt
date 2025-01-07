/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushGatewayDevice(
    /**
     * Required. The app_id given when the pusher was created.
     */
    @SerialName("app_id")
    val appId: String,
    /**
     * Required. The pushkey given when the pusher was created.
     */
    @SerialName("pushkey")
    val pushKey: String
)
