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

package io.element.android.libraries.push.impl.test

import io.element.android.appconfig.PushConfig
import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultTestPushTest {
    @Test
    fun `test DefaultTestPush`() = runTest {
        val executeResult = lambdaRecorder<PushGatewayNotifyRequest.Params, Unit> { }
        val defaultTestPush = DefaultTestPush(
            pushGatewayNotifyRequest = FakePushGatewayNotifyRequest(
                executeResult = executeResult,
            )
        )
        val aConfig = CurrentUserPushConfig(
            url = "aUrl",
            pushKey = "aPushKey",
        )
        defaultTestPush.execute(aConfig)
        executeResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    PushGatewayNotifyRequest.Params(
                        url = aConfig.url,
                        appId = PushConfig.PUSHER_APP_ID,
                        pushKey = aConfig.pushKey,
                        eventId = DefaultTestPush.TEST_EVENT_ID,
                        roomId = DefaultTestPush.TEST_ROOM_ID,
                    )
                )
            )
    }
}
