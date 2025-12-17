/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.test

import io.element.android.libraries.pushproviders.api.Config
import io.element.android.tests.testutils.lambda.lambdaError

class FakeTestPush(
    private val executeResult: (Config) -> Unit = { lambdaError() }
) : TestPush {
    override suspend fun execute(config: Config) {
        executeResult(config)
    }
}
