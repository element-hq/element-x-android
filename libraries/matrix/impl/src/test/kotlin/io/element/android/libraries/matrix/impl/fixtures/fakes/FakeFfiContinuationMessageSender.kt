/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.ContinuationMessageSender
import org.matrix.rustcomponents.sdk.NoHandle

class FakeFfiContinuationMessageSender(
    private val confirmResult: () -> Unit = { lambdaError() },
    private val cancelResult: () -> Unit = { lambdaError() },
) : ContinuationMessageSender(NoHandle) {
    override suspend fun confirm() {
        confirmResult()
    }

    override suspend fun cancel() {
        cancelResult()
    }
}
