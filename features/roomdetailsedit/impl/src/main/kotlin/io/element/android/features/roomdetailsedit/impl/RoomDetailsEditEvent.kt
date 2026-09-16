/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetailsedit.impl

import io.element.android.libraries.matrix.ui.media.AvatarAction

sealed interface RoomDetailsEditEvent {
    data class HandleAvatarAction(val action: AvatarAction) : RoomDetailsEditEvent
    data class UpdateRoomName(val name: String) : RoomDetailsEditEvent
    data class UpdateRoomTopic(val topic: String) : RoomDetailsEditEvent
    data object OnBackPress : RoomDetailsEditEvent
    data object Save : RoomDetailsEditEvent
    data object CloseDialog : RoomDetailsEditEvent
}
