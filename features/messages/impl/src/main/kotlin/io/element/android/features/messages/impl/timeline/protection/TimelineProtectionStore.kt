/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.SnapshotStateSet
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId

interface TimelineProtectionStore {
    val allowedEventIds: SnapshotStateSet<EventId>
    fun allowEvent(eventId: EventId)
}

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultTimelineProtectionStore : TimelineProtectionStore {
    private val allowedEvents = mutableStateSetOf<EventId>()

    override val allowedEventIds: SnapshotStateSet<EventId> = allowedEvents

    override fun allowEvent(eventId: EventId) {
        allowedEvents += eventId
    }
}
