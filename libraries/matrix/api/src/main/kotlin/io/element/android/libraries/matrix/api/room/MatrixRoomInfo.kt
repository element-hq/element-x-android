/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class MatrixRoomInfo(
    val id: RoomId,
    /** The room's name from the room state event if received from sync, or one that's been computed otherwise. */
    val name: String?,
    /** Room name as defined by the room state event only. */
    val rawName: String?,
    val topic: String?,
    val avatarUrl: String?,
    val isDirect: Boolean,
    val isPublic: Boolean,
    val isSpace: Boolean,
    val isTombstoned: Boolean,
    val isFavorite: Boolean,
    val canonicalAlias: RoomAlias?,
    val alternativeAliases: ImmutableList<RoomAlias>,
    val currentUserMembership: CurrentUserMembership,
    /**
     * Member who invited the current user to a room that's in the invited
     * state.
     *
     * Can be missing if the room membership invite event is missing from the
     * store.
     */
    val inviter: RoomMember?,
    val activeMembersCount: Long,
    val invitedMembersCount: Long,
    val joinedMembersCount: Long,
    val userPowerLevels: ImmutableMap<UserId, Long>,
    val highlightCount: Long,
    val notificationCount: Long,
    val userDefinedNotificationMode: RoomNotificationMode?,
    val hasRoomCall: Boolean,
    val activeRoomCallParticipants: ImmutableList<UserId>,
    val isMarkedUnread: Boolean,
    /**
     * "Interesting" messages received in that room, independently of the
     * notification settings.
     */
    val numUnreadMessages: Long,
    /**
     * Events that will notify the user, according to their
     * notification settings.
     */
    val numUnreadNotifications: Long,
    /**
     * Events causing mentions/highlights for the user, according to their
     * notification settings.
     */
    val numUnreadMentions: Long,
    val heroes: ImmutableList<MatrixUser>,
    val pinnedEventIds: ImmutableList<EventId>,
    val creator: UserId?,
) {
    val aliases: List<RoomAlias>
        get() = listOfNotNull(canonicalAlias) + alternativeAliases
}
