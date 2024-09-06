/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.AvatarAction

sealed interface ConfigureRoomEvents {
    data class RoomNameChanged(val name: String) : ConfigureRoomEvents
    data class TopicChanged(val topic: String) : ConfigureRoomEvents
    data class RoomPrivacyChanged(val privacy: RoomPrivacy) : ConfigureRoomEvents
    data class RemoveFromSelection(val matrixUser: MatrixUser) : ConfigureRoomEvents
    data class CreateRoom(val config: CreateRoomConfig) : ConfigureRoomEvents
    data class HandleAvatarAction(val action: AvatarAction) : ConfigureRoomEvents
    data object CancelCreateRoom : ConfigureRoomEvents
}
