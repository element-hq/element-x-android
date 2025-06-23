/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.tracing

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import org.matrix.rustcomponents.sdk.TracingFileConfiguration
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustTracingService @Inject constructor(private val buildMeta: BuildMeta) : TracingService {
    override fun createTimberTree(target: String): Timber.Tree {
        return RustTracingTree(target = target, retrieveFromStackTrace = buildMeta.isDebuggable)
    }
}

private fun LogLevel.toRustLogLevel(): org.matrix.rustcomponents.sdk.LogLevel {
    return when (this) {
        LogLevel.ERROR -> org.matrix.rustcomponents.sdk.LogLevel.ERROR
        LogLevel.WARN -> org.matrix.rustcomponents.sdk.LogLevel.WARN
        LogLevel.INFO -> org.matrix.rustcomponents.sdk.LogLevel.INFO
        LogLevel.DEBUG -> org.matrix.rustcomponents.sdk.LogLevel.DEBUG
        LogLevel.TRACE -> org.matrix.rustcomponents.sdk.LogLevel.TRACE
    }
}

private fun WriteToFilesConfiguration.toTracingFileConfiguration(): TracingFileConfiguration? {
    return when (this) {
        is WriteToFilesConfiguration.Disabled -> null
        is WriteToFilesConfiguration.Enabled -> TracingFileConfiguration(
            path = directory,
            filePrefix = filenamePrefix,
            fileSuffix = filenameSuffix,
            maxFiles = numberOfFiles?.toULong(),
        )
    }
}

fun TracingConfiguration.map(): org.matrix.rustcomponents.sdk.TracingConfiguration = org.matrix.rustcomponents.sdk.TracingConfiguration(
    writeToStdoutOrSystem = writesToLogcat,
    logLevel = logLevel.toRustLogLevel(),
    extraTargets = extraTargets,
    traceLogPacks = traceLogPacks.map(),
    writeToFiles = writesToFilesConfiguration.toTracingFileConfiguration(),
    sentryDsn = null,
)
