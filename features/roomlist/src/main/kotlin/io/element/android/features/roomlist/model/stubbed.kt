/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.roomlist.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal fun stubbedRoomSummaries(): ImmutableList<RoomListRoomSummary> {
    return persistentListOf(
        RoomListRoomSummary(
            name = "Room",
            hasUnread = true,
            timestamp = "14:18",
            lastMessage = "A very very very very long message which suites on two lines",
            avatarData = AvatarData("!id", "R"),
            id = "roomId"
        ),
        RoomListRoomSummary(
            name = "Room#2",
            hasUnread = false,
            timestamp = "14:16",
            lastMessage = "A short message",
            avatarData = AvatarData("!id", "Z"),
            id = "roomId2"
        ),
        RoomListRoomSummaryPlaceholders.create("roomId2")
    )
}
