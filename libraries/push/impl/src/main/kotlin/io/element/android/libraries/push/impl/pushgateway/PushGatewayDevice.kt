/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
    val pushKey: String,
    /** Optional. Additional pusher data. */
    @SerialName("data")
    val data: PusherData? = null,
)

@Serializable
data class PusherData(
    @SerialName("default_payload")
    val defaultPayload: Map<String, String>,
)
