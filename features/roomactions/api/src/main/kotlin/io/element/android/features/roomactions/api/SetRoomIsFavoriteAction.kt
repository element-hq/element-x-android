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

package io.element.android.features.roomactions.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom

/**
 * Set the favorite status of a room.
 * This will update the notable tags of the room.
 */
interface SetRoomIsFavoriteAction {
    sealed interface Result {
        data object Success : Result
        data object RoomNotFound : Result
        data class Exception(val inner: java.lang.Exception) : Result
    }

    /**
     * Set the favorite status of a room by its id, it'll try to load the room from the session.
     */
    suspend operator fun invoke(roomId: RoomId, isFavorite: Boolean): Result

    /**
     * Set the favorite status of a room using the provided instance.
     */
    suspend operator fun invoke(room: MatrixRoom, isFavorite: Boolean): Result
}
