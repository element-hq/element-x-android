/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultAppErrorStateService(
    private val stringProvider: StringProvider,
) : AppErrorStateService {
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

    override fun showError(titleRes: Int, bodyRes: Int) {
        val title = stringProvider.getString(titleRes)
        val body = stringProvider.getString(bodyRes)
        showError(title, body)
    }
}
