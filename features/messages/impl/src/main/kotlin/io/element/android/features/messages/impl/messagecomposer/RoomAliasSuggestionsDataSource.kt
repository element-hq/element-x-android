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

package io.element.android.features.messages.impl.messagecomposer

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class RoomAliasSuggestion(
    val roomAlias: RoomAlias,
    val roomSummary: RoomSummary,
)

interface RoomAliasSuggestionsDataSource {
    fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>>
}

@ContributesBinding(SessionScope::class)
class DefaultRoomAliasSuggestionsDataSource @Inject constructor(
    private val roomListService: RoomListService,
) : RoomAliasSuggestionsDataSource {
    override fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>> {
        return roomListService
            .allRooms
            .filteredSummaries
            .map { roomSummaries ->
                roomSummaries
                    .mapNotNull { roomSummary ->
                        roomSummary.canonicalAlias?.let { roomAlias ->
                            RoomAliasSuggestion(
                                roomAlias = roomAlias,
                                roomSummary = roomSummary,
                            )
                        }
                    }
            }
    }
}

