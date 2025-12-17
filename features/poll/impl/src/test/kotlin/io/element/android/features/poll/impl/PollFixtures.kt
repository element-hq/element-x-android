/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun aPollTimelineItems(
    polls: Map<EventId, PollContent> = emptyMap(),
): Flow<List<MatrixTimelineItem>> {
    return flowOf(
        polls.map { entry ->
            MatrixTimelineItem.Event(
                uniqueId = UniqueId(entry.key.value),
                event = anEventTimelineItem(
                    eventId = entry.key,
                    content = entry.value,
                )
            )
        }
    )
}

fun anOngoingPollContent() = aPollContent(
    question = "Do you like polls?",
    answers = persistentListOf(
        PollAnswer("1", "Yes"),
        PollAnswer("2", "No"),
        PollAnswer("2", "Maybe"),
    ),
)

fun anEndedPollContent() = anOngoingPollContent().copy(
    endTime = 1702400215U
)
