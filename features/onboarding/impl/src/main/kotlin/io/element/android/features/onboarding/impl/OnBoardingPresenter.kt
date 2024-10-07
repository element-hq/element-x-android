/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
