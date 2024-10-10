/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.test

import io.element.android.features.logout.api.LogoutUseCase
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeLogoutUseCase(
    var logoutLambda: (Boolean) -> String? = { lambdaError() }
) : LogoutUseCase {
    override suspend fun logout(ignoreSdkError: Boolean): String? = simulateLongTask {
        logoutLambda(ignoreSdkError)
    }
}
