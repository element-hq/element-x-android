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

package io.element.android.libraries.matrix.api.room.navigation

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.MatrixRoom

/**
 * Return true if the given roomIdOrAlias is the same room as this room.
 */
fun MatrixRoom.isSameRoom(roomIdOrAlias: RoomIdOrAlias): Boolean {
    return when (roomIdOrAlias) {
        is RoomIdOrAlias.Id -> {
            roomIdOrAlias.roomId == roomId
        }
        is RoomIdOrAlias.Alias -> {
            roomIdOrAlias.roomAlias == alias || roomIdOrAlias.roomAlias in alternativeAliases
        }
    }
}
