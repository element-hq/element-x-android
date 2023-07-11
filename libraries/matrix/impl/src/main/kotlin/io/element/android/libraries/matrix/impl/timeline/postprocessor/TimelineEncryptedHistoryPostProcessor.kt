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

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.util.Date

class TimelineEncryptedHistoryPostProcessor(
    private val lastLoginTimestamp: Date?,
    private val isRoomEncrypted: Boolean,
    private val paginationStateFlow: MutableStateFlow<MatrixTimeline.PaginationState>,
) {

    fun process(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        if (!isRoomEncrypted || lastLoginTimestamp == null) return items

        val filteredItems = replaceWithEncryptionHistoryBannerIfNeeded(items)
        // Disable back pagination
        val wasFiltered = filteredItems !== items
        if (wasFiltered) {
            paginationStateFlow.getAndUpdate {
                it.copy(
                    isBackPaginating = false,
                    canBackPaginate = false
                )
            }
        }
        return filteredItems
    }

    private fun replaceWithEncryptionHistoryBannerIfNeeded(list: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        var lastEncryptedHistoryBannerIndex = -1
        for ((i, item) in list.withIndex()) {
            if (isItemEncryptionHistory(item)) {
                lastEncryptedHistoryBannerIndex = i
            }
        }
        return if (lastEncryptedHistoryBannerIndex >= 0) {
            val sublist = list.drop(lastEncryptedHistoryBannerIndex + 1).toMutableList()
            sublist.add(0, MatrixTimelineItem.Virtual(VirtualTimelineItem.EncryptedHistoryBanner))
            sublist
        } else {
            list
        }
    }

    private fun isItemEncryptionHistory(item: MatrixTimelineItem): Boolean {
        if ((item as? MatrixTimelineItem.Virtual)?.virtual is VirtualTimelineItem.EncryptedHistoryBanner) {
            return true
        }
        val timestamp = (item as? MatrixTimelineItem.Event)?.event?.timestamp ?: return false
        return timestamp <= lastLoginTimestamp!!.time
    }

}
