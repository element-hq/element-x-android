/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags

class OnBoardingPresenter @AssistedInject constructor(
    @Assisted private val params: OnBoardingNode.Params,
    private val buildMeta: BuildMeta,
    private val featureFlagService: FeatureFlagService,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val loginHelper: LoginHelper,
) : Presenter<OnBoardingState> {
    @AssistedFactory
    interface Factory {
        fun create(
            params: OnBoardingNode.Params,
        ): OnBoardingPresenter
    }

    private val defaultAccountProvider = params.accountProvider
    private val loginHint = params.loginHint

    @Composable
    override fun present(): OnBoardingState {
        val localCoroutineScope = rememberCoroutineScope()

        val canLoginWithQrCode by produceState(initialValue = false) {
            value = defaultAccountProvider == null &&
                featureFlagService.isFeatureEnabled(FeatureFlags.QrCodeLogin)
        }
        val canReportBug = remember { rageshakeFeatureAvailability.isAvailable() }

        loginHelper.Start()

        fun handleEvent(event: OnBoardingEvents) {
            when (event) {
                is OnBoardingEvents.OnSignIn -> loginHelper.submit(
                    coroutineScope = localCoroutineScope,
                    isAccountCreation = false,
                    homeserverUrl = event.defaultAccountProvider,
                    loginHint = loginHint,
                )
                OnBoardingEvents.ClearError -> loginHelper.clearError()
            }
        }

        return OnBoardingState(
            productionApplicationName = buildMeta.productionApplicationName,
            defaultAccountProvider = defaultAccountProvider,
            canLoginWithQrCode = canLoginWithQrCode,
            canCreateAccount = defaultAccountProvider == null && OnBoardingConfig.CAN_CREATE_ACCOUNT,
            canReportBug = canReportBug,
            loginFlow = loginHelper.loginFlow,
            eventSink = ::handleEvent,
        )
    }
}
