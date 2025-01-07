/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.libraries.push.impl.pushgateway

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import javax.inject.Inject

interface PushGatewayNotifyRequest {
    data class Params(
        val url: String,
        val appId: String,
        val pushKey: String,
        val eventId: EventId,
        val roomId: RoomId,
    )

    suspend fun execute(params: Params)
}

@ContributesBinding(AppScope::class)
class DefaultPushGatewayNotifyRequest @Inject constructor(
    private val pushGatewayApiFactory: PushGatewayApiFactory,
) : PushGatewayNotifyRequest {
    override suspend fun execute(params: PushGatewayNotifyRequest.Params) {
        val pushGatewayApi = pushGatewayApiFactory.create(
            params.url.substringBefore(PushGatewayConfig.URI_PUSH_GATEWAY_PREFIX_PATH)
        )
        val response = pushGatewayApi.notify(
            PushGatewayNotifyBody(
                PushGatewayNotification(
                    eventId = params.eventId.value,
                    roomId = params.roomId.value,
                    devices = listOf(
                        PushGatewayDevice(
                            params.appId,
                            params.pushKey
                        )
                    )
                )
            )
        )

        if (response.rejectedPushKeys.contains(params.pushKey)) {
            throw PushGatewayFailure.PusherRejected()
        }
    }
}
