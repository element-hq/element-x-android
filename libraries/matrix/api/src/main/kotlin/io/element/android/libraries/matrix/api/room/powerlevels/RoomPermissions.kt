/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Provides information about the permissions of users in a room.
 */
interface RoomPermissions : AutoCloseable {
    /**
     * Returns true if the current user is able to ban from the room.
     */
    fun canOwnUserBan(): Boolean

    /**
     * Returns true if the current user is able to invite in the room.
     */
    fun canOwnUserInvite(): Boolean

    /**
     * Returns true if the current user is able to kick from the room.
     */
    fun canOwnUserKick(): Boolean

    /**
     * Returns true if the current user is able to pin or unpin events in the
     * room.
     */
    fun canOwnUserPinUnpin(): Boolean

    /**
     * Returns true if the current user user is able to redact messages of
     * other users in the room.
     */
    fun canOwnUserRedactOther(): Boolean

    /**
     * Returns true if the current user is able to redact their own messages in
     * the room.
     */
    fun canOwnUserRedactOwn(): Boolean

    /**
     * Returns true if the current user is able to send a specific message type
     * in the room.
     */
    fun canOwnUserSendMessage(message: MessageEventType): Boolean

    /**
     * Returns true if the current user is able to send a specific state event
     * type in the room.
     */
    fun canOwnUserSendState(stateEvent: StateEventType): Boolean

    /**
     * Returns true if the current user is able to trigger a notification in
     * the room.
     */
    fun canOwnUserTriggerRoomNotification(): Boolean

    /**
     * Returns true if the user with the given userId is able to ban in the
     * room.
     */
    fun canUserBan(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to invite in the
     * room.
     */
    fun canUserInvite(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to kick in the
     * room.
     */
    fun canUserKick(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to pin or unpin
     * events in the room.
     */
    fun canUserPinUnpin(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to redact
     * messages of other users in the room.
     */
    fun canUserRedactOther(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to redact
     * their own messages in the room.
     */
    fun canUserRedactOwn(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to send a
     * specific message type in the room.
     */
    fun canUserSendMessage(userId: UserId, message: MessageEventType): Boolean

    /**
     * Returns true if the user with the given userId is able to send a
     * specific state event type in the room.
     */
    fun canUserSendState(userId: UserId, stateEvent: StateEventType): Boolean

    /**
     * Returns true if the user with the given userId is able to trigger a
     * notification in the room.
     *
     * The call may fail if there is an error in getting the power levels.
     */
    fun canUserTriggerRoomNotification(userId: UserId): Boolean
}

/**
 * Returns true if the current user can edit roles and permissions in the room ie. can send
 * a power levels state event.
 */
fun RoomPermissions.canEditRolesAndPermissions(): Boolean {
    return canOwnUserSendState(StateEventType.ROOM_POWER_LEVELS)
}

/**
 * Returns true if the current user can start a call in the room ie. can send
 * a call member state event.
 */
fun RoomPermissions.canCall(): Boolean {
    return canOwnUserSendState(StateEventType.CALL_MEMBER)
}

fun <T> Result<RoomPermissions>.use(default: T, block: (RoomPermissions) -> T): T {
    return fold(
        onSuccess = { perms ->
            perms.use(block)
        },
        onFailure = {
            default
        }
    )
}

fun <T> BaseRoom.permissionsFlow(default: T, block: (RoomPermissions) -> T): Flow<T> {
    return roomInfoFlow
        .map { info ->
            // If the user is a privileged creator, we return a constant hashcode to avoid recomputing permissions
            // each time the power levels change (as they have all permissions).
            if (info.privilegedCreatorRole && info.creators.contains(sessionId)) {
                Long.MAX_VALUE
            } else {
                info.roomPowerLevels?.hashCode() ?: 0L
            }
        }
        .distinctUntilChanged()
        .map {
            roomPermissions().use(default, block)
        }
}

@Composable
fun <T> BaseRoom.permissionsAsState(default: T, block: (RoomPermissions) -> T): State<T> {
    return remember(this, default, block) {
        permissionsFlow(default, block)
    }.collectAsState(default)
}
