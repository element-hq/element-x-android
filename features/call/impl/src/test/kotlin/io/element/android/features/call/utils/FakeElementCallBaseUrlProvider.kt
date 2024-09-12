/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.utils

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.call.ElementCallBaseUrlProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakeElementCallBaseUrlProvider(
    private val providesLambda: (MatrixClient) -> String? = { lambdaError() }
) : ElementCallBaseUrlProvider {
    override suspend fun provides(matrixClient: MatrixClient): String? {
        return providesLambda(matrixClient)
    }
}
