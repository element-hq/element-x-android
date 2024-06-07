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

package io.element.android.libraries.featureflag.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFeatureFlagService @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagProvider>
) : FeatureFlagService {
    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return providers.filter { it.hasFeature(feature) }
            .sortedByDescending(FeatureFlagProvider::priority)
            .firstOrNull()
            ?.isFeatureEnabledFlow(feature)
            ?: flowOf(feature.defaultValue)
    }

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        return providers.filterIsInstance<MutableFeatureFlagProvider>()
            .sortedBy(FeatureFlagProvider::priority)
            .firstOrNull()
            ?.setFeatureEnabled(feature, enabled)
            ?.let { true }
            ?: false
    }
}
