/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.tracing

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import org.matrix.rustcomponents.sdk.TracingFileConfiguration
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustTracingService @Inject constructor(private val buildMeta: BuildMeta) : TracingService {
    override fun setupTracing(tracingConfiguration: TracingConfiguration) {
        val filter = tracingConfiguration.filterConfiguration
        val rustTracingConfiguration = org.matrix.rustcomponents.sdk.TracingConfiguration(
            filter = tracingConfiguration.filterConfiguration.filter,
            writeToStdoutOrSystem = tracingConfiguration.writesToLogcat,
            writeToFiles = tracingConfiguration.writesToFilesConfiguration.toTracingFileConfiguration(),
        )
        org.matrix.rustcomponents.sdk.setupTracing(rustTracingConfiguration)
        Timber.v("Tracing config filter = $filter: ${filter.filter}")
    }

    override fun createTimberTree(): Timber.Tree {
        return RustTracingTree(retrieveFromStackTrace = buildMeta.isDebuggable)
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
