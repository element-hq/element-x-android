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

package io.element.android.services.appnavstate.test

import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.services.appnavstate.api.AppNavigationState

fun anAppNavigationState(
    sessionId: SessionId? = null,
    spaceId: SpaceId? = MAIN_SPACE,
    roomId: RoomId? = null,
    threadId: ThreadId? = null,
    owner: String = "a-owner",
): AppNavigationState {
    if (sessionId == null) {
        return AppNavigationState.Root
    }
    val session = AppNavigationState.Session(owner, sessionId)
    if (spaceId == null) {
        return session
    }
    val space = AppNavigationState.Space(owner, spaceId, session)
    if (roomId == null) {
        return space
    }
    val room = AppNavigationState.Room(owner, roomId, space)
    if (threadId == null) {
        return room
    }
    return AppNavigationState.Thread(owner, threadId, room)
}
