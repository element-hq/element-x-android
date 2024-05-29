/*
 * Copyright (c) 2024 New Vector Ltd
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
