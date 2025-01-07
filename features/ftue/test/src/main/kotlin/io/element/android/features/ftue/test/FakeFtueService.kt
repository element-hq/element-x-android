/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.test

import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFtueService(
    private val resetLambda: () -> Unit = { lambdaError() },
) : FtueService {
    override val state: MutableStateFlow<FtueState> = MutableStateFlow(FtueState.Unknown)

    override suspend fun reset() {
        resetLambda()
    }

    suspend fun emitState(newState: FtueState) {
        state.emit(newState)
    }
}
