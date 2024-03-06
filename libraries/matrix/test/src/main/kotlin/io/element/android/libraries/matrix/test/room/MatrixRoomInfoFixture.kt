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

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap

fun aMatrixRoomInfo(
    id: String = A_ROOM_ID.value,
    name: String? = A_ROOM_NAME,
    topic: String? = null,
    avatarUrl: String? = null,
    isDirect: Boolean = false,
    isPublic: Boolean = true,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
    isFavorite: Boolean = false,
    canonicalAlias: String? = null,
    alternativeAliases: ImmutableList<String> = persistentListOf(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    latestEvent: EventTimelineItem? = null,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 1L,
    invitedMembersCount: Long = 0L,
    joinedMembersCount: Long = 1L,
    highlightCount: Long = 0L,
    notificationCount: Long = 0L,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    userPowerLevels: Map<UserId, Long> = emptyMap(),
    activeRoomCallParticipants: ImmutableList<String> = persistentListOf(),
) = MatrixRoomInfo(
    id = id,
    name = name,
    topic = topic,
    avatarUrl = avatarUrl,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isTombstoned = isTombstoned,
    isFavorite = isFavorite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    currentUserMembership = currentUserMembership,
    latestEvent = latestEvent,
    inviter = inviter,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    userPowerLevels = userPowerLevels.toPersistentMap(),
    activeRoomCallParticipants = activeRoomCallParticipants
)
