/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.user.MatrixUser
import org.matrix.rustcomponents.sdk.RoomInfo

/**
 * Extract the heroes from the room info.
 * For now we only use heroes for direct rooms with 2 members.
 * Also we keep the heroes only if there is one single hero.
 */
fun RoomInfo.elementHeroes(): List<MatrixUser> {
    return heroes
        .takeIf { isDirect && activeMembersCount.toLong() == 2L }
        ?.takeIf { it.size == 1 }
        ?.map { it.map() }
        .orEmpty()
}
