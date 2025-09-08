/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.test

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.PushConfig
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig

interface TestPush {
    suspend fun execute(config: CurrentUserPushConfig)
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultTestPush(
    private val pushGatewayNotifyRequest: PushGatewayNotifyRequest,
) : TestPush {
    override suspend fun execute(config: CurrentUserPushConfig) {
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = config.url,
                appId = PushConfig.PUSHER_APP_ID,
                pushKey = config.pushKey,
                eventId = TEST_EVENT_ID,
                roomId = TEST_ROOM_ID,
            )
        )
    }

    companion object {
        val TEST_EVENT_ID = EventId("\$THIS_IS_A_FAKE_EVENT_ID")
        val TEST_ROOM_ID = RoomId("!room:domain")
    }
}
