/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
