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
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

/**
 * Remove existing logs from the device to remove any leaks of sensitive data.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration01 @Inject constructor(
    private val logFilesRemover: LogFilesRemover,
) : AppMigration {
    override val order: Int = 1

    override suspend fun migrate() {
        logFilesRemover.perform()
    }
}
