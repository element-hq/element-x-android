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
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.api.canConnectToAnyHomeserver
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
    private val enterpriseService: EnterpriseService,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val loginHelper: LoginHelper,
) : Presenter<OnBoardingState> {
    @AssistedFactory
    interface Factory {
        fun create(
            params: OnBoardingNode.Params,
        ): OnBoardingPresenter
    }

    @Composable
    override fun present(): OnBoardingState {
        val localCoroutineScope = rememberCoroutineScope()
        val forcedAccountProvider = remember {
            // If defaultHomeserverList() returns a singleton list, this is the default account provider.
            // In this case, the user can sign in using this homeserver, or use QrCode login
            enterpriseService.defaultHomeserverList().singleOrNull()
        }
        val canConnectToAnyHomeserver = remember {
            enterpriseService.canConnectToAnyHomeserver()
        }
        val mustChooseAccountProvider = remember {
            !canConnectToAnyHomeserver && enterpriseService.defaultHomeserverList().size > 1
        }
        val linkAccountProvider by produceState<String?>(initialValue = null) {
            // Account provider from the link, if allowed by the enterprise service
            value = params.accountProvider?.takeIf {
                enterpriseService.isAllowedToConnectToHomeserver(it)
            }
        }
        val defaultAccountProvider = remember(linkAccountProvider) {
            // If there is a forced account provider, this is the default account provider
            // Else use the account provider passed in the params if any and if allowed
            forcedAccountProvider ?: linkAccountProvider
        }
        val canLoginWithQrCode by produceState(initialValue = false, linkAccountProvider) {
            value = linkAccountProvider == null &&
                featureFlagService.isFeatureEnabled(FeatureFlags.QrCodeLogin)
        }
        val canReportBug = remember { rageshakeFeatureAvailability.isAvailable() }

        val loginMode by loginHelper.collectLoginMode()

        fun handleEvent(event: OnBoardingEvents) {
            when (event) {
                is OnBoardingEvents.OnSignIn -> loginHelper.submit(
                    coroutineScope = localCoroutineScope,
                    isAccountCreation = false,
                    homeserverUrl = event.defaultAccountProvider,
                    loginHint = params.loginHint?.takeIf { forcedAccountProvider == null },
                )
                OnBoardingEvents.ClearError -> loginHelper.clearError()
            }
        }

        return OnBoardingState(
            productionApplicationName = buildMeta.productionApplicationName,
            defaultAccountProvider = defaultAccountProvider,
            mustChooseAccountProvider = mustChooseAccountProvider,
            canLoginWithQrCode = canLoginWithQrCode,
            canCreateAccount = defaultAccountProvider == null && canConnectToAnyHomeserver && OnBoardingConfig.CAN_CREATE_ACCOUNT,
            canReportBug = canReportBug,
            loginMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}
