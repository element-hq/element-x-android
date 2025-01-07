/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.edit

import io.element.android.libraries.matrix.ui.media.AvatarAction

sealed interface RoomDetailsEditEvents {
    data class HandleAvatarAction(val action: AvatarAction) : RoomDetailsEditEvents
    data class UpdateRoomName(val name: String) : RoomDetailsEditEvents
    data class UpdateRoomTopic(val topic: String) : RoomDetailsEditEvents
    data object Save : RoomDetailsEditEvents
    data object CancelSaveChanges : RoomDetailsEditEvents
}
