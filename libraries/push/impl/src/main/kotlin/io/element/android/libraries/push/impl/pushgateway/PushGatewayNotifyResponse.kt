/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushGatewayNotifyResponse(
    @SerialName("rejected")
    val rejectedPushKeys: List<String>
)
