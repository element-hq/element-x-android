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

import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.configureroom.avatar.AvatarAction
import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface ConfigureRoomEvents {
    data class RoomNameChanged(val name: String) : ConfigureRoomEvents
    data class TopicChanged(val topic: String) : ConfigureRoomEvents
    data class RoomPrivacyChanged(val privacy: RoomPrivacy) : ConfigureRoomEvents
    data class RemoveFromSelection(val matrixUser: MatrixUser) : ConfigureRoomEvents
    data class CreateRoom(val config: CreateRoomConfig) : ConfigureRoomEvents
    data class HandleAvatarAction(val action: AvatarAction) : ConfigureRoomEvents
    object CancelCreateRoom : ConfigureRoomEvents
}
