/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter

class ResetIdentityRootPresenter : Presenter<ResetIdentityRootState> {
    @Composable
    override fun present(): ResetIdentityRootState {
        var displayConfirmDialog by remember { mutableStateOf(false) }

        fun handleEvent(event: ResetIdentityRootEvent) {
            displayConfirmDialog = when (event) {
                ResetIdentityRootEvent.Continue -> true
                ResetIdentityRootEvent.DismissDialog -> false
            }
        }

        return ResetIdentityRootState(
            displayConfirmationDialog = displayConfirmDialog,
            eventSink = ::handleEvent,
        )
    }
}
