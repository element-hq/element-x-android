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

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.features.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSessionPreferencesStoreFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    sessionObserver: SessionObserver,
) : SessionPreferencesStoreFactory {
    private val cache = ConcurrentHashMap<SessionId, DefaultSessionPreferencesStore>()

    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionCreated(userId: String) = Unit
            override suspend fun onSessionDeleted(userId: String) {
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
