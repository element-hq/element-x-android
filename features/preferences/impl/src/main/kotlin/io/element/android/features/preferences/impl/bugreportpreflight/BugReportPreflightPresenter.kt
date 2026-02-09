/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.bugreportpreflight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter

@Inject
class BugReportPreflightPresenter(
    private val viewModel: BugReportPreflightViewModel,
) : Presenter<BugReportPreflightUiState> {
    @Composable
    override fun present(): BugReportPreflightUiState {
        val state by viewModel.uiState.collectAsState()
        return state
    }
}
