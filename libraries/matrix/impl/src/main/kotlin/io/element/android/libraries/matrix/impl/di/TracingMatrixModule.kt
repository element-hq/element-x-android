/*
 * Copyright (c) 2024 New Vector Ltd
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
