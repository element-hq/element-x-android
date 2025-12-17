/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.test

import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushHandler(
    private val handleResult: (PushData, String) -> Unit = { _, _ -> lambdaError() },
    private val handleInvalidResult: (String, String) -> Unit = { _, _ -> lambdaError() },
) : PushHandler {
    override suspend fun handle(pushData: PushData, providerInfo: String) {
        handleResult(pushData, providerInfo)
    }

    override suspend fun handleInvalid(providerInfo: String, data: String) {
        handleInvalidResult(providerInfo, data)
    }
}
