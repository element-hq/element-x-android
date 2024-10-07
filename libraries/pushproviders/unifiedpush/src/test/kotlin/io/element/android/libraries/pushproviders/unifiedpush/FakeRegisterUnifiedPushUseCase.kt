/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.tests.testutils.lambda.lambdaError

class FakeRegisterUnifiedPushUseCase(
    private val result: (Distributor, String) -> Result<Unit> = { _, _ -> lambdaError() }
) : RegisterUnifiedPushUseCase {
    override suspend fun execute(distributor: Distributor, clientSecret: String): Result<Unit> {
        return result(distributor, clientSecret)
    }
}
