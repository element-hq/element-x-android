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

import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.RoomId

@Immutable
data class RoomListRoomSummary(
    val id: String,
    val roomId: RoomId = RoomId(id),
    val name: String = "",
    val hasUnread: Boolean = false,
    val timestamp: String? = null,
    val lastMessage: CharSequence? = null,
    val avatarData: AvatarData = AvatarData(id, name),
    val isPlaceholder: Boolean = false,
)
