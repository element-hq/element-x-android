/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType

data class MatrixRoomPowerLevels(
    val ban: Long,
    val invite: Long,
    val kick: Long,
    val sendEvents: Long,
    val redactEvents: Long,
    val roomName: Long,
    val roomAvatar: Long,
    val roomTopic: Long,
)

/**
 * Shortcut for calling [MatrixRoom.canUserInvite] with our own user.
 */
suspend fun MatrixRoom.canInvite(): Result<Boolean> = canUserInvite(sessionId)

/**
 * Shortcut for calling [MatrixRoom.canUserKick] with our own user.
 */
suspend fun MatrixRoom.canKick(): Result<Boolean> = canUserKick(sessionId)

/**
 * Shortcut for calling [MatrixRoom.canUserBan] with our own user.
 */
suspend fun MatrixRoom.canBan(): Result<Boolean> = canUserBan(sessionId)

/**
 * Shortcut for calling [MatrixRoom.canUserSendState] with our own user.
 */
suspend fun MatrixRoom.canSendState(type: StateEventType): Result<Boolean> = canUserSendState(sessionId, type)

/**
 * Shortcut for calling [MatrixRoom.canUserSendMessage] with our own user.
 */
suspend fun MatrixRoom.canSendMessage(type: MessageEventType): Result<Boolean> = canUserSendMessage(sessionId, type)

/**
 * Shortcut for calling [MatrixRoom.canUserRedactOwn] with our own user.
 */
suspend fun MatrixRoom.canRedactOwn(): Result<Boolean> = canUserRedactOwn(sessionId)

/**
 * Shortcut for calling [MatrixRoom.canRedactOther] with our own user.
 */
suspend fun MatrixRoom.canRedactOther(): Result<Boolean> = canUserRedactOther(sessionId)

/**
 * Shortcut for calling [MatrixRoom.canUserPinUnpin] with our own user.
 */
suspend fun MatrixRoom.canPinUnpin(): Result<Boolean> = canUserPinUnpin(sessionId)
