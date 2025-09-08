/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.room.StateEventType
import org.matrix.rustcomponents.sdk.FilterTimelineEventType
import org.matrix.rustcomponents.sdk.TimelineEventTypeFilter

interface TimelineEventTypeFilterFactory {
    fun create(listStateEventType: List<StateEventType>): TimelineEventTypeFilter
}

@ContributesBinding(AppScope::class)
@Inject
class RustTimelineEventTypeFilterFactory : TimelineEventTypeFilterFactory {
    override fun create(listStateEventType: List<StateEventType>): TimelineEventTypeFilter {
        return TimelineEventTypeFilter.exclude(
            listStateEventType.map { stateEventType ->
                FilterTimelineEventType.State(stateEventType.map())
            }
        )
    }
}
