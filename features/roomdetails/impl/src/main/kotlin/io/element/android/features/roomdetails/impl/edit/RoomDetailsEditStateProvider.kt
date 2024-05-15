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

package io.element.android.features.roomdetails.impl.edit

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
            aRoomDetailsEditState(roomAvatarUrl = Uri.parse("example://uri")),
            aRoomDetailsEditState(canChangeName = true, canChangeTopic = false, canChangeAvatar = true, saveButtonEnabled = false),
            aRoomDetailsEditState(canChangeName = false, canChangeTopic = true, canChangeAvatar = false, saveButtonEnabled = false),
            aRoomDetailsEditState(saveAction = AsyncAction.Loading),
            aRoomDetailsEditState(saveAction = AsyncAction.Failure(Throwable("Whelp")))
        )
}

private fun aRoomDetailsEditState(
    roomId: RoomId = RoomId("!aRoomId:aDomain"),
    roomName: String = "Marketing",
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
    roomName = roomName,
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
