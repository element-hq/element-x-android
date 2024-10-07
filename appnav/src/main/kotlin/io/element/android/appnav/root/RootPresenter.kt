/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import im.vector.app.features.analytics.plan.SuperProperties
import io.element.android.features.rageshake.api.crash.CrashDetectionState
import io.element.android.features.rageshake.api.detection.RageshakeDetectionState
import io.element.android.features.share.api.ShareService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.SdkMetadata
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.apperror.api.AppErrorStateService
import javax.inject.Inject

class RootPresenter @Inject constructor(
    private val crashDetectionPresenter: Presenter<CrashDetectionState>,
    private val rageshakeDetectionPresenter: Presenter<RageshakeDetectionState>,
    private val appErrorStateService: AppErrorStateService,
    private val analyticsService: AnalyticsService,
    private val shareService: ShareService,
    private val sdkMetadata: SdkMetadata,
) : Presenter<RootState> {
    @Composable
    override fun present(): RootState {
        val rageshakeDetectionState = rageshakeDetectionPresenter.present()
        val crashDetectionState = crashDetectionPresenter.present()
        val appErrorState by appErrorStateService.appErrorStateFlow.collectAsState()

        LaunchedEffect(Unit) {
            analyticsService.updateSuperProperties(
                SuperProperties(
                    cryptoSDK = SuperProperties.CryptoSDK.Rust,
                    appPlatform = SuperProperties.AppPlatform.EXA,
                    cryptoSDKVersion = sdkMetadata.sdkGitSha,
                )
            )
        }

        LaunchedEffect(Unit) {
            shareService.observeFeatureFlag(this)
        }

        return RootState(
            rageshakeDetectionState = rageshakeDetectionState,
            crashDetectionState = crashDetectionState,
            errorState = appErrorState,
        )
    }
}
