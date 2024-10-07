/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common.permissions

import androidx.compose.runtime.Composable

class FakePermissionsPresenter : PermissionsPresenter {
    val events = mutableListOf<PermissionsEvents>()

    private fun handleEvent(event: PermissionsEvents) {
        events += event
    }

    private var state = PermissionsState(
        permissions = PermissionsState.Permissions.NoneGranted,
        shouldShowRationale = false,
        eventSink = ::handleEvent
    )
        set(value) {
            field = value.copy(eventSink = ::handleEvent)
        }

    fun givenState(state: PermissionsState) {
        this.state = state
    }

    @Composable
    override fun present(): PermissionsState = state
}
