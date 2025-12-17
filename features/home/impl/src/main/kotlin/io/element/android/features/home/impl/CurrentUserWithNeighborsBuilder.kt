/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.sessionstorage.api.SessionData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class CurrentUserWithNeighborsBuilder {
    /**
     * Build a list of [MatrixUser] containing the current user. If there are other sessions, the list
     * will contain 3 users, with the current user in the middle.
     * If there is only one other session, the list will contain twice the other user, to allow cycling.
     */
    fun build(
        matrixUser: MatrixUser,
        sessions: List<SessionData>,
    ): ImmutableList<MatrixUser> {
        // Sort by position to always have the same order (not depending on last account usage)
        return sessions.sortedBy { it.position }
            .map {
                if (it.userId == matrixUser.userId.value) {
                    // Always use the freshest profile for the current user
                    matrixUser
                } else {
                    // Use the data from the DB
                    MatrixUser(
                        userId = UserId(it.userId),
                        displayName = it.userDisplayName,
                        avatarUrl = it.userAvatarUrl,
                    )
                }
            }
            .let { sessionList ->
                // If the list has one item, there is no other session, return the list
                when (sessionList.size) {
                    // Can happen when the user signs out (?)
                    0 -> listOf(matrixUser)
                    1 -> sessionList
                    else -> {
                        // Create a list with extra item at the start and end if necessary to have the current user in the middle
                        // If the list is [A, B, C, D] and the current user is A we want to return [D, A, B]
                        // If the current user is B, we want to return [A, B, C]
                        // If the current user is C, we want to return [B, C, D]
                        // If the current user is D, we want to return [C, D, A]
                        // Special case: if there are only two users, we want to return [B, A, B] or [A, B, A] to allows cycling
                        // between the two users.
                        val currentUserIndex = sessionList.indexOfFirst { it.userId == matrixUser.userId }
                        when (currentUserIndex) {
                            // This can happen when the user signs out.
                            // In this case, just return a singleton list with the current user.
                            -1 -> listOf(matrixUser)
                            0 -> listOf(sessionList.last()) + sessionList.take(2)
                            sessionList.lastIndex -> sessionList.takeLast(2) + sessionList.first()
                            else -> sessionList.slice(currentUserIndex - 1..currentUserIndex + 1)
                        }
                    }
                }
            }
            .toImmutableList()
    }
}
