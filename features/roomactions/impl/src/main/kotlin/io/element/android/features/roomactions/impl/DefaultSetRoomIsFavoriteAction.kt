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

package io.element.android.features.roomactions.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomactions.api.SetRoomIsFavoriteAction
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@ContributesBinding(SessionScope::class)
class DefaultSetRoomIsFavoriteAction @Inject constructor(private val client: MatrixClient) : SetRoomIsFavoriteAction {

    override suspend operator fun invoke(roomId: RoomId, isFavorite: Boolean): SetRoomIsFavoriteAction.Result {
        return client.getRoom(roomId)?.use { room ->
            invoke(room, isFavorite)
        } ?: SetRoomIsFavoriteAction.Result.RoomNotFound
    }

    override suspend fun invoke(room: MatrixRoom, isFavorite: Boolean): SetRoomIsFavoriteAction.Result {
        return room.setIsFavorite(isFavorite).fold(
            onSuccess = {
                SetRoomIsFavoriteAction.Result.Success
            },
            onFailure = { throwable ->
                if (throwable is Exception && throwable !is CancellationException) {
                    SetRoomIsFavoriteAction.Result.Exception(throwable)
                } else {
                    throw throwable
                }
            }
        )
    }

}
