/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class FakeRoomPermissions(
    private val canBan: Boolean = false,
    private val canInvite: Boolean = false,
    private val canKick: Boolean = false,
    private val canPinUnpin: Boolean = false,
    private val canRedactOther: Boolean = false,
    private val canRedactOwn: Boolean = false,
    private val canTriggerRoomNotification: Boolean = false,
    private val canSendMessage: (MessageEventType) -> Boolean = { false },
    private val canSendState: (StateEventType) -> Boolean = { false },
    private val canUserBan: (UserId) -> Boolean = { false },
    private val canUserInvite: (UserId) -> Boolean = { false },
    private val canUserKick: (UserId) -> Boolean = { false },
    private val canUserPinUnpin: (UserId) -> Boolean = { false },
    private val canUserRedactOther: (UserId) -> Boolean = { false },
    private val canUserRedactOwn: (UserId) -> Boolean = { false },
    private val canUserTriggerRoomNotification: (UserId) -> Boolean = { false },
    private val canUserSendMessage: (UserId, MessageEventType) -> Boolean = { _, _ -> false },
    private val canUserSendState: (UserId, StateEventType) -> Boolean = { _, _ -> false },
) : RoomPermissions {
    override fun canOwnUserBan(): Boolean = canBan
    override fun canOwnUserInvite(): Boolean = canInvite
    override fun canOwnUserKick(): Boolean = canKick
    override fun canOwnUserPinUnpin(): Boolean = canPinUnpin
    override fun canOwnUserRedactOther(): Boolean = canRedactOther
    override fun canOwnUserRedactOwn(): Boolean = canRedactOwn
    override fun canOwnUserSendMessage(message: MessageEventType): Boolean = canSendMessage(message)
    override fun canOwnUserSendState(stateEvent: StateEventType): Boolean = canSendState(stateEvent)

    override fun canOwnUserTriggerRoomNotification(): Boolean = canTriggerRoomNotification
    override fun canUserBan(userId: UserId): Boolean = canUserBan(userId)
    override fun canUserInvite(userId: UserId): Boolean = canUserInvite(userId)
    override fun canUserKick(userId: UserId): Boolean = canUserKick(userId)
    override fun canUserPinUnpin(userId: UserId): Boolean = canUserPinUnpin(userId)
    override fun canUserRedactOther(userId: UserId): Boolean = canUserRedactOther(userId)
    override fun canUserRedactOwn(userId: UserId): Boolean = canUserRedactOwn(userId)
    override fun canUserSendMessage(userId: UserId, message: MessageEventType): Boolean = canUserSendMessage(userId, message)
    override fun canUserSendState(userId: UserId, stateEvent: StateEventType): Boolean = canUserSendState(userId, stateEvent)
    override fun canUserTriggerRoomNotification(userId: UserId): Boolean = canUserTriggerRoomNotification(userId)

    override fun close() {
        // no-op for the fake
    }
}
