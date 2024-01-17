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

interface FeatureFlagProvider {
    val priority: Int
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>
    fun hasFeature(feature: Feature): Boolean
}

const val LOW_PRIORITY = 0
const val MEDIUM_PRIORITY = 1
const val HIGH_PRIORITY = 2
