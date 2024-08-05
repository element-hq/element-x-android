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
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

/**
 * This post processor is responsible for adding virtual items to indicate all the previous last forward item.
 */
class LastForwardIndicatorsPostProcessor(
    private val mode: Timeline.Mode,
) {
    private val lastForwardIdentifiers = LinkedHashSet<String>()

    fun process(
        items: List<MatrixTimelineItem>,
    ): List<MatrixTimelineItem> {
        // We don't need to add the last forward indicator if we are not in the FOCUSED_ON_EVENT mode
        if (mode != Timeline.Mode.FOCUSED_ON_EVENT) {
            return items
        } else {
            return buildList {
                val latestEventIdentifier = items.latestEventIdentifier()
                // Remove if it always exists (this should happen only when no new events are added)
                lastForwardIdentifiers.remove(latestEventIdentifier)

                items.forEach { item ->
                    add(item)

                    if (item is MatrixTimelineItem.Event) {
                        if (lastForwardIdentifiers.contains(item.uniqueId)) {
                            add(createLastForwardIndicator(item.uniqueId))
                        }
                    }
                }
                // This is important to always add this one at the end of the list so it's used to keep the scroll position.
                add(createLastForwardIndicator(latestEventIdentifier))
                lastForwardIdentifiers.add(latestEventIdentifier)
            }
        }
    }
}

private fun createLastForwardIndicator(identifier: String): MatrixTimelineItem {
    return MatrixTimelineItem.Virtual(
        uniqueId = "last_forward_indicator_$identifier",
        virtual = VirtualTimelineItem.LastForwardIndicator
    )
}

private fun List<MatrixTimelineItem>.latestEventIdentifier(): String {
    return findLast {
        it is MatrixTimelineItem.Event
    }?.let {
        (it as MatrixTimelineItem.Event).uniqueId
    } ?: "fake_id"
}
