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

package io.element.android.features.createroom.impl

import io.element.android.features.createroom.impl.configureroom.RoomPrivacy
import io.element.android.features.createroom.impl.di.CreateRoomScope
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.libraries.di.SingleIn
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@SingleIn(CreateRoomScope::class)
class CreateRoomDataStore @Inject constructor(
    val selectedUserListDataStore: UserListDataStore,
) {

    private val createRoomConfigFlow: MutableStateFlow<CreateRoomConfig> = MutableStateFlow(CreateRoomConfig())

    fun getCreateRoomConfig(): Flow<CreateRoomConfig> = combine(
        selectedUserListDataStore.selectedUsers(),
        createRoomConfigFlow,
    ) { selectedUsers, config ->
        config.copy(invites = selectedUsers.toImmutableList())
    }

    fun setRoomName(roomName: String?) {
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(roomName = roomName?.takeIf { it.isNotEmpty() }))
    }

    fun setTopic(topic: String?) {
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(topic = topic?.takeIf { it.isNotEmpty() }))
    }

    fun setAvatarUrl(avatarUrl: String?) {
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(avatarUrl = avatarUrl))
    }

    fun setPrivacy(privacy: RoomPrivacy?) {
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(privacy = privacy))
    }
}
