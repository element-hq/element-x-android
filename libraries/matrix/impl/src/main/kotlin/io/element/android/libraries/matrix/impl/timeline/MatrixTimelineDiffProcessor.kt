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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.timeline.MatrixTimeline
import io.element.android.libraries.matrix.timeline.MatrixTimelineItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener
import org.matrix.rustcomponents.sdk.VirtualTimelineItem

internal class MatrixTimelineDiffProcessor(
    private val paginationState: MutableStateFlow<MatrixTimeline.PaginationState>,
    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>>,
    private val coroutineScope: CoroutineScope,
    private val diffDispatcher: CoroutineDispatcher,
) : TimelineListener {

    override fun onUpdate(update: TimelineDiff) {
        coroutineScope.launch {
            updateTimelineItems {
                applyDiff(update)
            }
            when (val firstItem = timelineItems.value.firstOrNull()) {
                is MatrixTimelineItem.Virtual -> updateBackPaginationState(firstItem.virtual)
                else -> updateBackPaginationState(null)
            }
        }
    }

    private fun updateBackPaginationState(virtualItem: VirtualTimelineItem?) {
        val currentPaginationState = paginationState.value
        val newPaginationState = when (virtualItem) {
            VirtualTimelineItem.LoadingIndicator -> currentPaginationState.copy(
                isBackPaginating = true,
                canBackPaginate = true
            )
            VirtualTimelineItem.TimelineStart -> currentPaginationState.copy(
                isBackPaginating = false,
                canBackPaginate = false
            )
            else -> currentPaginationState.copy(
                isBackPaginating = false,
                canBackPaginate = true
            )
        }
        paginationState.value = newPaginationState
    }

    private suspend fun updateTimelineItems(block: MutableList<MatrixTimelineItem>.() -> Unit) =
        withContext(diffDispatcher) {
            val mutableTimelineItems = timelineItems.value.toMutableList()
            block(mutableTimelineItems)
            timelineItems.value = mutableTimelineItems
        }

    private fun MutableList<MatrixTimelineItem>.applyDiff(diff: TimelineDiff) {
        when (diff.change()) {
            TimelineChange.APPEND -> {
                val items = diff.append()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.PUSH_BACK -> {
                val item = diff.pushBack()?.asMatrixTimelineItem() ?: return
                add(item)
            }
            TimelineChange.PUSH_FRONT -> {
                val item = diff.pushFront()?.asMatrixTimelineItem() ?: return
                add(0, item)
            }
            TimelineChange.SET -> {
                val updateAtData = diff.set() ?: return
                val item = updateAtData.item.asMatrixTimelineItem()
                set(updateAtData.index.toInt(), item)
            }
            TimelineChange.INSERT -> {
                val insertAtData = diff.insert() ?: return
                val item = insertAtData.item.asMatrixTimelineItem()
                add(insertAtData.index.toInt(), item)
            }
            TimelineChange.REMOVE -> {
                val removeAtData = diff.remove() ?: return
                removeAt(removeAtData.toInt())
            }
            TimelineChange.RESET -> {
                clear()
                val items = diff.reset()?.map { it.asMatrixTimelineItem() } ?: return
                addAll(items)
            }
            TimelineChange.POP_FRONT -> {
                removeFirstOrNull()
            }
            TimelineChange.POP_BACK -> {
                removeLastOrNull()
            }
            TimelineChange.CLEAR -> {
                clear()
            }
        }
    }
}
