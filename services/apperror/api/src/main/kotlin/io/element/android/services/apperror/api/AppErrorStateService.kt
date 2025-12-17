/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.api

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.StateFlow

interface AppErrorStateService {
    val appErrorStateFlow: StateFlow<AppErrorState>

    fun showError(title: String, body: String)

    fun showError(@StringRes titleRes: Int, @StringRes bodyRes: Int)
}
