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

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem

data class MatrixRoomInfo(
    val id: String,
    val name: String?,
    val topic: String?,
    val avatarUrl: String?,
    val isDirect: Boolean,
    val isPublic: Boolean,
    val isSpace: Boolean,
    val isTombstoned: Boolean,
    val canonicalAlias: String?,
    val alternativeAliases: List<String>,
    val currentUserMembership: CurrentUserMembership,
    val latestEvent: EventTimelineItem?,
    val inviter: RoomMember?,
    val activeMembersCount: Long,
    val invitedMembersCount: Long,
    val joinedMembersCount: Long,
    val highlightCount: Long,
    val notificationCount: Long,
    val userDefinedNotificationMode: RoomNotificationMode?,
    val hasRoomCall: Boolean,
    val activeRoomCallParticipants: List<String>
)
