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

package io.element.android.libraries.featureflag.test

import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFeatureFlagService(
    initialState: Map<String, Boolean> = emptyMap()
) : FeatureFlagService {
    private val enabledFeatures = initialState
        .map {
            it.key to MutableStateFlow(it.value)
        }
        .toMap()
        .toMutableMap()

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        val flow = enabledFeatures.getOrPut(feature.key) { MutableStateFlow(enabled) }
        flow.emit(enabled)
        return true
    }

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return enabledFeatures.getOrPut(feature.key) { MutableStateFlow(feature.defaultValue) }
    }
}
