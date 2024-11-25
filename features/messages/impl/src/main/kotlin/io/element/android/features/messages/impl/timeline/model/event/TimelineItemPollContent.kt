/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.features.poll.api.pollcontent.PollAnswerItem
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind

data class TimelineItemPollContent(
    val isMine: Boolean,
    val isEditable: Boolean,
    val eventId: EventId?,
    val question: String,
    val answerItems: List<PollAnswerItem>,
    val pollKind: PollKind,
    val isEnded: Boolean,
    override val isEdited: Boolean,
) : TimelineItemEventContent,
    TimelineItemEventMutableContent {
    override val type: String = "TimelineItemPollContent"
}
