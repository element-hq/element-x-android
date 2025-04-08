/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import android.net.Uri
import io.element.android.features.createroom.impl.configureroom.RoomAccess
import io.element.android.features.createroom.impl.configureroom.RoomAccessItem
import io.element.android.features.createroom.impl.configureroom.RoomAddress
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityItem
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityState
import io.element.android.features.createroom.impl.di.CreateRoomScope
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import java.io.File
import javax.inject.Inject

@SingleIn(CreateRoomScope::class)
class CreateRoomDataStore @Inject constructor(
    val selectedUserListDataStore: UserListDataStore,
    private val roomAliasHelper: RoomAliasHelper,
) {
    private val createRoomConfigFlow: MutableStateFlow<CreateRoomConfig> = MutableStateFlow(CreateRoomConfig())
    private var cachedAvatarUri: Uri? = null
        set(value) {
            field?.path?.let { File(it) }?.safeDelete()
            field = value
        }

    val createRoomConfigWithInvites: Flow<CreateRoomConfig> = combine(
        selectedUserListDataStore.selectedUsers,
        createRoomConfigFlow,
    ) { selectedUsers, config ->
        config.copy(invites = selectedUsers.toImmutableList())
    }

    fun setRoomName(roomName: String) {
        createRoomConfigFlow.getAndUpdate { config ->
            val newVisibility = when (config.roomVisibility) {
                is RoomVisibilityState.Public -> {
                    val roomAddress = config.roomVisibility.roomAddress
                    if (roomAddress is RoomAddress.AutoFilled || roomName.isEmpty()) {
                        val roomAliasName = roomAliasHelper.roomAliasNameFromRoomDisplayName(roomName)
                        config.roomVisibility.copy(
                            roomAddress = RoomAddress.AutoFilled(roomAliasName),
                        )
                    } else {
                        config.roomVisibility
                    }
                }
                else -> config.roomVisibility
            }
            config.copy(
                roomName = roomName.takeIf { it.isNotEmpty() },
                roomVisibility = newVisibility,
            )
        }
    }

    fun setTopic(topic: String) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(topic = topic.takeIf { it.isNotEmpty() })
        }
    }

    fun setAvatarUri(uri: Uri?, cached: Boolean = false) {
        cachedAvatarUri = uri.takeIf { cached }
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(avatarUri = uri)
        }
    }

    fun setRoomVisibility(visibility: RoomVisibilityItem) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                roomVisibility = when (visibility) {
                    RoomVisibilityItem.Private -> RoomVisibilityState.Private
                    RoomVisibilityItem.Public -> {
                        val roomAliasName = roomAliasHelper.roomAliasNameFromRoomDisplayName(config.roomName.orEmpty())
                        RoomVisibilityState.Public(
                            roomAddress = RoomAddress.AutoFilled(roomAliasName),
                            roomAccess = RoomAccess.Anyone,
                        )
                    }
                }
            )
        }
    }

    fun setRoomAddress(address: String) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                roomVisibility = when (config.roomVisibility) {
                    is RoomVisibilityState.Public -> {
                        val sanitizedAddress = address.lowercase()
                        config.roomVisibility.copy(roomAddress = RoomAddress.Edited(sanitizedAddress))
                    }
                    else -> config.roomVisibility
                }
            )
        }
    }

    fun setRoomAccess(access: RoomAccessItem) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                roomVisibility = when (config.roomVisibility) {
                    is RoomVisibilityState.Public -> {
                        when (access) {
                            RoomAccessItem.Anyone -> config.roomVisibility.copy(roomAccess = RoomAccess.Anyone)
                            RoomAccessItem.AskToJoin -> config.roomVisibility.copy(roomAccess = RoomAccess.Knocking)
                        }
                    }
                    else -> config.roomVisibility
                }
            )
        }
    }

    fun clearCachedData() {
        cachedAvatarUri = null
    }
}
