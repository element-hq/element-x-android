/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

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

    val displayNameOrDefault: String
        get() = when {
            displayName == null -> userId.extractedDisplayName
            else -> displayName
        }
}

enum class RoomMembershipState {
    BAN,
    INVITE,
    JOIN,
    KNOCK,
    LEAVE;

    fun isActive(): Boolean = this == JOIN || this == INVITE
}

/**
 * Returns the best name value to display for the RoomMember.
 * If the [RoomMember.displayName] is present and not empty it'll be used, otherwise the [RoomMember.userId] will be used.
 */
fun RoomMember.getBestName(): String {
    return displayName?.takeIf { it.isNotEmpty() } ?: userId.value
}

fun RoomMember.toMatrixUser() = MatrixUser(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
)
