/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfiguration
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfigurations

@Module
@ContributesTo(AppScope::class)
object TracingMatrixModule {
    @Provides
    fun providesTracingFilterConfiguration(buildMeta: BuildMeta): TracingFilterConfiguration {
        return when (buildMeta.buildType) {
            BuildType.DEBUG -> TracingFilterConfigurations.debug
            BuildType.NIGHTLY -> TracingFilterConfigurations.nightly
            BuildType.RELEASE -> TracingFilterConfigurations.release
        }
    }
}
