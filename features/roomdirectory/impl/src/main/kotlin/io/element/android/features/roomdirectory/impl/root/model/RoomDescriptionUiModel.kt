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

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription

data class RoomDescriptionUiModel(
    val roomId: RoomId,
    val name: String,
    val description: String,
    val avatarData: AvatarData,
    val canBeJoined: Boolean,
)

fun RoomDescription.toUiModel(): RoomDescriptionUiModel {
    return RoomDescriptionUiModel(
        roomId = roomId,
        name = name ?: "",
        description = topic ?: "",
        avatarData = AvatarData(
            id = roomId.value,
            name = name ?: "",
            url = avatarUrl,
            size = AvatarSize.RoomDirectoryItem,
        ),
        canBeJoined = joinRule == RoomDescription.JoinRule.PUBLIC,
    )
}
