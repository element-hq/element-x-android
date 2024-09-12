/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
        productionApplicationName = "EAX-Minimal",
        desktopApplicationName = "EAX-Minimal-Desktop",
        applicationId = "io.element.android.samples.minimal",
        isEnterpriseBuild = false,
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
