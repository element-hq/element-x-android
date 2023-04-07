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

package io.element.android.libraries.push.impl.firebase

import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.core.asRoomId
import io.element.android.libraries.push.impl.push.PushData

/**
 * In this case, the format is:
 * <pre>
 * {
 *     "event_id":"$anEventId",
 *     "room_id":"!aRoomId",
 *     "unread":"1",
 *     "prio":"high",
 *     "cs":"<client_secret>"
 * }
 * </pre>
 * .
 */
data class PushDataFirebase(
    val eventId: String?,
    val roomId: String?,
    var unread: Int?,
    val clientSecret: String?
)

fun PushDataFirebase.toPushData(): PushData? {
    val safeEventId = eventId?.asEventId() ?: return null
    val safeRoomId = roomId?.asRoomId() ?: return null
    return PushData(
        eventId = safeEventId,
        roomId = safeRoomId,
        unread = unread,
        clientSecret = clientSecret,
    )
}
