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

package io.element.android.libraries.matrix.test.roomdirectory

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import io.element.android.libraries.matrix.test.A_ROOM_ID

fun aRoomDescription(
    roomId: RoomId = A_ROOM_ID,
    name: String? = null,
    topic: String? = null,
    alias: RoomAlias? = null,
    avatarUrl: String? = null,
    joinRule: RoomDescription.JoinRule = RoomDescription.JoinRule.UNKNOWN,
    isWorldReadable: Boolean = true,
    joinedMembers: Long = 2L
) = RoomDescription(
    roomId = roomId,
    name = name,
    topic = topic,
    alias = alias,
    avatarUrl = avatarUrl,
    joinRule = joinRule,
    isWorldReadable = isWorldReadable,
    numberOfMembers = joinedMembers
)
