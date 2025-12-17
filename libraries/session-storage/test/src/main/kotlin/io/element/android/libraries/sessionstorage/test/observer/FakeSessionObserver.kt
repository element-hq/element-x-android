/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

    suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean = true) {
        listeners.forEach { it.onSessionDeleted(userId, wasLastSession = wasLastSession) }
    }
}
