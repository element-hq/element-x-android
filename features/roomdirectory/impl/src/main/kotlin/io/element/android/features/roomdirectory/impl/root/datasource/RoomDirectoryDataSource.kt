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

package io.element.android.features.roomdirectory.impl.root.datasource

import io.element.android.features.roomdirectory.impl.root.model.RoomDirectoryRoomSummary
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RoomDirectoryDataSource @Inject constructor() {
    private val _searchResults = MutableStateFlow<ImmutableList<RoomDirectoryRoomSummary>>(persistentListOf())

    suspend fun updateSearchQuery(searchQuery: String) {
        //TODO branch to matrix sdk
        if (searchQuery.isEmpty()) {
            _searchResults.value = persistentListOf()
        } else {
            delay(100)
            _searchResults.value = all.value.filter {
                it.name.contains(searchQuery)
            }.toImmutableList()
        }
    }

    suspend fun loadMore() {
        //TODO branch to matrix sdk
    }


    val all: StateFlow<ImmutableList<RoomDirectoryRoomSummary>> = MutableStateFlow(
        persistentListOf(
            RoomDirectoryRoomSummary(
                roomId = RoomId("!exa:matrix.org"),
                name = "Element X Android",
                description = "Element X is a secure, private and decentralized messenger.",
                avatarData = AvatarData(
                    id = "!exa:matrix.org",
                    name = "Element X Android",
                    url = null,
                    size = AvatarSize.RoomDirectorySearchItem
                ),
                canBeJoined = true,
            ),
            RoomDirectoryRoomSummary(
                roomId = RoomId("!exi:matrix.org"),
                name = "Element X iOS",
                description = "Element X is a secure, private and decentralized messenger.",
                avatarData = AvatarData(
                    id = "!exi:matrix.org",
                    name = "Element X iOS",
                    url = null,
                    size = AvatarSize.RoomDirectorySearchItem
                ),
                canBeJoined = false,
            )
        )
    )
    val searchResults: StateFlow<ImmutableList<RoomDirectoryRoomSummary>> = _searchResults
}
