/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.sessionstorage.impl.observer

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.sessionstorage.api.observer.SessionListener

class TestSessionListener : SessionListener {
    sealed class Event {
        data class Created(val userId: String) : Event()
        data class Deleted(val userId: String) : Event()
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
