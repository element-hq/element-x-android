/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
