/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustTimelineEventTypeFilter
import org.matrix.rustcomponents.sdk.TimelineEventTypeFilter

class FakeTimelineEventTypeFilterFactory : TimelineEventTypeFilterFactory {
    override fun create(listStateEventType: List<StateEventType>): TimelineEventTypeFilter {
        return FakeRustTimelineEventTypeFilter()
    }
}
