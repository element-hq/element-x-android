/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.Membership as RustMembership
import org.matrix.rustcomponents.sdk.RoomInfo as RustRoomInfo
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode

class MatrixRoomInfoMapper(
    private val timelineItemMapper: EventTimelineItemMapper = EventTimelineItemMapper(),
) {
    fun map(rustRoomInfo: RustRoomInfo): MatrixRoomInfo = rustRoomInfo.use {
        return MatrixRoomInfo(
            id = it.id,
            name = it.name,
            topic = it.topic,
            avatarUrl = it.avatarUrl,
            isDirect = it.isDirect,
            isPublic = it.isPublic,
            isSpace = it.isSpace,
            isTombstoned = it.isTombstoned,
            isFavorite = it.isFavourite,
            canonicalAlias = it.canonicalAlias,
            alternativeAliases = it.alternativeAliases.toImmutableList(),
            currentUserMembership = it.membership.map(),
            latestEvent = it.latestEvent?.use(timelineItemMapper::map),
            inviter = it.inviter?.use(RoomMemberMapper::map),
            activeMembersCount = it.activeMembersCount.toLong(),
            invitedMembersCount = it.invitedMembersCount.toLong(),
            joinedMembersCount = it.joinedMembersCount.toLong(),
            highlightCount = it.highlightCount.toLong(),
            notificationCount = it.notificationCount.toLong(),
            userDefinedNotificationMode = it.userDefinedNotificationMode?.map(),
            hasRoomCall = it.hasRoomCall,
            activeRoomCallParticipants = it.activeRoomCallParticipants.toImmutableList()
        )
    }
}

fun RustMembership.map(): CurrentUserMembership = when (this) {
    RustMembership.INVITED -> CurrentUserMembership.INVITED
    RustMembership.JOINED -> CurrentUserMembership.JOINED
    RustMembership.LEFT -> CurrentUserMembership.LEFT
}

fun RustRoomNotificationMode.map(): RoomNotificationMode = when (this) {
    RustRoomNotificationMode.ALL_MESSAGES -> RoomNotificationMode.ALL_MESSAGES
    RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
    RustRoomNotificationMode.MUTE -> RoomNotificationMode.MUTE
}
