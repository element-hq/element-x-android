/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import io.element.android.features.call.impl.utils.PipController
import io.element.android.tests.testutils.lambda.lambdaError

class FakePipController(
    private val canEnterPipResult: () -> Boolean = { lambdaError() },
    private val enterPipResult: () -> Unit = { lambdaError() },
    private val exitPipResult: () -> Unit = { lambdaError() },
) : PipController {
    override suspend fun canEnterPip(): Boolean = canEnterPipResult()

    override fun enterPip() = enterPipResult()

    override fun exitPip() = exitPipResult()
}
