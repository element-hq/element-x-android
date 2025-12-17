/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContentProvider
import io.element.android.features.poll.api.pollcontent.PollContentView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TimelineItemPollView(
    content: TimelineItemPollContent,
    eventSink: (TimelineEvents.TimelineItemPollEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onSelectAnswer(pollStartId: EventId, answerId: String) {
        eventSink(TimelineEvents.SelectPollAnswer(pollStartId, answerId))
    }

    fun onEndPoll(pollStartId: EventId) {
        eventSink(TimelineEvents.EndPoll(pollStartId))
    }

    fun onEditPoll(pollStartId: EventId) {
        eventSink(TimelineEvents.EditPoll(pollStartId))
    }

    PollContentView(
        eventId = content.eventId,
        question = content.question,
        answerItems = content.answerItems.toImmutableList(),
        pollKind = content.pollKind,
        isPollEnded = content.isEnded,
        isPollEditable = content.isEditable,
        isMine = content.isMine,
        onSelectAnswer = ::onSelectAnswer,
        onEditPoll = ::onEditPoll,
        onEndPoll = ::onEndPoll,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemPollViewPreview(@PreviewParameter(TimelineItemPollContentProvider::class) content: TimelineItemPollContent) =
    ElementPreview {
        TimelineItemPollView(
            content = content,
            eventSink = {},
        )
    }
