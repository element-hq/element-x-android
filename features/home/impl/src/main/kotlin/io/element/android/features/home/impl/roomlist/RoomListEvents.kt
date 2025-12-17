/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.matrix.api.core.RoomId

sealed interface RoomListEvents {
    data class UpdateVisibleRange(val range: IntRange) : RoomListEvents
    data object DismissRequestVerificationPrompt : RoomListEvents
    data object DismissBanner : RoomListEvents
    data object DismissNewNotificationSoundBanner : RoomListEvents
    data object ToggleSearchResults : RoomListEvents
    data class ShowContextMenu(val roomSummary: RoomListRoomSummary) : RoomListEvents

    data class AcceptInvite(val roomSummary: RoomListRoomSummary) : RoomListEvents
    data class DeclineInvite(val roomSummary: RoomListRoomSummary, val blockUser: Boolean) : RoomListEvents
    data class ShowDeclineInviteMenu(val roomSummary: RoomListRoomSummary) : RoomListEvents
    data object HideDeclineInviteMenu : RoomListEvents

    sealed interface ContextMenuEvents : RoomListEvents
    data object HideContextMenu : ContextMenuEvents
    data class LeaveRoom(val roomId: RoomId, val needsConfirmation: Boolean) : ContextMenuEvents
    data class MarkAsRead(val roomId: RoomId) : ContextMenuEvents
    data class MarkAsUnread(val roomId: RoomId) : ContextMenuEvents
    data class SetRoomIsFavorite(val roomId: RoomId, val isFavorite: Boolean) : ContextMenuEvents
    data class ClearCacheOfRoom(val roomId: RoomId) : ContextMenuEvents
}
