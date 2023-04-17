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

package io.element.android.libraries.featureflag.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.featureflag.impl.BuildtimeFeatureFlagProvider
import io.element.android.libraries.featureflag.impl.FeatureFlagProvider
import io.element.android.libraries.featureflag.impl.PreferencesFeatureFlagProvider
import io.element.android.libraries.featureflag.impl.RuntimeFeatureFlagProvider

@Module
@ContributesTo(AppScope::class)
object FeatureFlagModule {

    @Provides
    fun providesRuntimeFeatureFlagProvider(preferencesFeatureFlagProvider: PreferencesFeatureFlagProvider): RuntimeFeatureFlagProvider {
        return preferencesFeatureFlagProvider
    }

    @JvmStatic
    @Provides
    @ElementsIntoSet
    fun providesFeatureFlagProvider(
        buildMeta: BuildMeta,
        buildtimeFeatureFlagProvider: BuildtimeFeatureFlagProvider,
        runtimeFeatureFlagProvider: RuntimeFeatureFlagProvider,
    ): Set<FeatureFlagProvider> {
        val providers = HashSet<FeatureFlagProvider>()
        //TODO change this condition?
        if (buildMeta.isDebug) {
            providers.add(runtimeFeatureFlagProvider)
        } else {
            providers.add(buildtimeFeatureFlagProvider)
        }
        return providers
    }
}
