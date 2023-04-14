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

fun AppNavigationState.currentSessionId(): SessionId? {
    return when (this) {
        AppNavigationState.Root -> null
        is AppNavigationState.Session -> sessionId
        is AppNavigationState.Space -> parentSession.sessionId
        is AppNavigationState.Room -> parentSpace.parentSession.sessionId
        is AppNavigationState.Thread -> parentRoom.parentSpace.parentSession.sessionId
    }
}

fun AppNavigationState.currentSpaceId(): SpaceId? {
    return when (this) {
        AppNavigationState.Root -> null
        is AppNavigationState.Session -> null
        is AppNavigationState.Space -> spaceId
        is AppNavigationState.Room -> parentSpace.spaceId
        is AppNavigationState.Thread -> parentRoom.parentSpace.spaceId
    }
}

fun AppNavigationState.currentRoomId(): RoomId? {
    return when (this) {
        AppNavigationState.Root -> null
        is AppNavigationState.Session -> null
        is AppNavigationState.Space -> null
        is AppNavigationState.Room -> roomId
        is AppNavigationState.Thread -> parentRoom.roomId
    }
}

fun AppNavigationState.currentThreadId(): ThreadId? {
    return when (this) {
        AppNavigationState.Root -> null
        is AppNavigationState.Session -> null
        is AppNavigationState.Space -> null
        is AppNavigationState.Room -> null
        is AppNavigationState.Thread -> threadId
    }
}
