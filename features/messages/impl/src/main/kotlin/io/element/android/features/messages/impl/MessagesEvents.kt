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

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId

sealed interface MessagesEvents {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : MessagesEvents
    data class ToggleReaction(val reaction: String, val eventId: EventId) : MessagesEvents
    data class InviteDialogDismissed(val action: InviteDialogAction) : MessagesEvents
    data object Dismiss : MessagesEvents
}

enum class InviteDialogAction {
    Cancel,
    Invite,
}
