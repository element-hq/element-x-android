/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.initializer

import android.content.Context
import android.system.Os
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import io.element.android.features.preferences.impl.developer.tracing.SharedPreferencesTracingConfigurationStore
import io.element.android.features.preferences.impl.developer.tracing.TargetLogLevelMapBuilder
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfigurations
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import io.element.android.x.BuildConfig
import io.element.android.x.di.AppBindings
import timber.log.Timber

class TracingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val appBindings = context.bindings<AppBindings>()
        val tracingService = appBindings.tracingService()
        val bugReporter = appBindings.bugReporter()
        Timber.plant(tracingService.createTimberTree())
        val tracingConfiguration = if (BuildConfig.DEBUG) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val store = SharedPreferencesTracingConfigurationStore(prefs)
            val builder = TargetLogLevelMapBuilder(store)
            TracingConfiguration(
                filterConfiguration = TracingFilterConfigurations.custom(builder.getCurrentMap()),
                writesToLogcat = true,
                writesToFilesConfiguration = WriteToFilesConfiguration.Disabled
            )
        } else {
            val config = if (BuildConfig.BUILD_TYPE == "nightly") {
                TracingFilterConfigurations.nightly
            } else {
                TracingFilterConfigurations.release
            }
            TracingConfiguration(
                filterConfiguration = config,
                writesToLogcat = false,
                writesToFilesConfiguration = WriteToFilesConfiguration.Enabled(
                    directory = bugReporter.logDirectory().absolutePath,
                    filenamePrefix = "logs",
                    filenameSuffix = null,
                    // Keep a minimum of 1 week of log files.
                    numberOfFiles = 7 * 24,
                )
            )
        }
        bugReporter.setCurrentTracingFilter(tracingConfiguration.filterConfiguration.filter)
        tracingService.setupTracing(tracingConfiguration)
        // Also set env variable for rust back trace
        Os.setenv("RUST_BACKTRACE", "1", true)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = mutableListOf()
}
