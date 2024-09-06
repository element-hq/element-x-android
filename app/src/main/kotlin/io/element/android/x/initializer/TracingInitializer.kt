/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x.initializer

import android.content.Context
import android.system.Os
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import io.element.android.features.preferences.impl.developer.tracing.SharedPreferencesTracingConfigurationStore
import io.element.android.features.preferences.impl.developer.tracing.TargetLogLevelMapBuilder
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.meta.BuildType
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
        val tracingConfiguration = if (BuildConfig.BUILD_TYPE == BuildType.RELEASE.name) {
            TracingConfiguration(
                filterConfiguration = TracingFilterConfigurations.release,
                writesToLogcat = false,
                writesToFilesConfiguration = defaultWriteToDiskConfiguration(bugReporter),
            )
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val store = SharedPreferencesTracingConfigurationStore(prefs)
            val builder = TargetLogLevelMapBuilder(
                tracingConfigurationStore = store,
                defaultConfig = if (BuildConfig.BUILD_TYPE == BuildType.NIGHTLY.name) {
                    TracingFilterConfigurations.nightly
                } else {
                    TracingFilterConfigurations.debug
                }
            )
            TracingConfiguration(
                filterConfiguration = TracingFilterConfigurations.custom(builder.getCurrentMap()),
                writesToLogcat = BuildConfig.DEBUG,
                writesToFilesConfiguration = defaultWriteToDiskConfiguration(bugReporter),
            )
        }
        bugReporter.setCurrentTracingFilter(tracingConfiguration.filterConfiguration.filter)
        tracingService.setupTracing(tracingConfiguration)
        // Also set env variable for rust back trace
        Os.setenv("RUST_BACKTRACE", "1", true)
    }

    private fun defaultWriteToDiskConfiguration(bugReporter: BugReporter): WriteToFilesConfiguration.Enabled {
        return WriteToFilesConfiguration.Enabled(
            directory = bugReporter.logDirectory().absolutePath,
            filenamePrefix = "logs",
            // Keep a maximum of 1 week of log files.
            numberOfFiles = 7 * 24,
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = mutableListOf()
}
