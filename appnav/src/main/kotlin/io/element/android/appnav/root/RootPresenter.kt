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

package io.element.android.appnav.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import im.vector.app.features.analytics.plan.SuperProperties
import io.element.android.features.rageshake.api.crash.CrashDetectionPresenter
import io.element.android.features.rageshake.api.detection.RageshakeDetectionPresenter
import io.element.android.features.share.api.ShareService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.SdkMetadata
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.apperror.api.AppErrorStateService
import javax.inject.Inject

class RootPresenter @Inject constructor(
    private val crashDetectionPresenter: CrashDetectionPresenter,
    private val rageshakeDetectionPresenter: RageshakeDetectionPresenter,
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
