/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history.model

import io.element.android.features.poll.api.pollcontent.PollContentStateFactory
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PollHistoryItemsFactory @Inject constructor(
    private val pollContentStateFactory: PollContentStateFactory,
    private val dateFormatter: DateFormatter,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend fun create(timelineItems: List<MatrixTimelineItem>): PollHistoryItems = withContext(dispatchers.computation) {
        val past = ArrayList<PollHistoryItem>()
        val ongoing = ArrayList<PollHistoryItem>()
        for (index in timelineItems.indices.reversed()) {
            val timelineItem = timelineItems[index]
            val pollHistoryItem = create(timelineItem) ?: continue
            if (pollHistoryItem.state.isPollEnded) {
                past.add(pollHistoryItem)
            } else {
                ongoing.add(pollHistoryItem)
            }
        }
        PollHistoryItems(
            ongoing = ongoing.toPersistentList(),
            past = past.toPersistentList()
        )
    }

    private suspend fun create(timelineItem: MatrixTimelineItem): PollHistoryItem? {
        return when (timelineItem) {
            is MatrixTimelineItem.Event -> {
                val pollContent = timelineItem.event.content as? PollContent ?: return null
                val pollContentState = pollContentStateFactory.create(timelineItem.event, pollContent)
                PollHistoryItem(
                    formattedDate = dateFormatter.format(
                        timestamp = timelineItem.event.timestamp,
                        mode = DateFormatterMode.Day,
                        useRelative = true
                    ),
                    state = pollContentState
                )
            }
            else -> null
        }
    }
}
