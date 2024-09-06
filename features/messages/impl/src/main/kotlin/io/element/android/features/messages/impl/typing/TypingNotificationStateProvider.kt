/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.toImmutableList

class TypingNotificationStateProvider : PreviewParameterProvider<TypingNotificationState> {
    override val values: Sequence<TypingNotificationState>
        get() = sequenceOf(
            aTypingNotificationState(),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice", isNameAmbiguous = true),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice"),
                    aTypingRoomMember(displayName = "Bob"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice"),
                    aTypingRoomMember(displayName = "Bob"),
                    aTypingRoomMember(displayName = "Charlie"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice"),
                    aTypingRoomMember(displayName = "Bob"),
                    aTypingRoomMember(displayName = "Charlie"),
                    aTypingRoomMember(displayName = "Dan"),
                    aTypingRoomMember(displayName = "Eve"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(displayName = "Alice with a very long display name which means that it will be truncated"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = emptyList(),
                reserveSpace = true,
            ),
        )
}

internal fun aTypingNotificationState(
    typingMembers: List<RoomMember> = emptyList(),
    reserveSpace: Boolean = false,
) = TypingNotificationState(
    renderTypingNotifications = true,
    typingMembers = typingMembers.toImmutableList(),
    reserveSpace = reserveSpace,
)

internal fun aTypingRoomMember(
    userId: UserId = UserId("@alice:example.com"),
    displayName: String? = null,
    isNameAmbiguous: Boolean = false,
): RoomMember {
    return RoomMember(
        userId = userId,
        displayName = displayName,
        avatarUrl = null,
        membership = RoomMembershipState.JOIN,
        isNameAmbiguous = isNameAmbiguous,
        powerLevel = 0,
        normalizedPowerLevel = 0,
        isIgnored = false,
        role = RoomMember.Role.USER,
    )
}
