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
import io.element.android.features.location.impl.common.LocationConstraintsCheck
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.checkLocationConstraints
import io.element.android.features.location.impl.common.permissions.PermissionsEvents
import io.element.android.features.location.impl.common.permissions.PermissionsPresenter
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.toDialogState
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.persistentListOf

@AssistedInject
class ShowLocationPresenter(
    @Assisted private val mode: ShowLocationMode,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
    private val dateFormatter: DateFormatter,
    private val stringProvider: StringProvider,
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
        var dialogState: LocationConstraintsDialogState by remember {
            mutableStateOf(LocationConstraintsDialogState.None)
        }

        LaunchedEffect(permissionsState.permissions) {
            if (permissionsState.isAnyGranted) {
                dialogState = LocationConstraintsDialogState.None
            }
        }

        fun handleEvent(event: ShowLocationEvent) {
            when (event) {
                is ShowLocationEvent.Share -> {
                    locationActions.share(event.location, null)
                }
                is ShowLocationEvent.TrackMyLocation -> {
                    if (event.enabled) {
                        val locationConstraints = checkLocationConstraints(permissionsState, locationActions)
                        isTrackMyLocation = locationConstraints is LocationConstraintsCheck.Success
                        dialogState = locationConstraints.toDialogState()
                    } else {
                        isTrackMyLocation = false
                    }
                }
                ShowLocationEvent.DismissDialog -> dialogState = LocationConstraintsDialogState.None
                ShowLocationEvent.OpenAppSettings -> {
                    locationActions.openAppSettings()
                    dialogState = LocationConstraintsDialogState.None
                }
                ShowLocationEvent.OpenLocationSettings -> {
                    locationActions.openLocationSettings()
                    dialogState = LocationConstraintsDialogState.None
                }
                ShowLocationEvent.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        val locationShares = remember {
            when (mode) {
                is ShowLocationMode.Static -> {
                    val relativeTime = dateFormatter.format(timestamp = mode.timestamp, mode = DateFormatterMode.Full, useRelative = true)
                    val formattedTimestamp = stringProvider.getString(
                        CommonStrings.screen_static_location_sheet_timestamp_description,
                        relativeTime
                    )
                    persistentListOf(
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
                            location = mode.location,
                            isLive = false,
                            assetType = mode.assetType,
                        )
                    )
                }
                ShowLocationMode.Live -> persistentListOf()
            }
        }

        return ShowLocationState(
            dialogState = dialogState,
            locationShares = locationShares,
            hasLocationPermission = permissionsState.isAnyGranted,
            isTrackMyLocation = isTrackMyLocation,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }
}
