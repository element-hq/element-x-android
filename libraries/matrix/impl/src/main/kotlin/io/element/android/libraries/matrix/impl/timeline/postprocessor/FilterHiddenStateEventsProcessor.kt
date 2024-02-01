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
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent

/**
 * This class is used to filter out 'hidden' state events from the timeline.
 */
class FilterHiddenStateEventsProcessor {
    fun process(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        return items.filter { item ->
            when (item) {
                is MatrixTimelineItem.Event -> {
                    when (val content = item.event.content) {
                        // If it's a state event, make sure it's visible
                        is StateContent -> content.isVisibleInTimeline()
                        // We can display any other event
                        else -> true
                    }
                }
                is MatrixTimelineItem.Virtual -> true
                is MatrixTimelineItem.Other -> true
            }
        }
    }
}
