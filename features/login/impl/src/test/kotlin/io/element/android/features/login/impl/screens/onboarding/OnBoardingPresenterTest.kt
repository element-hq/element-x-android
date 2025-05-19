/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.features.login.impl.web.FakeWebClientUrlForAuthenticationRetriever
import io.element.android.features.login.impl.web.WebClientUrlForAuthenticationRetriever
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.oidc.api.OidcActionFlow
import io.element.android.libraries.oidc.impl.customtab.DefaultOidcActionFlow
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnBoardingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val buildMeta = aBuildMeta(
            applicationName = "A",
            productionApplicationName = "B",
            desktopApplicationName = "C",
        )
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(FeatureFlags.QrCodeLogin.key to true),
            buildMeta = buildMeta,
        )
        val presenter = createPresenter(
            buildMeta = buildMeta,
            featureFlagService = featureFlagService,
            rageshakeFeatureAvailability = { true },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.canLoginWithQrCode).isFalse()
            assertThat(initialState.productionApplicationName).isEqualTo("B")
            assertThat(initialState.canCreateAccount).isEqualTo(OnBoardingConfig.CAN_CREATE_ACCOUNT)
            assertThat(initialState.canReportBug).isTrue()
            assertThat(awaitItem().canLoginWithQrCode).isTrue()
        }
    }

    @Test
    fun `present - rageshake not available`() = runTest {
        val presenter = createPresenter(
            buildMeta = aBuildMeta(),
            featureFlagService = FakeFeatureFlagService(),
            rageshakeFeatureAvailability = { false },
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().canReportBug).isFalse()
        }
    }

    private fun createPresenter(
        params: OnBoardingNode.Params = OnBoardingNode.Params(null, null),
        buildMeta: BuildMeta = aBuildMeta(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        rageshakeFeatureAvailability: () -> Boolean = { true },
        loginHelper: LoginHelper = createLoginHelper(),
    ) = OnBoardingPresenter(
        params = params,
        buildMeta = buildMeta,
        featureFlagService = featureFlagService,
        rageshakeFeatureAvailability = rageshakeFeatureAvailability,
        loginHelper = loginHelper,
    )
}

fun createLoginHelper(
    oidcActionFlow: OidcActionFlow = DefaultOidcActionFlow(),
    authenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
    defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory(),
    webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever(),
): LoginHelper = LoginHelper(
    oidcActionFlow = oidcActionFlow,
    authenticationService = authenticationService,
    defaultLoginUserStory = defaultLoginUserStory,
    webClientUrlForAuthenticationRetriever = webClientUrlForAuthenticationRetriever,
)
