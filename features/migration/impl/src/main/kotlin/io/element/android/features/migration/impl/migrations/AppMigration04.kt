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

import android.content.Context
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

/**
 * Remove notifications.bin file, used to store notification data locally.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration04 @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppMigration {
    companion object {
        internal const val NOTIFICATION_FILE_NAME = "notifications.bin"
    }
    override val order: Int = 4

    override suspend fun migrate() {
        runCatching { context.getDatabasePath(NOTIFICATION_FILE_NAME).delete() }
    }
}
