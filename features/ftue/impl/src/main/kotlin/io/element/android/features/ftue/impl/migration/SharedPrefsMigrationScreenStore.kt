/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.ftue.impl.migration

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.matrix.api.core.SessionId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class SharedPrefsMigrationScreenStore @Inject constructor(
    @DefaultPreferences private val sharedPreferences: SharedPreferences,
) : MigrationScreenStore {
    override fun isMigrationScreenNeeded(sessionId: SessionId): Boolean {
        return sharedPreferences.getBoolean(sessionId.toKey(), false).not()
    }

    override fun setMigrationScreenShown(sessionId: SessionId) {
        sharedPreferences.edit().putBoolean(sessionId.toKey(), true).apply()
    }

    override fun reset() {
        sharedPreferences.edit {
            sharedPreferences.all.keys
                .filter { it.startsWith(IS_MIGRATION_SCREEN_SHOWN_PREFIX) }
                .forEach {
                    remove(it)
                }
        }
    }

    private fun SessionId.toKey(): String {
        // Hash the sessionId to get rid of exotic char and take only the first 16 chars,
        // The risk of collision is not high.
        return IS_MIGRATION_SCREEN_SHOWN_PREFIX + value.hash().take(16)
    }

    companion object {
        private const val IS_MIGRATION_SCREEN_SHOWN_PREFIX = "is_migration_screen_shown_"
    }
}
