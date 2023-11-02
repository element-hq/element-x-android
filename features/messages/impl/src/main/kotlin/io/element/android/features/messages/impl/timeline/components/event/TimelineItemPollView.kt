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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContentProvider
import io.element.android.features.poll.api.PollContentView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TimelineItemPollView(
    content: TimelineItemPollContent,
    isMine: Boolean,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onAnswerSelected(pollStartId: EventId, answerId: String) {
        eventSink(TimelineEvents.PollAnswerSelected(pollStartId, answerId))
    }

    fun onPollEnd(pollStartId: EventId) {
        eventSink(TimelineEvents.PollEndClicked(pollStartId))
    }

    PollContentView(
        eventId = content.eventId,
        question = content.question,
        answerItems = content.answerItems.toImmutableList(),
        pollKind = content.pollKind,
        isPollEnded = content.isEnded,
        isMine = isMine,
        onAnswerSelected = ::onAnswerSelected,
        onPollEdit = {}, // TODO Polls: Wire up this callback once poll edit screen is done.
        onPollEnd = ::onPollEnd,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemPollViewPreview(@PreviewParameter(TimelineItemPollContentProvider::class) content: TimelineItemPollContent) =
    ElementPreview {
        TimelineItemPollView(
            content = content,
            isMine = false,
            eventSink = {},
        )
    }

@PreviewsDayNight
@Composable
internal fun TimelineItemPollCreatorViewPreview(@PreviewParameter(TimelineItemPollContentProvider::class) content: TimelineItemPollContent) =
    ElementPreview {
        TimelineItemPollView(
            content = content,
            isMine = true,
            eventSink = {},
        )
    }
