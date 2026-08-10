/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId

interface TimelineProtectionStore {
    val allowedEventIds: Set<EventId>
    fun allowEvent(eventId: EventId)
}

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultTimelineProtectionStore : TimelineProtectionStore {
    private val allowedEvents = mutableStateOf(emptySet<EventId>())

    override val allowedEventIds: Set<EventId> get() = allowedEvents.value

    override fun allowEvent(eventId: EventId) {
        allowedEvents.value = allowedEvents.value + eventId
    }
}
