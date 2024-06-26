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

package io.element.android.libraries.sessionstorage.test.observer

import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver

class FakeSessionObserver : SessionObserver {
    private val _listeners = mutableListOf<SessionListener>()

    val listeners: List<SessionListener>
        get() = _listeners

    override fun addListener(listener: SessionListener) {
        _listeners.add(listener)
    }

    override fun removeListener(listener: SessionListener) {
        _listeners.remove(listener)
    }

    suspend fun onSessionCreated(userId: String) {
        listeners.forEach { it.onSessionCreated(userId) }
    }

    suspend fun onSessionDeleted(userId: String) {
        listeners.forEach { it.onSessionDeleted(userId) }
    }
}
