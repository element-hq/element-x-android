/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSessionPreferencesStoreFactory(
    @ApplicationContext private val context: Context,
    sessionObserver: SessionObserver,
) : SessionPreferencesStoreFactory {
    private val cache = ConcurrentHashMap<SessionId, DefaultSessionPreferencesStore>()

    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                val sessionPreferences = cache.remove(SessionId(userId))
                sessionPreferences?.clear()
            }
        })
    }

    override fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore = cache.getOrPut(sessionId) {
        DefaultSessionPreferencesStore(context, sessionId, sessionCoroutineScope)
    }

    override fun remove(sessionId: SessionId) {
        cache.remove(sessionId)
    }
}
