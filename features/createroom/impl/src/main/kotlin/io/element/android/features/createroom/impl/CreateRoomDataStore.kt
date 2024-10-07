/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
