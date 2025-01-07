/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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

@Suppress("unused")
class DefaultPermissionsPresenter @AssistedInject constructor(
    @Assisted private val permissions: List<String>
) : PermissionsPresenter {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : PermissionsPresenter.Factory {
        override fun create(permissions: List<String>): DefaultPermissionsPresenter
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
