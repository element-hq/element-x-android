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

import io.element.android.libraries.matrix.api.core.UserId

data class RoomMember(
    val userId: UserId,
    val displayName: String?,
    val avatarUrl: String?,
    val membership: RoomMembershipState,
    val isNameAmbiguous: Boolean,
    val powerLevel: Long,
    val normalizedPowerLevel: Long,
    val isIgnored: Boolean,
    val role: Role,
) {
    /**
     * Role of the RoomMember, based on its [powerLevel].
     */
    enum class Role(val powerLevel: Long) {
        ADMIN(100L),
        MODERATOR(50L),
        USER(0L);

        companion object {
            fun forPowerLevel(powerLevel: Long): Role {
                return when {
                    powerLevel >= ADMIN.powerLevel -> ADMIN
                    powerLevel >= MODERATOR.powerLevel -> MODERATOR
                    else -> USER
                }
            }
        }
    }

    /**
     * Disambiguated display name for the RoomMember.
     * If the display name is null, the user ID is returned.
     * If the display name is ambiguous, the user ID is appended in parentheses.
     * Otherwise, the display name is returned.
     */
    val disambiguatedDisplayName: String = when {
        displayName == null -> userId.value
        isNameAmbiguous -> "$displayName ($userId)"
        else -> displayName
    }
}

enum class RoomMembershipState {
    BAN,
    INVITE,
    JOIN,
    KNOCK,
    LEAVE
}

/**
 * Returns the best name value to display for the RoomMember.
 * If the [RoomMember.displayName] is present and not empty it'll be used, otherwise the [RoomMember.userId] will be used.
 */
fun RoomMember.getBestName(): String {
    return displayName?.takeIf { it.isNotEmpty() } ?: userId.value
}
