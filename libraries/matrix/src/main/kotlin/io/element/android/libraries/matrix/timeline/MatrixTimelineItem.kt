/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.matrix.timeline

import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.TimelineItem

sealed interface MatrixTimelineItem {
    data class Event(val event: EventTimelineItem) : MatrixTimelineItem {
        val uniqueId: String = event.uniqueIdentifier()
    }

    object Virtual : MatrixTimelineItem
    object Other : MatrixTimelineItem
}

fun TimelineItem.asMatrixTimelineItem(): MatrixTimelineItem {
    val asEvent = asEvent()
    if (asEvent != null) {
        return MatrixTimelineItem.Event(asEvent)
    }
    val asVirtual = asVirtual()
    if (asVirtual != null) {
        return MatrixTimelineItem.Virtual
    }
    return MatrixTimelineItem.Other
}
