/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.tracing

import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import io.element.android.tests.testutils.lambda.lambdaError
import timber.log.Timber

class FakeTracingService(
    private val createTimberTreeResult: (String) -> Timber.Tree = { lambdaError() },
    private val updateWriteToFilesConfigurationResult: (WriteToFilesConfiguration) -> Unit = { lambdaError() }
) : TracingService {
    override fun createTimberTree(target: String): Timber.Tree {
        return createTimberTreeResult(target)
    }

    override fun updateWriteToFilesConfiguration(config: WriteToFilesConfiguration) {
        updateWriteToFilesConfigurationResult(config)
    }
}
