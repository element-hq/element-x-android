/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FakePermissionsPresenter : PermissionsPresenter {
    val events = mutableListOf<PermissionsEvent>()

    private fun handleEvent(event: PermissionsEvent) {
        events += event
    }

    private var state by mutableStateOf(
        PermissionsState(
            permissions = PermissionsState.Permissions.NoneGranted,
            shouldShowRationale = false,
            permissionsAlreadyRequested = false,
            eventSink = ::handleEvent,
        )
    )

    fun givenState(state: PermissionsState) {
        this.state = state.copy(eventSink = ::handleEvent)
    }

    @Composable
    override fun present(): PermissionsState = state
}
