/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
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
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
) : Presenter<OnBoardingState> {
    @Composable
    override fun present(): OnBoardingState {
        val canLoginWithQrCode by produceState(initialValue = false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.QrCodeLogin)
        }
        val canReportBug = remember { rageshakeFeatureAvailability.isAvailable() }
        return OnBoardingState(
            productionApplicationName = buildMeta.productionApplicationName,
            canLoginWithQrCode = canLoginWithQrCode,
            canCreateAccount = OnBoardingConfig.CAN_CREATE_ACCOUNT,
            canReportBug = canReportBug,
        )
    }
}
