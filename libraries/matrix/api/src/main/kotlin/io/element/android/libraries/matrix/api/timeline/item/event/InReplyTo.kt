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

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface InReplyTo {
    /** The event details are not loaded yet. We can fetch them. */
    data class NotLoaded(val eventId: EventId) : InReplyTo

    /** The event details are pending to be fetched. We should **not** fetch them again. */
    data object Pending : InReplyTo

    /** The event details are available. */
    data class Ready(
        val eventId: EventId,
        val content: EventContent,
        val senderId: UserId,
        val senderDisplayName: String?,
        val senderAvatarUrl: String?,
    ) : InReplyTo

    /**
     * Fetching the event details failed.
     *
     * We can try to fetch them again **with a proper retry strategy**, but not blindly:
     *
     * If the reason for the failure is consistent on the server, we'd enter a loop
     * where we keep trying to fetch the same event.
     * */
    data object Error : InReplyTo
}
