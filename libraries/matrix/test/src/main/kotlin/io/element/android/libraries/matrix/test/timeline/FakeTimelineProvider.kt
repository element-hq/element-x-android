/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.timeline

import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTimelineProvider(
    initialTimeline: Timeline? = null,
) : TimelineProvider {
    private val timelineFlow = MutableStateFlow(initialTimeline)

    override fun activeTimelineFlow(): StateFlow<Timeline?> {
        return timelineFlow.asStateFlow()
    }
}
