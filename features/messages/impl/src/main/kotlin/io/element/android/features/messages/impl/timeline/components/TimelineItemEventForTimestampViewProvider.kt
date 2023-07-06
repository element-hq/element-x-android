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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState

class TimelineItemEventForTimestampViewProvider : PreviewParameterProvider<TimelineItem.Event> {
    override val values: Sequence<TimelineItem.Event>
        get() = sequenceOf(
            aTimelineItemEvent(),
            // Sending failed
            aTimelineItemEvent().copy(localSendState = LocalEventSendState.SendingFailed("AN_ERROR")),
            // Edited
            aTimelineItemEvent().copy(content = aTimelineItemTextContent().copy(isEdited = true)),
            // Sending failed + Edited (not sure this is possible IRL, but should be covered by test)
            aTimelineItemEvent().copy(
                localSendState = LocalEventSendState.SendingFailed("AN_ERROR"),
                content = aTimelineItemTextContent().copy(isEdited = true),
            ),
        )
}
