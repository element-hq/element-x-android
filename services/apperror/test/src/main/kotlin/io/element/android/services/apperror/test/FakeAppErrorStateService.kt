/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.test

import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppErrorStateService(
    initialState: AppErrorState = AppErrorState.NoError,
    private val showErrorResult: (String, String) -> Unit = { _, _ -> lambdaError() },
    private val showErrorResResult: (Int, Int) -> Unit = { _, _ -> lambdaError() }
) : AppErrorStateService {
    private val mutableAppErrorStateFlow = MutableStateFlow(initialState)
    override val appErrorStateFlow: StateFlow<AppErrorState> = mutableAppErrorStateFlow.asStateFlow()

    override fun showError(title: String, body: String) {
        showErrorResult(title, body)
    }

    override fun showError(titleRes: Int, bodyRes: Int) {
        showErrorResResult(titleRes, bodyRes)
    }

    fun setAppErrorState(state: AppErrorState) {
        mutableAppErrorStateFlow.value = state
    }
}
