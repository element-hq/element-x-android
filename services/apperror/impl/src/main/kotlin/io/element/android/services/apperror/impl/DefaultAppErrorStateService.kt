/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultAppErrorStateService @Inject constructor() : AppErrorStateService {
    private val currentAppErrorState = MutableStateFlow<AppErrorState>(AppErrorState.NoError)
    override val appErrorStateFlow: StateFlow<AppErrorState> = currentAppErrorState

    override fun showError(title: String, body: String) {
        currentAppErrorState.value = AppErrorState.Error(
            title = title,
            body = body,
            dismiss = {
                currentAppErrorState.value = AppErrorState.NoError
            },
        )
    }
}
