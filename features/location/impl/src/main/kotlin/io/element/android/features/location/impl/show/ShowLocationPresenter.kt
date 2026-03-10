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
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.room.location.AssetType

@AssistedInject
class ShowLocationPresenter(
    @Assisted private val mode: ShowLocationMode,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
    private val dateFormatter: DateFormatter,
) : Presenter<ShowLocationState> {
    @AssistedFactory
    fun interface Factory {
        fun create(mode: ShowLocationMode): ShowLocationPresenter
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
                is ShowLocationEvents.Share -> {
                    locationActions.share(event.location, null)
                }
                is ShowLocationEvents.TrackMyLocation -> {
                    if (event.enabled) {
                        when {
                            permissionsState.isAnyGranted -> {
                                if (!locationActions.isLocationEnabled()) {
                                    permissionDialog = ShowLocationState.Dialog.LocationServiceDisabled
                                } else {
                                    isTrackMyLocation = true
                                }
                            }
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
                ShowLocationEvents.OpenLocationSettings -> {
                    locationActions.openLocationSettings()
                    permissionDialog = ShowLocationState.Dialog.None
                }
                ShowLocationEvents.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        val markers = remember(mode) {
            when (mode) {
                is ShowLocationMode.Static -> {
                    val pinVariant = if (mode.assetType == AssetType.PIN) {
                        PinVariant.PinnedLocation
                    } else {
                        PinVariant.UserLocation(
                            avatarData = AvatarData(
                                id = mode.senderId.value,
                                name = mode.senderName,
                                url = mode.senderAvatarUrl,
                                size = AvatarSize.UserListItem,
                            ),
                            isLive = false,
                        )
                    }
                    listOf(
                        LocationMarkerData(
                            id = mode.senderId.value,
                            location = mode.location,
                            variant = pinVariant,
                        )
                    )
                }
                ShowLocationMode.Live -> emptyList()
            }
        }

        val locationShares = remember(mode) {
            when (mode) {
                is ShowLocationMode.Static -> {
                    val relativeTime = dateFormatter.format(timestamp = mode.timestamp, mode = DateFormatterMode.Full, useRelative = true)
                    val formattedTimestamp = "Shared $relativeTime"
                    listOf(
                        LocationShareItem(
                            userId = mode.senderId,
                            displayName = mode.senderName,
                            avatarData = AvatarData(
                                id = mode.senderId.value,
                                name = mode.senderName,
                                url = mode.senderAvatarUrl,
                                size = AvatarSize.UserListItem,
                            ),
                            formattedTimestamp = formattedTimestamp,
                            isLive = false,
                            assetType = mode.assetType,
                            location = mode.location,
                        )
                    )
                }
                ShowLocationMode.Live -> emptyList()
            }
        }

        return ShowLocationState(
            permissionDialog = permissionDialog,
            mode = mode,
            markers = markers,
            locationShares = locationShares,
            hasLocationPermission = permissionsState.isAnyGranted,
            isTrackMyLocation = isTrackMyLocation,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }
}
