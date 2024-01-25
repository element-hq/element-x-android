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

package io.element.android.samples.minimal

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfigurations
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import io.element.android.libraries.matrix.impl.tracing.RustTracingService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

object Singleton {
    val buildMeta = BuildMeta(
        isDebuggable = true,
        buildType = BuildType.DEBUG,
        applicationName = "EAX-Minimal",
        applicationId = "io.element.android.samples.minimal",
        lowPrivacyLoggingEnabled = false,
        versionName = "0.1.0",
        versionCode = 1,
        gitRevision = "",
        gitBranchName = "",
        flavorDescription = "NA",
        flavorShortDescription = "NA",
    )

    init {
        val tracingConfiguration = TracingConfiguration(
            filterConfiguration = TracingFilterConfigurations.debug,
            writesToLogcat = true,
            writesToFilesConfiguration = WriteToFilesConfiguration.Disabled
        )
        RustTracingService(buildMeta).setupTracing(tracingConfiguration)
    }

    val appScope = MainScope() + CoroutineName("Minimal Scope")
    val coroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )
}
