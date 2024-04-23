/*
 * Copyright (c) 2024 New Vector Ltd
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

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

class InvisibleIndicatorPostProcessor(
    private val isLive: Boolean,
) {
    private val latestEventIdentifiers: MutableSet<String> = HashSet()

    fun process(
        items: List<MatrixTimelineItem>,
    ): List<MatrixTimelineItem> {
        if (isLive) {
            return items
        } else {
            return buildList {
                items.forEach { item ->
                    add(item)
                    if (item is MatrixTimelineItem.Event) {
                        if (latestEventIdentifiers.contains(item.uniqueId)) {
                            add(createLatestKnownEventIndicator(item.uniqueId))
                        }
                    }
                }
                items.latestEventIdentifier()?.let { latestEventIdentifier ->
                    if (latestEventIdentifiers.add(latestEventIdentifier)) {
                        add(createLatestKnownEventIndicator(latestEventIdentifier))
                    }
                }
            }
        }
    }

    private fun createLatestKnownEventIndicator(identifier: String): MatrixTimelineItem {
        return MatrixTimelineItem.Virtual(
            uniqueId = "latest_known_event_$identifier",
            virtual = VirtualTimelineItem.LatestKnownEventIndicator
        )
    }

    private fun List<MatrixTimelineItem>.latestEventIdentifier(): String? {
        return findLast {
            when (it) {
                is MatrixTimelineItem.Event -> true
                else -> false
            }
        }?.let {
            (it as MatrixTimelineItem.Event).uniqueId
        }
    }

    private fun List<MatrixTimelineItem>.indexOf(identifier: String): Int {
        return indexOfLast {
            when (it) {
                is MatrixTimelineItem.Event -> {
                    it.uniqueId == identifier
                }
                else -> false
            }
        }
    }
}
