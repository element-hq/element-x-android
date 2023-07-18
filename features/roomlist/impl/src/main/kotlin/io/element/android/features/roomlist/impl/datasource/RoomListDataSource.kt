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

package io.element.android.features.roomlist.impl.datasource

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomListDataSource @Inject constructor(
    private val roomSummaryDataSource: RoomSummaryDataSource,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    private val _filter = MutableStateFlow("")
    private val _allRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())
    private val _filteredRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())

    fun launchIn(coroutineScope: CoroutineScope) {
        roomSummaryDataSource
            .allRooms()
            .onEach { roomSummaries ->
                _allRooms.value = if (roomSummaries.isEmpty()) {
                    RoomListRoomSummaryPlaceholders.createFakeList(16)
                } else {
                    mapRoomSummaries(roomSummaries)
                }.toImmutableList()
            }
            .launchIn(coroutineScope)

        combine(
            _filter,
            _allRooms
        ) { filterValue, allRoomsValue ->
            when {
                filterValue.isEmpty() -> emptyList()
                else -> allRoomsValue.filter { it.name.contains(filterValue, ignoreCase = true) }
            }.toImmutableList()
        }
            .onEach {
                _filteredRooms.value = it
            }.launchIn(coroutineScope)
    }

    fun updateFilter(filterValue: String) {
        _filter.value = filterValue
    }

    val filter: StateFlow<String> = _filter
    val allRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _allRooms
    val filteredRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _filteredRooms

    private suspend fun mapRoomSummaries(
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> = withContext(coroutineDispatchers.computation) {
        roomSummaries.map { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummaryPlaceholders.create(roomSummary.identifier)
                is RoomSummary.Filled -> {
                    val avatarData = AvatarData(
                        id = roomSummary.identifier(),
                        name = roomSummary.details.name,
                        url = roomSummary.details.avatarURLString,
                        size = AvatarSize.RoomListItem,
                    )
                    val roomIdentifier = roomSummary.identifier()
                    RoomListRoomSummary(
                        id = roomSummary.identifier(),
                        roomId = RoomId(roomIdentifier),
                        name = roomSummary.details.name,
                        hasUnread = roomSummary.details.unreadNotificationCount > 0,
                        timestamp = lastMessageTimestampFormatter.format(roomSummary.details.lastMessageTimestamp),
                        lastMessage = roomSummary.details.lastMessage?.let { message ->
                            roomLastMessageFormatter.format(message.event, roomSummary.details.isDirect)
                        }.orEmpty(),
                        avatarData = avatarData,
                    )
                }
            }
        }
    }
}
