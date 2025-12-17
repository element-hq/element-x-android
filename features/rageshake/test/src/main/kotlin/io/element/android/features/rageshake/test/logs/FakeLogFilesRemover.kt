/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.test.logs

import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import java.io.File

class FakeLogFilesRemover(
    val performLambda: LambdaOneParamRecorder<(File) -> Boolean, Unit> = lambdaRecorder<(File) -> Boolean, Unit> { },
) : LogFilesRemover {
    override suspend fun perform(predicate: (File) -> Boolean) {
        performLambda(predicate)
    }
}
