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
 * Shortcut for calling [MatrixRoom.canBanUser] with our own user.
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
