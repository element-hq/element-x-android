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

package io.element.android.features.roomdetails.impl.edition

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import kotlinx.collections.immutable.persistentListOf

open class RoomDetailsEditionStateProvider : PreviewParameterProvider<RoomDetailsEditionState> {
    override val values: Sequence<RoomDetailsEditionState>
        get() = sequenceOf(
            aRoomDetailsEditionState(),
            aRoomDetailsEditionState().copy(roomTopic = ""),
            aRoomDetailsEditionState().copy(canChangeName = true, canChangeTopic = false, canChangeAvatar = true, saveButtonEnabled = false),
            aRoomDetailsEditionState().copy(canChangeName = false, canChangeTopic = true, canChangeAvatar = false, saveButtonEnabled = false),
        )
}

fun aRoomDetailsEditionState() = RoomDetailsEditionState(
    roomId = "a room id",
    roomName = "Marketing",
    canChangeName = true,
    roomTopic = "a room topic that is quite long so should wrap onto multiple lines",
    canChangeTopic = true,
    roomAvatarUrl = null,
    canChangeAvatar = true,
    avatarActions = persistentListOf(),
    saveButtonEnabled = true,
    saveAction = Async.Uninitialized,
    eventSink = {}
)
