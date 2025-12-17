/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test

import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeSessionEnterpriseService(
    private val isElementCallAvailableResult: () -> Boolean = { lambdaError() },
) : SessionEnterpriseService {
    override suspend fun init() {
    }

    override suspend fun isElementCallAvailable(): Boolean = simulateLongTask {
        isElementCallAvailableResult()
    }
}
