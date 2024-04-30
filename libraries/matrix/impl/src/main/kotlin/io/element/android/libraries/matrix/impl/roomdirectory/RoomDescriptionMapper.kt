/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule
import org.matrix.rustcomponents.sdk.RoomDescription as RustRoomDescription

class RoomDescriptionMapper {
    fun map(roomDescription: RustRoomDescription): RoomDescription {
        return RoomDescription(
            roomId = RoomId(roomDescription.roomId),
            name = roomDescription.name,
            topic = roomDescription.topic,
            avatarUrl = roomDescription.avatarUrl,
            alias = roomDescription.alias?.let(::RoomAlias),
            joinRule = when (roomDescription.joinRule) {
                PublicRoomJoinRule.PUBLIC -> RoomDescription.JoinRule.PUBLIC
                PublicRoomJoinRule.KNOCK -> RoomDescription.JoinRule.KNOCK
                null -> RoomDescription.JoinRule.UNKNOWN
            },
            isWorldReadable = roomDescription.isWorldReadable,
            numberOfMembers = roomDescription.joinedMembers.toLong(),
        )
    }
}
