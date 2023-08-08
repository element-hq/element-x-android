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
import androidx.startup.Initializer
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
        Timber.plant(tracingService.createTracingTree())
        val tracingConfiguration = if (BuildConfig.DEBUG) {
            TracingConfiguration(
                filterConfiguration = TracingFilterConfigurations.debug,
                writesToStdout = false,
                writesToFilesConfiguration = WriteToFilesConfiguration.Disabled
            )
        } else {
            TracingConfiguration(
                filterConfiguration = TracingFilterConfigurations.release,
                writesToStdout = false,
                writesToFilesConfiguration = WriteToFilesConfiguration.Enabled(
                    directory = bugReporter.logDirectory().absolutePath,
                    filenamePrefix = "logs"
                )
            )
        }
        bugReporter.cleanLogDirectoryIfNeeded()
        tracingService.setupTracing(tracingConfiguration)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = mutableListOf()
}
