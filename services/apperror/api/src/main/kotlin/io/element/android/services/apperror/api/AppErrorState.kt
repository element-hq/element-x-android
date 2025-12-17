/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.api

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AppErrorState {
    data object NoError : AppErrorState

    data class Error(
        val title: String,
        val body: String,
        val dismiss: () -> Unit,
    ) : AppErrorState
}
