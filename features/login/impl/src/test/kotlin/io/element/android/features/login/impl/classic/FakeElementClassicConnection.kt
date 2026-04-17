/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.classic

import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeElementClassicConnection(
    private val startResult: () -> Unit = { lambdaError() },
    private val stopResult: () -> Unit = { lambdaError() },
    private val requestSessionResult: () -> Unit = { lambdaError() },
    initialState: ElementClassicConnectionState = ElementClassicConnectionState.Idle
) : ElementClassicConnection {
    override fun start() = startResult()
    override fun stop() = stopResult()
    override fun requestSession() = requestSessionResult()
    private val mutableStateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<ElementClassicConnectionState> = mutableStateFlow.asStateFlow()
    suspend fun emitState(state: ElementClassicConnectionState) {
        mutableStateFlow.emit(state)
    }
}
