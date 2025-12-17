/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.di

import io.element.android.features.messages.api.pinned.PinnedEventsTimelineProvider
import io.element.android.libraries.matrix.api.timeline.TimelineProvider

interface TimelineBindings {
    val timelineProvider: TimelineProvider
    val pinnedEventsTimelineProvider: PinnedEventsTimelineProvider
}
