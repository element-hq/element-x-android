/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.apperror.api

import kotlinx.coroutines.flow.StateFlow

interface AppErrorStateService {
    val appErrorStateFlow: StateFlow<AppErrorState>

    fun showError(title: String, body: String)
}
