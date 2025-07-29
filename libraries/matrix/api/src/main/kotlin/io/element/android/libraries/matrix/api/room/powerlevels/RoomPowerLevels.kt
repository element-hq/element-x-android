/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableMap

/**
 * Represents the power levels in a Matrix room, containing both the levels needed to perform actions and the custom power levels for users.
 *
 * **WARNING**: this won't contain the power level of the room creators, as it is not stored in the power levels event. The `users` property is private to
 * enforce this restriction and try to avoid using this property directly to check if a user has a certain role.
 * Use the [usersWithRole] or [roleOf] methods instead, and never for creators, that logic should be handled separately.
 */
data class RoomPowerLevels(
    /**
     * The power levels required to perform various actions in the room.
     */
    val values: RoomPowerLevelsValues,
    private val users: ImmutableMap<UserId, Long>,
) {
    /**
     * Returns the power level of the user in the room.
     *
     * If the user is not found, returns 0.
     */
    fun powerLevelOf(userId: UserId): Long {
        return users[userId] ?: 0L
    }

    /**
     * Returns the set of [UserId]s that have the given role in the room.
     *
     * **WARNING**: This method must not be used with a creator role. It'll result in a runtime error.
     */
    fun usersWithRole(role: RoomMember.Role): Set<UserId> {
        return if (role is RoomMember.Role.Owner && role.isCreator) {
            error("RoomPowerLevels.usersWithRole should not be used with a creator role, use roomInfo.creators instead")
        } else {
            users.filterValues { RoomMember.Role.forPowerLevel(it) == role }.keys
        }
    }

    /**
     * Returns the role of the user in the room based on their power level.
     * If the user is not found, returns null.
     *
     * **WARNING**: This method must not be used with a creator role, as it won't return any results.
     */
    fun roleOf(userId: UserId): RoomMember.Role? {
        return users[userId]?.let(RoomMember.Role::forPowerLevel)
    }
}
