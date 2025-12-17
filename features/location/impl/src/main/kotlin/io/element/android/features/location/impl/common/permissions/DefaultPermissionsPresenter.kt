/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.permissions

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding

@Suppress("unused")
@AssistedInject
class DefaultPermissionsPresenter(
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

        fun handleEvent(event: PermissionsEvents) {
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
            eventSink = ::handleEvent,
        )
    }
}
