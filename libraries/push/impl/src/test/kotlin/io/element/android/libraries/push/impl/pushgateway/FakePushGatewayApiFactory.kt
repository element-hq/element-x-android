/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

class FakePushGatewayApiFactory(
    private val notifyResponse: () -> PushGatewayNotifyResponse
) : PushGatewayApiFactory {
    var baseUrlParameter: String? = null
        private set

    override fun create(baseUrl: String): PushGatewayAPI {
        baseUrlParameter = baseUrl
        return FakePushGatewayAPI(notifyResponse)
    }
}

class FakePushGatewayAPI(
    private val notifyResponse: () -> PushGatewayNotifyResponse
) : PushGatewayAPI {
    override suspend fun notify(body: PushGatewayNotifyBody): PushGatewayNotifyResponse {
        return notifyResponse()
    }
}
