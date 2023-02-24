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

object RoomListRoomSummaryPlaceholders {

    fun create(id: String): RoomListRoomSummary {
        return RoomListRoomSummary(
            id = id,
            isPlaceholder = true,
            name = "Short name",
            timestamp = "hh:mm",
            lastMessage = "Last message for placeholder",
            avatarData = AvatarData(id, "S")
        )
    }

    fun createFakeList(size: Int): List<RoomListRoomSummary> {
        return mutableListOf<RoomListRoomSummary>().apply {
            repeat(size) {
                add(create("\$fakeRoom$it"))
            }
        }
    }
}
