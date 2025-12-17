/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.test

import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.services.appnavstate.api.NavigationState

const val A_SESSION_OWNER = "aSessionOwner"
const val A_SPACE_OWNER = "aSpaceOwner"
const val A_ROOM_OWNER = "aRoomOwner"
const val A_THREAD_OWNER = "aThreadOwner"

fun aNavigationState(
    sessionId: SessionId? = null,
    spaceId: SpaceId? = MAIN_SPACE,
    roomId: RoomId? = null,
    threadId: ThreadId? = null,
): NavigationState {
    if (sessionId == null) {
        return NavigationState.Root
    }
    val session = NavigationState.Session(A_SESSION_OWNER, sessionId)
    if (spaceId == null) {
        return session
    }
    val space = NavigationState.Space(A_SPACE_OWNER, spaceId, session)
    if (roomId == null) {
        return space
    }
    val room = NavigationState.Room(A_ROOM_OWNER, roomId, space)
    if (threadId == null) {
        return room
    }
    return NavigationState.Thread(A_THREAD_OWNER, threadId, room)
}
