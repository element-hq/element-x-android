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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.location.api.Location
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
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.api.room.joinedRoomMembers
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine

@AssistedInject
class ShowLocationPresenter(
    @Assisted private val mode: ShowLocationMode,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
    private val dateFormatter: DateFormatter,
    private val stringProvider: StringProvider,
    private val joinedRoom: JoinedRoom,
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

        val locationShares = when (mode) {
            is ShowLocationMode.Static -> {
                remember {
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
            }
            is ShowLocationMode.Live -> {
                produceState(persistentListOf()) {
                    val comparator = LiveLocationShareComparator(currentUser = joinedRoom.sessionId)
                    val liveLocationSharesFlow = joinedRoom.subscribeToLiveLocationShares()
                    val membersStateFlow = joinedRoom.membersStateFlow.mapState { it.joinedRoomMembers() }
                    combine(liveLocationSharesFlow, membersStateFlow) { liveShares, members ->
                        liveShares
                            .sortedWith(comparator)
                            .mapNotNull { share ->
                                val lastLocation = share.lastLocation ?: return@mapNotNull null
                                val location = Location.fromGeoUri(lastLocation.geoUri) ?: return@mapNotNull null
                                val member = members.find { it.userId == share.userId }
                                val displayName = member?.getBestName() ?: share.userId.value
                                val avatarUrl = member?.avatarUrl
                                val relativeTime = dateFormatter.format(timestamp = lastLocation.timestamp, mode = DateFormatterMode.Full, useRelative = true)
                                val formattedTimestamp = stringProvider.getString(
                                    CommonStrings.screen_static_location_sheet_timestamp_description,
                                    relativeTime
                                )
                                LocationShareItem(
                                    userId = share.userId,
                                    displayName = displayName,
                                    avatarData = AvatarData(
                                        id = share.userId.value,
                                        name = displayName,
                                        url = avatarUrl,
                                        size = AvatarSize.UserListItem,
                                    ),
                                    formattedTimestamp = formattedTimestamp,
                                    location = location,
                                    isLive = true,
                                    assetType = lastLocation.assetType,
                                )
                            }
                            .toImmutableList()
                    }.collect { value = it }
                }.value
            }
        }

        val focusedLocation = when (mode) {
            is ShowLocationMode.Static -> locationShares.firstOrNull()
            is ShowLocationMode.Live -> locationShares.firstOrNull { it.userId == mode.senderId }
        }

        return ShowLocationState(
            dialogState = dialogState,
            locationShares = locationShares,
            focusedLocation = focusedLocation,
            hasLocationPermission = permissionsState.isAnyGranted,
            isTrackMyLocation = isTrackMyLocation,
            isLive = mode is ShowLocationMode.Live,
            appName = appName,
            eventSink = ::handleEvent,
        )
    }
}
