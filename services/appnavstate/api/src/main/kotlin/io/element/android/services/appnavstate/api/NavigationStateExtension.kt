/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId

fun NavigationState.currentSessionId(): SessionId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> sessionId
        is NavigationState.Space -> parentSession.sessionId
        is NavigationState.Room -> parentSpace.parentSession.sessionId
        is NavigationState.Thread -> parentRoom.parentSpace.parentSession.sessionId
    }
}

fun NavigationState.currentSpaceId(): SpaceId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> null
        is NavigationState.Space -> spaceId
        is NavigationState.Room -> parentSpace.spaceId
        is NavigationState.Thread -> parentRoom.parentSpace.spaceId
    }
}

fun NavigationState.currentRoomId(): RoomId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> null
        is NavigationState.Space -> null
        is NavigationState.Room -> roomId
        is NavigationState.Thread -> parentRoom.roomId
    }
}

fun NavigationState.currentThreadId(): ThreadId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> null
        is NavigationState.Space -> null
        is NavigationState.Room -> null
        is NavigationState.Thread -> threadId
    }
}
