/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineEventFilter
import org.matrix.rustcomponents.sdk.TimelineEventFilter

class FakeTimelineEventFilterFactory : TimelineEventFilterFactory {
    override fun create(listStateEventType: List<StateEventType>): TimelineEventFilter {
        return FakeFfiTimelineEventFilter()
    }
}
