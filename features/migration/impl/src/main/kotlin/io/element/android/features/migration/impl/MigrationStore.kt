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

package io.element.android.features.migration.impl

import kotlinx.coroutines.flow.Flow

interface MigrationStore {
    /**
     * Return of flow of the current value for application migration version.
     * If the value is not set, it will emit 0.
     * If the emitted value is lower than the current application migration version, it means
     * that a migration should occur, and at the end [setApplicationMigrationVersion] should be called.
     */
    fun applicationMigrationVersion(): Flow<Int>

    /**
     * Set the application migration version, typically after a migration has been done.
     */
    suspend fun setApplicationMigrationVersion(version: Int)
}
