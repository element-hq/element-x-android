/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl.observer

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.sessionstorage.api.observer.SessionListener

class TestSessionListener : SessionListener {
    sealed interface Event {
        data class Created(val userId: String) : Event
        data class Deleted(val userId: String) : Event
    }

    private val trackRecord: MutableList<Event> = mutableListOf()

    override suspend fun onSessionCreated(userId: String) {
        trackRecord.add(Event.Created(userId))
    }

    override suspend fun onSessionDeleted(userId: String) {
        trackRecord.add(Event.Deleted(userId))
    }

    fun assertEvents(vararg events: Event) {
        assertThat(trackRecord).containsExactly(*events)
    }
}
