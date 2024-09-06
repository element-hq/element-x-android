/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.matrix.api.core.RoomId

sealed interface RoomListEvents {
    data class UpdateVisibleRange(val range: IntRange) : RoomListEvents
    data object DismissRequestVerificationPrompt : RoomListEvents
    data object DismissRecoveryKeyPrompt : RoomListEvents
    data object ToggleSearchResults : RoomListEvents
    data class AcceptInvite(val roomListRoomSummary: RoomListRoomSummary) : RoomListEvents
    data class DeclineInvite(val roomListRoomSummary: RoomListRoomSummary) : RoomListEvents
    data class ShowContextMenu(val roomListRoomSummary: RoomListRoomSummary) : RoomListEvents

    sealed interface ContextMenuEvents : RoomListEvents
    data object HideContextMenu : ContextMenuEvents
    data class LeaveRoom(val roomId: RoomId) : ContextMenuEvents
    data class MarkAsRead(val roomId: RoomId) : ContextMenuEvents
    data class MarkAsUnread(val roomId: RoomId) : ContextMenuEvents
    data class SetRoomIsFavorite(val roomId: RoomId, val isFavorite: Boolean) : ContextMenuEvents
}
