/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.user.MatrixUser

open class RoomSummaryDetailsProvider : PreviewParameterProvider<RoomSummary> {
    override val values: Sequence<RoomSummary>
        get() = sequenceOf(
            aRoomSummaryDetails(),
            aRoomSummaryDetails(name = null),
        )
}

fun aRoomSummaryDetails(
    roomId: RoomId = RoomId("!room:domain"),
    name: String? = "roomName",
    canonicalAlias: RoomAlias? = null,
    alternativeAliases: List<RoomAlias> = emptyList(),
    isDirect: Boolean = true,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = null,
    inviter: RoomMember? = null,
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    isDm: Boolean = false,
    numUnreadMentions: Int = 0,
    numUnreadMessages: Int = 0,
    numUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    isFavorite: Boolean = false,
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    heroes: List<MatrixUser> = emptyList(),
) = RoomSummary(
    roomId = roomId,
    name = name,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    inviter = inviter,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
    numUnreadMentions = numUnreadMentions,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    isFavorite = isFavorite,
    currentUserMembership = currentUserMembership,
    heroes = heroes,
)
