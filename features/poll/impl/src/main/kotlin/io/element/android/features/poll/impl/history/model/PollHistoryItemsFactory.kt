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

package io.element.android.features.poll.impl.history.model

import io.element.android.features.poll.api.pollcontent.PollContentStateFactory
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PollHistoryItemsFactory @Inject constructor(
    private val pollContentStateFactory: PollContentStateFactory,
    private val daySeparatorFormatter: DaySeparatorFormatter,
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
                    formattedDate = daySeparatorFormatter.format(timelineItem.event.timestamp),
                    state = pollContentState
                )
            }
            else -> null
        }
    }
}
