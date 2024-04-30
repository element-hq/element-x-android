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
