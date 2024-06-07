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

package io.element.android.features.preferences.impl.developer.tracing

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import javax.inject.Inject

interface TracingConfigurationStore {
    fun getLogLevel(target: Target): LogLevel?
    fun storeLogLevel(target: Target, logLevel: LogLevel)
    fun reset()
}

@ContributesBinding(AppScope::class)
class SharedPreferencesTracingConfigurationStore @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : TracingConfigurationStore {
    override fun getLogLevel(target: Target): LogLevel? {
        return sharedPreferences.getString("$KEY_PREFIX${target.name}", null)
            ?.let { LogLevel.valueOf(it) }
    }

    override fun storeLogLevel(target: Target, logLevel: LogLevel) {
        sharedPreferences.edit {
            putString("$KEY_PREFIX${target.name}", logLevel.name)
        }
    }

    override fun reset() {
        sharedPreferences.edit {
            sharedPreferences.all.keys.filter { it.startsWith(KEY_PREFIX) }.forEach {
                remove(it)
            }
        }
    }

    companion object {
        private const val KEY_PREFIX = "tracing_log_level_"
    }
}
