/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.libraries.push.impl.pushgateway

import retrofit2.http.Body
import retrofit2.http.POST

interface PushGatewayAPI {
    /**
     * Ask the Push Gateway to send a push to the current device.
     *
     * Ref: https://matrix.org/docs/spec/push_gateway/r0.1.1#post-matrix-push-v1-notify
     */
    @POST(PushGatewayConfig.URI_PUSH_GATEWAY_PREFIX_PATH + "notify")
    suspend fun notify(@Body body: PushGatewayNotifyBody): PushGatewayNotifyResponse
}
