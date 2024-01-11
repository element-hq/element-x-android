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

import io.element.android.libraries.featureflag.api.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMutableFeatureFlagProvider(override val priority: Int) : MutableFeatureFlagProvider {
    private val enabledFeatures = mutableMapOf<String, MutableStateFlow<Boolean>>()

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean) {
        val flow = enabledFeatures.getOrPut(feature.key) { MutableStateFlow(enabled) }
        flow.emit(enabled)
    }

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return enabledFeatures.getOrPut(feature.key) { MutableStateFlow(feature.defaultValue) }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
