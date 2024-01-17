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

import android.net.Uri
import io.element.android.features.createroom.impl.configureroom.RoomPrivacy
import io.element.android.features.createroom.impl.di.CreateRoomScope
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.di.SingleIn
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import javax.inject.Inject

@SingleIn(CreateRoomScope::class)
class CreateRoomDataStore @Inject constructor(
    val selectedUserListDataStore: UserListDataStore,
) {
    private val createRoomConfigFlow: MutableStateFlow<CreateRoomConfig> = MutableStateFlow(CreateRoomConfig())
    private var cachedAvatarUri: Uri? = null
        set(value) {
            field?.path?.let { File(it) }?.safeDelete()
            field = value
        }

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

    fun setAvatarUri(uri: Uri?, cached: Boolean = false) {
        cachedAvatarUri = uri.takeIf { cached }
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(avatarUri = uri))
    }

    fun setPrivacy(privacy: RoomPrivacy) {
        createRoomConfigFlow.tryEmit(createRoomConfigFlow.value.copy(privacy = privacy))
    }

    fun clearCachedData() {
        cachedAvatarUri = null
    }
}
