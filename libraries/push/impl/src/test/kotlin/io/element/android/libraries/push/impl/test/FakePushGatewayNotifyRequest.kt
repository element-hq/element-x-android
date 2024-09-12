/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.test

import io.element.android.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushGatewayNotifyRequest(
    private val executeResult: (PushGatewayNotifyRequest.Params) -> Unit = { lambdaError() }
) : PushGatewayNotifyRequest {
    override suspend fun execute(params: PushGatewayNotifyRequest.Params) {
        executeResult(params)
    }
}
