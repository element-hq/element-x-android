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

package io.element.android.libraries.push.impl.unifiedpush

import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.core.asRoomId
import io.element.android.libraries.push.impl.push.PushData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * In this case, the format is:
 * <pre>
 * {
 *     "notification":{
 *         "event_id":"$anEventId",
 *         "room_id":"!aRoomId",
 *         "counts":{
 *             "unread":1
 *         },
 *         "prio":"high"
 *     }
 * }
 * </pre>
 * .
 */
@Serializable
data class PushDataUnifiedPush(
    val notification: PushDataUnifiedPushNotification? = null
)

@Serializable
data class PushDataUnifiedPushNotification(
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("counts") var counts: PushDataUnifiedPushCounts? = null,
)

@Serializable
data class PushDataUnifiedPushCounts(
    @SerialName("unread") val unread: Int? = null
)

fun PushDataUnifiedPush.toPushData() = PushData(
    eventId = notification?.eventId?.takeIf { MatrixPatterns.isEventId(it) }?.asEventId(),
    roomId = notification?.roomId?.takeIf { MatrixPatterns.isRoomId(it) }?.asRoomId(),
    unread = notification?.counts?.unread,
    clientSecret = null // TODO EAx check how client secret will be sent through UnifiedPush
)
