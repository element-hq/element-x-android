/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.MutableStateFlow

class MutablePresenter<State>(initialState: State) : Presenter<State> {
    private val stateFlow = MutableStateFlow(initialState)

    fun updateState(state: State) {
        stateFlow.value = state
    }

    @Composable
    override fun present(): State {
        return stateFlow.collectAsState().value
    }
}
