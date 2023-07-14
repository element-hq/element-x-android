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
