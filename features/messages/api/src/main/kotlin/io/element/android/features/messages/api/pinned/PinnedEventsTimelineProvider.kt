/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.pinned

import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.flow.Flow

/**
 * A [TimelineProvider] whose active timeline holds the pinned events of the room, rather than its live events.
 *
 * It exists as its own type so that it can be injected where only the pinned timeline is wanted.
 */
interface PinnedEventsTimelineProvider : TimelineProvider {
    /**
     * How many of the room's pinned events the user can actually see, or `null` while that is not known yet.
     *
     * A room can pin an event the user has no access to — one sent before they joined, in a room that does not share
     * its history. Such an event never appears in the pinned timeline, so counting the ids in the room state would
     * promise more than any screen can show.
     */
    fun displayablePinnedEventsCount(): Flow<Int?>
}
