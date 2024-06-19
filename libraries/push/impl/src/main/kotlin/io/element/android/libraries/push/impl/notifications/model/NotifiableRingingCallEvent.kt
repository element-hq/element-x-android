/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallNotifyType
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

data class NotifiableRingingCallEvent(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    override val eventId: EventId,
    override val editedEventId: EventId?,
    override val description: String?,
    override val canBeReplaced: Boolean,
    override val isRedacted: Boolean,
    override val isUpdated: Boolean,
    val roomName: String?,
    val senderId: UserId,
    val senderDisambiguatedDisplayName: String?,
    val senderAvatarUrl: String?,
    val roomAvatarUrl: String? = null,
    val callNotifyType: CallNotifyType,
    val timestamp: Long,
) : NotifiableEvent {
    companion object {
        fun shouldRing(callNotifyType: CallNotifyType, timestamp: Long): Boolean {
            val timeout = 10.seconds.inWholeMilliseconds
            val elapsed = Instant.now().toEpochMilli() - timestamp
            // Only ring if the type is RING and the elapsed time is less than the timeout
            return callNotifyType == CallNotifyType.RING && elapsed < timeout
        }
    }
}
