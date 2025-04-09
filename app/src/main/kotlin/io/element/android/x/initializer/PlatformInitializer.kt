/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.initializer

import android.content.Context
import android.system.Os
import androidx.startup.Initializer
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import io.element.android.x.di.AppBindings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

private const val ELEMENT_X_TARGET = "elementx"

class PlatformInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val appBindings = context.bindings<AppBindings>()
        val tracingService = appBindings.tracingService()
        val platformService = appBindings.platformService()
        val bugReporter = appBindings.bugReporter()
        Timber.plant(tracingService.createTimberTree(ELEMENT_X_TARGET))
        val preferencesStore = appBindings.preferencesStore()
        val featureFlagService = appBindings.featureFlagService()
        val logLevel = runBlocking { preferencesStore.getTracingLogLevelFlow().first() }
        val tracingConfiguration = TracingConfiguration(
            writesToLogcat = runBlocking { featureFlagService.isFeatureEnabled(FeatureFlags.PrintLogsToLogcat) },
            writesToFilesConfiguration = defaultWriteToDiskConfiguration(bugReporter),
            logLevel = logLevel,
            extraTargets = listOf(ELEMENT_X_TARGET),
            traceLogPacks = runBlocking { preferencesStore.getTracingLogPacksFlow().first() },
        )
        bugReporter.setCurrentTracingLogLevel(logLevel.name)
        platformService.init(tracingConfiguration)
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
