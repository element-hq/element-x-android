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

package io.element.android.features.roomlist.impl.migration

import io.element.android.features.roomlist.api.migration.MigrationScreenStore
import io.element.android.libraries.matrix.api.core.SessionId

class InMemoryMigrationScreenStore : MigrationScreenStore {
    private val store = mutableMapOf<SessionId, Boolean>()

    override fun isMigrationScreenNeeded(sessionId: SessionId): Boolean {
        // If store does not have key return true, else return the opposite of the value
        return store[sessionId]?.not() ?: true
    }

    override fun setMigrationScreenShown(sessionId: SessionId) {
        store[sessionId] = true
    }

    override fun reset() {
        store.clear()
    }
}
