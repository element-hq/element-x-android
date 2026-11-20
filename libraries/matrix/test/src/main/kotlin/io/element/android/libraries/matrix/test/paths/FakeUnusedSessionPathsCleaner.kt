/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.paths

import io.element.android.libraries.matrix.api.paths.UnusedSessionPathsCleaner
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnusedSessionPathsCleaner(
    private val cleanLambda: () -> Unit = { lambdaError() },
) : UnusedSessionPathsCleaner {
    override suspend fun clean() {
        cleanLambda()
    }
}
