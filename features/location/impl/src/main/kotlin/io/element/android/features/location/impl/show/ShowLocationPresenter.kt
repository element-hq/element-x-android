/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta

@AssistedInject
class ShowLocationPresenter(
    @Assisted private val location: Location,
    @Assisted private val description: String?,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
) : Presenter<ShowLocationState> {
    @AssistedFactory
    fun interface Factory {
        fun create(location: Location, description: String?): ShowLocationPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): ShowLocationState {
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var isTrackMyLocation by remember { mutableStateOf(false) }
        val appName by remember { derivedStateOf { buildMeta.applicationName } }
        var permissionDialog: ShowLocationState.Dialog by remember {
            mutableStateOf(ShowLocationState.Dialog.None)
        }

        LaunchedEffect(permissionsState.permissions) {
            if (permissionsState.isAnyGranted) {
                permissionDialog = ShowLocationState.Dialog.None
            }
        }

        fun handleEvent(event: ShowLocationEvents) {
            when (event) {
                ShowLocationEvents.Share -> locationActions.share(location, description)
                is ShowLocationEvents.TrackMyLocation -> {
                    if (event.enabled) {
                        when {
                            permissionsState.isAnyGranted -> isTrackMyLocation = true
                            permissionsState.shouldShowRationale -> permissionDialog = ShowLocationState.Dialog.PermissionRationale
                            else -> permissionDialog = ShowLocationState.Dialog.PermissionDenied
                        }
                    } else {
                        isTrackMyLocation = false
                    }
                }
                ShowLocationEvents.DismissDialog -> permissionDialog = ShowLocationState.Dialog.None
                ShowLocationEvents.OpenAppSettings -> {
                    locationActions.openSettings()
                    permissionDialog = ShowLocationState.Dialog.None
                }
                ShowLocationEvents.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        return ShowLocationState(
            permissionDialog = permissionDialog,
            location = location,
            description = description,
            hasLocationPermission = permissionsState.isAnyGranted,
            isTrackMyLocation = isTrackMyLocation,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }
}
