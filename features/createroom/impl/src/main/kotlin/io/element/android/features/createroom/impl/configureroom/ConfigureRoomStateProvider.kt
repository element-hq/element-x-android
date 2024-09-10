/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.permissions.api.aPermissionsState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class ConfigureRoomStateProvider : PreviewParameterProvider<ConfigureRoomState> {
    override val values: Sequence<ConfigureRoomState>
        get() = sequenceOf(
            aConfigureRoomState(),
            aConfigureRoomState().copy(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    invites = aMatrixUserList().toImmutableList(),
                    privacy = RoomPrivacy.Public,
                ),
            ),
        )
}

fun aConfigureRoomState() = ConfigureRoomState(
    config = CreateRoomConfig(),
    avatarActions = persistentListOf(),
    createRoomAction = AsyncAction.Uninitialized,
    cameraPermissionState = aPermissionsState(showDialog = false),
    eventSink = { },
)
