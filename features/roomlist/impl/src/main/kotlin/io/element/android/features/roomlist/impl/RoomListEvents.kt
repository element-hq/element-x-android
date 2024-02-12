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

package io.element.android.features.roomlist.impl

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.matrix.api.core.RoomId

sealed interface RoomListEvents {
    data class UpdateFilter(val newFilter: String) : RoomListEvents
    data class UpdateVisibleRange(val range: IntRange) : RoomListEvents
    data object DismissRequestVerificationPrompt : RoomListEvents
    data object DismissRecoveryKeyPrompt : RoomListEvents
    data object ToggleSearchResults : RoomListEvents
    data class ShowContextMenu(val roomListRoomSummary: RoomListRoomSummary) : RoomListEvents

    sealed interface ContextMenuEvents : RoomListEvents
    data object HideContextMenu : ContextMenuEvents
    data class LeaveRoom(val roomId: RoomId) : ContextMenuEvents
    data class MarkAsRead(val roomId: RoomId) : ContextMenuEvents
    data class MarkAsUnread(val roomId: RoomId) : ContextMenuEvents
    data class SetRoomIsFavorite(val roomId: RoomId, val isFavorite: Boolean) : ContextMenuEvents
}
