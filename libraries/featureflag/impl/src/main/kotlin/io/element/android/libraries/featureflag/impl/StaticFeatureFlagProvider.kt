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

import io.element.android.appconfig.OnBoardingConfig
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * This provider is used for release build.
 * This is the place to enable or disable feature for the release build.
 */
class StaticFeatureFlagProvider @Inject constructor() :
    FeatureFlagProvider {
    override val priority = LOW_PRIORITY

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        val isFeatureEnabled = if (feature is FeatureFlags) {
            when (feature) {
                FeatureFlags.LocationSharing -> true
                FeatureFlags.Polls -> true
                FeatureFlags.NotificationSettings -> true
                FeatureFlags.VoiceMessages -> true
                FeatureFlags.PinUnlock -> true
                FeatureFlags.Mentions -> true
                FeatureFlags.MarkAsUnread -> true
                FeatureFlags.RoomDirectorySearch -> false
                FeatureFlags.ShowBlockedUsersDetails -> false
                FeatureFlags.QrCodeLogin -> OnBoardingConfig.CAN_LOGIN_WITH_QR_CODE
                FeatureFlags.IncomingShare -> true
            }
        } else {
            false
        }
        return flowOf(isFeatureEnabled)
    }

    override fun hasFeature(feature: Feature) = true
}
