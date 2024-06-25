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
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.sessionstorage.api.SessionStore
import java.io.File
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class AppMigration05 @Inject constructor(
    private val sessionStore: SessionStore,
    private val baseDirectory: File,
) : AppMigration {
    override val order: Int = 5

    override suspend fun migrate() {
        val allSessions = sessionStore.getAllSessions()
        for (session in allSessions) {
            if (session.sessionPath.isEmpty()) {
                val sessionPath = File(baseDirectory, session.userId.replace(':', '_')).absolutePath
                sessionStore.updateData(session.copy(sessionPath = sessionPath))
            }
        }
    }
}
