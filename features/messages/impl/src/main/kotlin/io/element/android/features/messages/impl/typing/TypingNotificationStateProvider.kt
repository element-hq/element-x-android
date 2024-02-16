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
    )
}
