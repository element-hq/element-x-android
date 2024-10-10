/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.test.test

import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushHandler(
    private val handleResult: (PushData) -> Unit = { lambdaError() }
) : PushHandler {
    override suspend fun handle(pushData: PushData) {
        handleResult(pushData)
    }
}
