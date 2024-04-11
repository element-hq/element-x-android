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

package io.element.android.features.roomdirectory.impl.root.model

import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription as MatrixRoomDescription

fun MatrixRoomDescription.toFeatureModel(): RoomDescription {
    fun name(): String {
        return name ?: alias ?: roomId.value
    }

    fun description(): String {
        val topic = topic
        val alias = alias
        val name = name
        return when {
            topic != null -> topic
            name != null && alias != null -> alias
            name == null && alias == null -> ""
            else -> roomId.value
        }
    }

    return RoomDescription(
        roomId = roomId,
        name = name(),
        description = description(),
        avatarUrl = avatarUrl,
        numberOfMembers = numberOfMembers,
        joinRule = when (joinRule) {
            MatrixRoomDescription.JoinRule.PUBLIC -> RoomDescription.JoinRule.PUBLIC
            MatrixRoomDescription.JoinRule.KNOCK -> RoomDescription.JoinRule.KNOCK
            MatrixRoomDescription.JoinRule.UNKNOWN -> RoomDescription.JoinRule.UNKNOWN
        }
    )
}
