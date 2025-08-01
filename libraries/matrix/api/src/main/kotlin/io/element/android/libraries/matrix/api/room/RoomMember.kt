/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
    val membershipChangeReason: String?,
) {
    /**
     * Role of the RoomMember, based on its [powerLevel].
     */
    sealed interface Role {
        data class Owner(val isCreator: Boolean) : Role
        data object Admin : Role
        data object Moderator : Role
        data object User : Role

        val powerLevel: Long
            get() = when (this) {
                is Owner -> if (isCreator) CREATOR_POWERLEVEL else SUPERADMIN_POWERLEVEL
                Admin -> ADMIN_POWERLEVEL
                Moderator -> MODERATOR_POWERLEVEL
                User -> USER_POWERLEVEL
            }

        companion object {
            private const val CREATOR_POWERLEVEL = Long.MAX_VALUE
            private const val SUPERADMIN_POWERLEVEL = 150L
            private const val ADMIN_POWERLEVEL = 100L
            private const val MODERATOR_POWERLEVEL = 50L
            private const val USER_POWERLEVEL = 0L

            fun forPowerLevel(powerLevel: Long): Role {
                return when {
                    powerLevel == CREATOR_POWERLEVEL -> Owner(isCreator = true)
                    powerLevel >= SUPERADMIN_POWERLEVEL -> Owner(isCreator = false)
                    powerLevel >= ADMIN_POWERLEVEL -> Admin
                    powerLevel >= MODERATOR_POWERLEVEL -> Moderator
                    else -> User
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
