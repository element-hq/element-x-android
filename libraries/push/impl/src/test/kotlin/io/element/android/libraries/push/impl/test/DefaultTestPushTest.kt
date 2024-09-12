/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.test

import io.element.android.appconfig.PushConfig
import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.libraries.pushproviders.test.aCurrentUserPushConfig
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
        val aConfig = aCurrentUserPushConfig()
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
