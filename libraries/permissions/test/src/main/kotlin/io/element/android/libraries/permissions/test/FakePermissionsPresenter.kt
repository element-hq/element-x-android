/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.permissions.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState

class FakePermissionsPresenter(
    private val initialState: PermissionsState = aPermissionsState(showDialog = false),
) : PermissionsPresenter {
    private fun eventSink(events: PermissionsEvents) {
        when (events) {
            PermissionsEvents.RequestPermissions -> state.value = state.value.copy(showDialog = true, permissionAlreadyAsked = true)
            PermissionsEvents.CloseDialog -> state.value = state.value.copy(showDialog = false)
            PermissionsEvents.OpenSystemSettingAndCloseDialog -> state.value = state.value.copy(showDialog = false)
        }
    }

    private val state = mutableStateOf(initialState.copy(eventSink = ::eventSink))

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
