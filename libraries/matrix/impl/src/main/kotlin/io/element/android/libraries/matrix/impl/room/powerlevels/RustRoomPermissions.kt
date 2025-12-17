/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions
import io.element.android.libraries.matrix.impl.room.map
import org.matrix.rustcomponents.sdk.RoomPowerLevels

class RustRoomPermissions(
    private val inner: RoomPowerLevels,
) : RoomPermissions {
    override fun canOwnUserBan(): Boolean {
        return inner.canOwnUserBan()
    }

    override fun canOwnUserInvite(): Boolean {
        return inner.canOwnUserInvite()
    }

    override fun canOwnUserKick(): Boolean {
        return inner.canOwnUserKick()
    }

    override fun canOwnUserPinUnpin(): Boolean {
        return inner.canOwnUserPinUnpin()
    }

    override fun canOwnUserRedactOther(): Boolean {
        return inner.canOwnUserRedactOther()
    }

    override fun canOwnUserRedactOwn(): Boolean {
        return inner.canOwnUserRedactOwn()
    }

    override fun canOwnUserSendMessage(message: MessageEventType): Boolean {
        return inner.canOwnUserSendMessage(message.map())
    }

    override fun canOwnUserSendState(stateEvent: StateEventType): Boolean {
        return inner.canOwnUserSendState(stateEvent.map())
    }

    override fun canOwnUserTriggerRoomNotification(): Boolean {
        return inner.canOwnUserTriggerRoomNotification()
    }

    override fun canUserBan(userId: UserId): Boolean {
        return inner.canUserBan(userId.value)
    }

    override fun canUserInvite(userId: UserId): Boolean {
        return inner.canUserInvite(userId.value)
    }

    override fun canUserKick(userId: UserId): Boolean {
        return inner.canUserKick(userId.value)
    }

    override fun canUserPinUnpin(userId: UserId): Boolean {
        return inner.canUserPinUnpin(userId.value)
    }

    override fun canUserRedactOther(userId: UserId): Boolean {
        return inner.canUserRedactOther(userId.value)
    }

    override fun canUserRedactOwn(userId: UserId): Boolean {
        return inner.canUserRedactOwn(userId.value)
    }

    override fun canUserSendMessage(userId: UserId, message: MessageEventType): Boolean {
        return inner.canUserSendMessage(userId.value, message.map())
    }

    override fun canUserSendState(userId: UserId, stateEvent: StateEventType): Boolean {
        return inner.canUserSendState(userId.value, stateEvent.map())
    }

    override fun canUserTriggerRoomNotification(userId: UserId): Boolean {
        return inner.canUserTriggerRoomNotification(userId.value)
    }

    override fun close() {
        inner.close()
    }
}
