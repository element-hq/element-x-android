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

package io.element.android.features.roomdirectory.api

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class RoomDescription(
    val roomId: RoomId,
    val name: String?,
    val alias: String?,
    val topic: String?,
    val avatarUrl: String?,
    val joinRule: JoinRule,
    val numberOfMembers: Long,
) : Parcelable {

    enum class JoinRule {
        PUBLIC,
        KNOCK,
        UNKNOWN
    }

    val computedName = name ?: alias ?: roomId.value

    val computedDescription: String
        get() {
            return when {
                topic != null -> topic
                name != null && alias != null -> alias
                name == null && alias == null -> ""
                else -> roomId.value
            }
        }

    fun canBeJoined() = joinRule == JoinRule.PUBLIC || joinRule == JoinRule.KNOCK

    fun avatarData(size: AvatarSize) = AvatarData(
        id = roomId.value,
        name = name,
        url = avatarUrl,
        size = size,
    )
}
