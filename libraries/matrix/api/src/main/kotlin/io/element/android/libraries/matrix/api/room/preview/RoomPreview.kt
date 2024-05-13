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

package io.element.android.libraries.matrix.api.room.preview

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomType

data class RoomPreview(
    /** The room id for this room. */
    val roomId: RoomId,
    /** The canonical alias for the room. */
    val canonicalAlias: RoomAlias?,
    /** The room's name, if set. */
    val name: String?,
    /** The room's topic, if set. */
    val topic: String?,
    /** The MXC URI to the room's avatar, if set. */
    val avatarUrl: String?,
    /** The number of joined members. */
    val numberOfJoinedMembers: Long,
    /** The room type (space, custom) or nothing, if it's a regular room. */
    val roomType: RoomType,
    /** Is the history world-readable for this room? */
    val isHistoryWorldReadable: Boolean,
    /** Is the room joined by the current user? */
    val isJoined: Boolean,
    /** Is the current user invited to this room? */
    val isInvited: Boolean,
    /** is the join rule public for this room? */
    val isPublic: Boolean,
    /** Can we knock (or restricted-knock) to this room? */
    val canKnock: Boolean,
)
