/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType

data class RoomPowerLevelsValues(
    val ban: Long,
    val invite: Long,
    val kick: Long,
    val sendEvents: Long,
    val redactEvents: Long,
    val roomName: Long,
    val roomAvatar: Long,
    val roomTopic: Long,
    val spaceChild: Long,
)

/**
 * Shortcut for calling [BaseRoom.canUserInvite] with our own user.
 */
suspend fun BaseRoom.canInvite(): Result<Boolean> = canUserInvite(sessionId)

/**
 * Shortcut for calling [BaseRoom.canUserKick] with our own user.
 */
suspend fun BaseRoom.canKick(): Result<Boolean> = canUserKick(sessionId)

/**
 * Shortcut for calling [BaseRoom.canUserBan] with our own user.
 */
suspend fun BaseRoom.canBan(): Result<Boolean> = canUserBan(sessionId)

/**
 * Shortcut for calling [BaseRoom.canUserSendState] with our own user.
 */
suspend fun BaseRoom.canSendState(type: StateEventType): Result<Boolean> = canUserSendState(sessionId, type)

/**
 * Shortcut for calling [BaseRoom.canUserSendMessage] with our own user.
 */
suspend fun BaseRoom.canSendMessage(type: MessageEventType): Result<Boolean> = canUserSendMessage(sessionId, type)

/**
 * Shortcut for calling [BaseRoom.canUserRedactOwn] with our own user.
 */
suspend fun BaseRoom.canRedactOwn(): Result<Boolean> = canUserRedactOwn(sessionId)

/**
 * Shortcut for calling [BaseRoom.canRedactOther] with our own user.
 */
suspend fun BaseRoom.canRedactOther(): Result<Boolean> = canUserRedactOther(sessionId)

/**
 * Shortcut for checking if current user can handle knock requests.
 */
suspend fun BaseRoom.canHandleKnockRequests(): Result<Boolean> = runCatchingExceptions {
    canInvite().getOrThrow() || canBan().getOrThrow() || canKick().getOrThrow()
}

/**
 * Shortcut for calling [BaseRoom.canUserPinUnpin] with our own user.
 */
suspend fun BaseRoom.canPinUnpin(): Result<Boolean> = canUserPinUnpin(sessionId)
