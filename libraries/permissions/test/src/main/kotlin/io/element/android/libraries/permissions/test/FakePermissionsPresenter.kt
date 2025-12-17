/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState

class FakePermissionsPresenter(
    private val initialState: PermissionsState = aPermissionsState(showDialog = false),
) : PermissionsPresenter {
    private fun handleEvent(event: PermissionsEvent) {
        when (event) {
            PermissionsEvent.RequestPermissions -> state.value = state.value.copy(showDialog = true, permissionAlreadyAsked = true)
            PermissionsEvent.CloseDialog -> state.value = state.value.copy(showDialog = false)
            PermissionsEvent.OpenSystemSettingAndCloseDialog -> state.value = state.value.copy(showDialog = false)
        }
    }

    private val state = mutableStateOf(initialState.copy(eventSink = ::handleEvent))

    fun setPermissionGranted() {
        state.value = state.value.copy(permissionGranted = true)
    }

    fun setPermissionDenied() {
        state.value = state.value.copy(permissionGranted = false, permissionAlreadyDenied = true)
    }

    @Composable
    override fun present(): PermissionsState {
        return state.value
    }
}
