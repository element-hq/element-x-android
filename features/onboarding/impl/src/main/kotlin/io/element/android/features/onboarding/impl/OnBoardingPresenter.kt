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

package io.element.android.features.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import javax.inject.Inject

/**
 * Note: this Presenter is ignored regarding code coverage because it cannot reach the coverage threshold.
 * When this presenter get more code in it, please remove the ignore rule in the kover configuration.
 */
class OnBoardingPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    private val featureFlagService: FeatureFlagService,
) : Presenter<OnBoardingState> {
    @Composable
    override fun present(): OnBoardingState {
        val canLoginWithQrCode by produceState(initialValue = false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.QrCodeLogin)
        }
         return OnBoardingState(
            isDebugBuild = buildMeta.buildType != BuildType.RELEASE,
            productionApplicationName = buildMeta.productionApplicationName,
            canLoginWithQrCode = canLoginWithQrCode,
            canCreateAccount = OnBoardingConfig.CAN_CREATE_ACCOUNT,
        )
    }
}
