/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState
import kotlinx.collections.immutable.toImmutableList

open class ConfigureRoomStateProvider : PreviewParameterProvider<ConfigureRoomState> {
    override val values: Sequence<ConfigureRoomState>
        get() = sequenceOf(
            aConfigureRoomState(),
            aConfigureRoomState(
                isKnockFeatureEnabled = false,
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    invites = aMatrixUserList().toImmutableList(),
                    roomVisibility = RoomVisibilityState.Public(
                        roomAddress = RoomAddress.AutoFilled("Room-101"),
                        roomAccess = RoomAccess.Knocking,
                    ),
                ),
            ),
            aConfigureRoomState(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    invites = aMatrixUserList().toImmutableList(),
                    roomVisibility = RoomVisibilityState.Public(
                        roomAddress = RoomAddress.AutoFilled("Room-101"),
                        roomAccess = RoomAccess.Knocking,
                    ),
                ),
            ),
            aConfigureRoomState(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    roomVisibility = RoomVisibilityState.Public(
                        roomAddress = RoomAddress.AutoFilled("Room-101"),
                        roomAccess = RoomAccess.Knocking,
                    ),
                ),
                roomAddressValidity = RoomAddressValidity.NotAvailable,
            ),
            aConfigureRoomState(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    roomVisibility = RoomVisibilityState.Public(
                        roomAddress = RoomAddress.AutoFilled("Room-101"),
                        roomAccess = RoomAccess.Knocking,
                    ),
                ),
                roomAddressValidity = RoomAddressValidity.InvalidSymbols,
            ),
            aConfigureRoomState(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    roomVisibility = RoomVisibilityState.Public(
                        roomAddress = RoomAddress.AutoFilled("Room-101"),
                        roomAccess = RoomAccess.Knocking,
                    ),
                ),
                roomAddressValidity = RoomAddressValidity.Valid,
            ),
        )
}

fun aConfigureRoomState(
    config: CreateRoomConfig = CreateRoomConfig(),
    isKnockFeatureEnabled: Boolean = true,
    avatarActions: List<AvatarAction> = emptyList(),
    createRoomAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    cameraPermissionState: PermissionsState = aPermissionsState(showDialog = false),
    homeserverName: String = "matrix.org",
    roomAddressValidity: RoomAddressValidity = RoomAddressValidity.Valid,
    eventSink: (ConfigureRoomEvents) -> Unit = { },
) = ConfigureRoomState(
    config = config,
    isKnockFeatureEnabled = isKnockFeatureEnabled,
    avatarActions = avatarActions.toImmutableList(),
    createRoomAction = createRoomAction,
    cameraPermissionState = cameraPermissionState,
    homeserverName = homeserverName,
    roomAddressValidity = roomAddressValidity,
    eventSink = eventSink,
)
