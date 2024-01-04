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

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.userlist.aListOfSelectedUsers
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.permissions.api.aPermissionsState
import kotlinx.collections.immutable.persistentListOf

open class ConfigureRoomStateProvider : PreviewParameterProvider<ConfigureRoomState> {
    override val values: Sequence<ConfigureRoomState>
        get() = sequenceOf(
            aConfigureRoomState(),
            aConfigureRoomState().copy(
                config = CreateRoomConfig(
                    roomName = "Room 101",
                    topic = "Room topic for this room when the text goes onto multiple lines and is really long, there shouldn’t be more than 3 lines",
                    invites = aListOfSelectedUsers(),
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
