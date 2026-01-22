/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.io.File

@Inject
class CreateRoomConfigStore(
    private val roomAliasHelper: RoomAliasHelper,
) {
    private val createRoomConfigFlow: MutableStateFlow<CreateRoomConfig> = MutableStateFlow(CreateRoomConfig())

    private var cachedAvatarUri: Uri? = null
        set(value) {
            field?.path?.let { File(it) }?.safeDelete()
            field = value
        }

    fun getCreateRoomConfigFlow(): StateFlow<CreateRoomConfig> = createRoomConfigFlow

    fun setRoomName(roomName: String) {
        createRoomConfigFlow.getAndUpdate { config ->
            val roomAccessWithNewAddress = if (config.visibilityState is RoomVisibilityState.Public) {
                val roomAddress = config.visibilityState.roomAddress
                if (roomAddress is RoomAddress.AutoFilled || roomName.isEmpty()) {
                    val roomAliasName = roomAliasHelper.roomAliasNameFromRoomDisplayName(roomName)
                    config.visibilityState.copy(roomAddress = RoomAddress.AutoFilled(roomAliasName))
                } else {
                    config.visibilityState
                }
            } else {
                config.visibilityState
            }
            config.copy(
                roomName = roomName.takeIf { it.isNotEmpty() },
                visibilityState = roomAccessWithNewAddress,
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
            config.copy(avatarUri = uri?.toString())
        }
    }

    /**
     * Sets both the room visibility and its access based on the provided join rule.
     */
    fun setJoinRule(joinRule: JoinRuleItem) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                visibilityState = when (joinRule) {
                    JoinRuleItem.Private -> RoomVisibilityState.Private()
                    is JoinRuleItem.PublicVisibility -> {
                        val roomAliasName = roomAliasHelper.roomAliasNameFromRoomDisplayName(config.roomName.orEmpty())
                        RoomVisibilityState.Public(
                            roomAddress = RoomAddress.AutoFilled(roomAliasName),
                            joinRuleItem = joinRule,
                        )
                    }
                }
            )
        }
    }

    fun setRoomAddress(address: String) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                visibilityState = when (config.visibilityState) {
                    is RoomVisibilityState.Public -> {
                        val sanitizedAddress = address.lowercase()
                        config.visibilityState.copy(roomAddress = RoomAddress.Edited(sanitizedAddress))
                    }
                    else -> config.visibilityState
                }
            )
        }
    }

    fun setIsSpace(isSpace: Boolean) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(isSpace = isSpace)
        }
    }

    fun setParentSpace(parentSpace: SpaceRoom?) {
        createRoomConfigFlow.getAndUpdate { config ->
            config.copy(
                parentSpace = parentSpace,
                visibilityState = RoomVisibilityState.Private(),
            )
        }
    }

    fun clearCachedData() {
        cachedAvatarUri = null
    }
}
