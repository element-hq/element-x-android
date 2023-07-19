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
import io.element.android.libraries.featureflag.api.FeatureFlags
import javax.inject.Inject

class BuildtimeFeatureFlagProvider @Inject constructor() :
    FeatureFlagProvider {

    override val priority: Int
        get() = LOW_PRIORITY

    override suspend fun isFeatureEnabled(feature: Feature): Boolean {
        return if (feature is FeatureFlags) {
            when (feature) {
                FeatureFlags.CollapseRoomStateEvents -> false
                FeatureFlags.ShowStartChatFlow -> false
                FeatureFlags.ShowMediaUploadingFlow -> false
            }
        } else {
            false
        }
    }

    override fun hasFeature(feature: Feature) = true
}
