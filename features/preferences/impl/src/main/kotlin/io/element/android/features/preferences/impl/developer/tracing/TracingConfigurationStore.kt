/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
