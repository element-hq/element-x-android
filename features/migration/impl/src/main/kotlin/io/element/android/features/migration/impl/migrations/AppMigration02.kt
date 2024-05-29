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

package io.element.android.features.migration.impl.migrations

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.features.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * This migration sets the skip session verification preference to true for all existing sessions.
 * This way we don't force existing users to verify their session again.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration02 @Inject constructor(
    private val sessionStore: SessionStore,
    private val sessionPreferenceStoreFactory: SessionPreferencesStoreFactory,
) : AppMigration {
    override val order: Int = 2

    override suspend fun migrate() {
        coroutineScope {
            for (session in sessionStore.getAllSessions()) {
                val sessionId = SessionId(session.userId)
                val preferences = sessionPreferenceStoreFactory.get(sessionId, this)
                preferences.setSkipSessionVerification(true)
                // This session preference store must be ephemeral since it's not created with the right coroutine scope
                sessionPreferenceStoreFactory.remove(sessionId)
            }
        }
    }
}
