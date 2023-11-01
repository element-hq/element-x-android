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

package io.element.android.libraries.featureflag.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface FeatureFlagService {
    /**
     * @param feature the feature to check for
     *
     * @return true if the feature is enabled
     */
    suspend fun isFeatureEnabled(feature: Feature): Boolean = isFeatureEnabledFlow(feature).first()

    /**
     * @param feature the feature to check for
     *
     * @return a flow of booleans, true if the feature is enabled, false if it is disabled.
     */
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>

    /**
     * @param feature the feature to enable or disable
     * @param enabled true to enable the feature
     *
     * @return true if the method succeeds, ie if a [io.element.android.libraries.featureflag.impl.MutableFeatureFlagProvider]
     * is registered
     */
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean
}
