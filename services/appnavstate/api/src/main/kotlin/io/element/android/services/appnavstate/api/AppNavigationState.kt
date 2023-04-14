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

/**
 * Can represent the current global app navigation state.
 * @param owner mostly a Node identifier associated with the state.
 * We are using the owner parameter to check when calling onLeaving methods is still using the same owner than his companion onNavigate.
 * Why this is needed : for now we rely on lifecycle methods of the node, which are async.
 * If you navigate quickly between nodes, onCreate of the new node is called before onDestroy of the previous node.
 * So we assume if we don't get the same owner, we can skip the onLeaving action as we already replaced it.
 */
sealed class AppNavigationState(open val owner: String) {
    object Root : AppNavigationState("ROOT")

    data class Session(
        override val owner: String,
        val sessionId: SessionId,
    ) : AppNavigationState(owner)

    data class Space(
        override val owner: String,
        // Can be fake value, if no space is selected
        val spaceId: SpaceId,
        val parentSession: Session,
    ) : AppNavigationState(owner)

    data class Room(
        override val owner: String,
        val roomId: RoomId,
        val parentSpace: Space,
    ) : AppNavigationState(owner)

    data class Thread(
        override val owner: String,
        val threadId: ThreadId,
        val parentRoom: Room,
    ) : AppNavigationState(owner)
}
