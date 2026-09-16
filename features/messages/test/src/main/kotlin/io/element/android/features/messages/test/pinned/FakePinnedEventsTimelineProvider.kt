/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.test.pinned

import io.element.android.features.messages.api.pinned.PinnedEventsTimelineProvider
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.timeline.FakeTimelineProvider
import kotlinx.coroutines.flow.StateFlow

class FakePinnedEventsTimelineProvider(
    private val fakeTimelineProvider: FakeTimelineProvider = FakeTimelineProvider(),
) : PinnedEventsTimelineProvider {
    override fun activeTimelineFlow(): StateFlow<Timeline?> = fakeTimelineProvider.activeTimelineFlow()
}
