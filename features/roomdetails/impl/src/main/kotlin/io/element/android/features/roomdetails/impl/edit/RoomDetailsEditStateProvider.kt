/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.edit

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState
import kotlinx.collections.immutable.toImmutableList

open class RoomDetailsEditStateProvider : PreviewParameterProvider<RoomDetailsEditState> {
    override val values: Sequence<RoomDetailsEditState>
        get() = sequenceOf(
            aRoomDetailsEditState(),
            aRoomDetailsEditState(roomTopic = ""),
            aRoomDetailsEditState(roomRawName = ""),
            aRoomDetailsEditState(roomAvatarUrl = "example://uri".toUri()),
            aRoomDetailsEditState(canChangeName = true, canChangeTopic = false, canChangeAvatar = true, saveButtonEnabled = false),
            aRoomDetailsEditState(canChangeName = false, canChangeTopic = true, canChangeAvatar = false, saveButtonEnabled = false),
            aRoomDetailsEditState(saveAction = AsyncAction.Loading),
            aRoomDetailsEditState(saveAction = AsyncAction.Failure(RuntimeException("Whelp"))),
        )
}

fun aRoomDetailsEditState(
    roomId: RoomId = RoomId("!aRoomId:aDomain"),
    roomRawName: String = "Marketing",
    canChangeName: Boolean = true,
    roomTopic: String = "a room topic that is quite long so should wrap onto multiple lines",
    canChangeTopic: Boolean = true,
    roomAvatarUrl: Uri? = null,
    canChangeAvatar: Boolean = true,
    avatarActions: List<AvatarAction> = emptyList(),
    saveButtonEnabled: Boolean = true,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    cameraPermissionState: PermissionsState = aPermissionsState(showDialog = false),
    eventSink: (RoomDetailsEditEvents) -> Unit = {},
) = RoomDetailsEditState(
    roomId = roomId,
    roomRawName = roomRawName,
    canChangeName = canChangeName,
    roomTopic = roomTopic,
    canChangeTopic = canChangeTopic,
    roomAvatarUrl = roomAvatarUrl,
    canChangeAvatar = canChangeAvatar,
    avatarActions = avatarActions.toImmutableList(),
    saveButtonEnabled = saveButtonEnabled,
    saveAction = saveAction,
    cameraPermissionState = cameraPermissionState,
    eventSink = eventSink,
)
