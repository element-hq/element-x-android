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

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Represent parsed data that the app has received from a Push content.
 *
 * @property eventId The Event ID. If not null, it will not be empty, and will have a valid format.
 * @property roomId The Room ID. If not null, it will not be empty, and will have a valid format.
 * @property unread Number of unread message.
 * @property clientSecret A client secret, used to determine which user should receive the notification.
 */
data class PushData(
        val eventId: EventId?,
        val roomId: RoomId?,
        val unread: Int?,
        val clientSecret: String?,
)
