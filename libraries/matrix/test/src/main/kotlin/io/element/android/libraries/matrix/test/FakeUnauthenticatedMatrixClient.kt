/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.UnauthenticatedMatrixClient
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnauthenticatedMatrixClient(
    private val getUrlResult: (String) -> Result<ByteArray> = { lambdaError() },
    private val closeLambda: () -> Unit = {},
) : UnauthenticatedMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = getUrlResult(url)

    override fun close() = closeLambda()
}
