/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.core.bool.orFalse
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Method to filter members by userId or displayName.
 * It does filter through the already known members, it doesn't perform additional requests.
 */
suspend fun BaseRoom.filterMembers(query: String, coroutineContext: CoroutineContext): List<RoomMember> = withContext(coroutineContext) {
    val roomMembersState = membersStateFlow.value
    val activeRoomMembers = roomMembersState.roomMembers()
        ?.filter { it.membership.isActive() }
        .orEmpty()
    val filteredMembers = if (query.isBlank()) {
        activeRoomMembers
    } else {
        activeRoomMembers.filter { member ->
            member.userId.value.contains(query, ignoreCase = true) ||
                member.displayName?.contains(query, ignoreCase = true).orFalse()
        }
    }
    filteredMembers
}
