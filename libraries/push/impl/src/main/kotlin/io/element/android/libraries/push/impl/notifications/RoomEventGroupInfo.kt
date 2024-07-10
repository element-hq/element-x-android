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

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Data class to hold information about a group of notifications for a room.
 */
data class RoomEventGroupInfo(
    val sessionId: SessionId,
    val roomId: RoomId,
    val roomDisplayName: String,
    val isDm: Boolean = false,
    // An event in the list has not yet been display
    val hasNewEvent: Boolean = false,
    // true if at least one on the not yet displayed event is noisy
    val shouldBing: Boolean = false,
    val customSound: String? = null,
    val hasSmartReplyError: Boolean = false,
    val isUpdated: Boolean = false,
)
