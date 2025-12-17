/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import io.element.android.libraries.push.impl.test.DefaultTestPush
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultPushGatewayNotifyRequestTest {
    @Test
    fun `notify success`() = runTest {
        val factory = FakePushGatewayApiFactory(
            notifyResponse = {
                PushGatewayNotifyResponse(
                    rejectedPushKeys = emptyList()
                )
            }
        )
        val pushGatewayNotifyRequest = DefaultPushGatewayNotifyRequest(
            pushGatewayApiFactory = factory,
        )
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = "aUrl",
                appId = "anAppId",
                pushKey = "aPushKey",
                eventId = DefaultTestPush.TEST_EVENT_ID,
                roomId = DefaultTestPush.TEST_ROOM_ID,
            )
        )
        assertThat(factory.baseUrlParameter).isEqualTo("aUrl")
    }

    @Test
    fun `notify success, url is stripped`() = runTest {
        val factory = FakePushGatewayApiFactory(
            notifyResponse = {
                PushGatewayNotifyResponse(
                    rejectedPushKeys = emptyList()
                )
            }
        )
        val pushGatewayNotifyRequest = DefaultPushGatewayNotifyRequest(
            pushGatewayApiFactory = factory,
        )
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = "aUrl" + PushGatewayConfig.URI_PUSH_GATEWAY_PREFIX_PATH,
                appId = "anAppId",
                pushKey = "aPushKey",
                eventId = DefaultTestPush.TEST_EVENT_ID,
                roomId = DefaultTestPush.TEST_ROOM_ID,
            )
        )
        assertThat(factory.baseUrlParameter).isEqualTo("aUrl")
    }

    @Test
    fun `notify with rejected push key should throw expected Exception`() {
        val factory = FakePushGatewayApiFactory(
            notifyResponse = {
                PushGatewayNotifyResponse(
                    rejectedPushKeys = listOf("aPushKey")
                )
            }
        )
        val pushGatewayNotifyRequest = DefaultPushGatewayNotifyRequest(
            pushGatewayApiFactory = factory,
        )
        assertThrows(PushGatewayFailure.PusherRejected::class.java) {
            runTest {
                pushGatewayNotifyRequest.execute(
                    PushGatewayNotifyRequest.Params(
                        url = "aUrl",
                        appId = "anAppId",
                        pushKey = "aPushKey",
                        eventId = DefaultTestPush.TEST_EVENT_ID,
                        roomId = DefaultTestPush.TEST_ROOM_ID,
                    )
                )
            }
        }
        assertThat(factory.baseUrlParameter).isEqualTo("aUrl")
    }
}
