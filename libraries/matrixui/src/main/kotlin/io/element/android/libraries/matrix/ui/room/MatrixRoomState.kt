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

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.room.powerlevels.canSendMessage

@Composable
fun MatrixRoom.canSendMessageAsState(type: MessageEventType, updateKey: Long): State<Boolean> {
    return produceState(initialValue = true, key1 = updateKey) {
        value = canSendMessage(type).getOrElse { true }
    }
}

@Composable
fun MatrixRoom.canRedactOwnAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canRedactOwn().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canRedactOtherAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canRedactOther().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canCall(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canUserJoinCall(sessionId).getOrElse { false }
    }
}

@Composable
fun MatrixRoom.isOwnUserAdmin(): Boolean {
    val roomInfo by roomInfoFlow.collectAsState(initial = null)
    val powerLevel = roomInfo?.userPowerLevels?.get(sessionId) ?: 0L
    return RoomMember.Role.forPowerLevel(powerLevel) == RoomMember.Role.ADMIN
}
