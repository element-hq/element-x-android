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

package io.element.android.libraries.matrix.api.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

data class NotificationData(
    val senderId: UserId,
    val eventId: EventId,
    val roomId: RoomId,
    val senderAvatarUrl: String?,
    val senderDisplayName: String?,
    val roomAvatarUrl: String?,
    val roomDisplayName: String?,
    val isDirect: Boolean,
    val isEncrypted: Boolean,
    val isNoisy: Boolean,
    val event: NotificationEvent,
)

data class NotificationEvent(
    val eventId: EventId,
    val senderId: UserId,
    val timestamp: Long,
    val content: String,
    // For images for instance
    val contentUrl: String?
)
