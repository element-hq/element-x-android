/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * This interface defines a way to get the active timeline.
 * It could be the live timeline, a pinned timeline or a detached timeline.
 * By default, the active timeline is the live timeline.
 */
interface TimelineProvider {
    fun activeTimelineFlow(): StateFlow<Timeline?>
}

suspend fun TimelineProvider.getActiveTimeline(): Timeline = activeTimelineFlow().filterNotNull().first()
