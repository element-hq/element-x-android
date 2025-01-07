/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.room.StateEventType
import org.matrix.rustcomponents.sdk.FilterTimelineEventType
import org.matrix.rustcomponents.sdk.TimelineEventTypeFilter
import javax.inject.Inject

interface TimelineEventTypeFilterFactory {
    fun create(listStateEventType: List<StateEventType>): TimelineEventTypeFilter
}

@ContributesBinding(AppScope::class)
class RustTimelineEventTypeFilterFactory @Inject constructor() : TimelineEventTypeFilterFactory {
    override fun create(listStateEventType: List<StateEventType>): TimelineEventTypeFilter {
        return TimelineEventTypeFilter.exclude(
            listStateEventType.map { stateEventType ->
                FilterTimelineEventType.State(stateEventType.map())
            }
        )
    }
}
