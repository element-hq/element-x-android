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

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable

/**
 * Attribute for a TimelineItem, used to render successive events from the same sender differently.
 *
 * Possible sequences in the timeline will be:
 *
 * Only one Event:
 * - [None]
 *
 * Two Events
 * - [First]
 * - [Last]
 *
 * Many Events:
 * - [First]
 * - [Middle] (repeated if necessary)
 * - [Last]
 */
@Immutable
sealed interface TimelineItemGroupPosition {
    /**
     * The event is part of a group of events from the same sender and is the first sent Event.
     */
    data object First : TimelineItemGroupPosition

    /**
     * The event is part of a group of events from the same sender and is neither the first nor the last sent Event.
     */
    data object Middle : TimelineItemGroupPosition

    /**
     * The event is part of a group of events from the same sender and is the last sent Event.
     */
    data object Last : TimelineItemGroupPosition

    /**
     * The event is not part of a group of events. Sender of previous event is different, and sender of next event is different.
     */
    data object None : TimelineItemGroupPosition

    /**
     * Return true if the previous sender of the event is a different sender.
     */
    fun isNew(): Boolean = when (this) {
        First, None -> true
        else -> false
    }
}
