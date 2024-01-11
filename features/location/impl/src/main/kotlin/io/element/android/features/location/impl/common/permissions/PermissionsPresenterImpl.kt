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

package io.element.android.features.location.impl.common.permissions

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.di.AppScope

class PermissionsPresenterImpl @AssistedInject constructor(
    @Assisted private val permissions: List<String>
) : PermissionsPresenter {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : PermissionsPresenter.Factory {
        override fun create(permissions: List<String>): PermissionsPresenterImpl
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun present(): PermissionsState {
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissions)

        fun handleEvents(event: PermissionsEvents) {
            when (event) {
                PermissionsEvents.RequestPermissions -> multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }

        return PermissionsState(
            permissions = when {
                multiplePermissionsState.allPermissionsGranted -> PermissionsState.Permissions.AllGranted
                multiplePermissionsState.permissions.any { it.status.isGranted } -> PermissionsState.Permissions.SomeGranted
                else -> PermissionsState.Permissions.NoneGranted
            },
            shouldShowRationale = multiplePermissionsState.shouldShowRationale,
            eventSink = ::handleEvents,
        )
    }
}
