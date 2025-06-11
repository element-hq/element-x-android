/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.features.login.impl.web.FakeWebClientUrlForAuthenticationRetriever
import io.element.android.features.login.impl.web.WebClientUrlForAuthenticationRetriever
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_3
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_LOGIN_HINT
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.oidc.api.OidcActionFlow
import io.element.android.libraries.oidc.test.customtab.FakeOidcActionFlow
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnBoardingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val ACCOUNT_PROVIDER_FROM_LINK = AN_ACCOUNT_PROVIDER
        private const val ACCOUNT_PROVIDER_FROM_CONFIG = AN_ACCOUNT_PROVIDER_2
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_2 = AN_ACCOUNT_PROVIDER_3
    }

    @Test
    fun `present - ensure initial conditions`() {
        assertThat(
            setOf(
                ACCOUNT_PROVIDER_FROM_LINK,
                ACCOUNT_PROVIDER_FROM_CONFIG,
                ACCOUNT_PROVIDER_FROM_CONFIG_2,
            ).size
        ).isEqualTo(3)
    }

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
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, EnterpriseService.ANY_ACCOUNT_PROVIDER) },
            ),
            rageshakeFeatureAvailability = { true },
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.defaultAccountProvider).isNull()
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
            rageshakeFeatureAvailability = { false },
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().canReportBug).isFalse()
        }
    }

    @Test
    fun `present - opening the app using link with allowed account provider, and the app does not force account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.QrCodeLogin.key to true),
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, EnterpriseService.ANY_ACCOUNT_PROVIDER) },
                isAllowedToConnectToHomeserverResult = { true },
            ),
        )
        presenter.test {
            skipItems(3)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(ACCOUNT_PROVIDER_FROM_LINK)
                assertThat(it.canLoginWithQrCode).isFalse()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - opening the app using link with not allowed account provider, and the app does not force account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.QrCodeLogin.key to true),
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
                isAllowedToConnectToHomeserverResult = { false },
            ),
        )
        presenter.test {
            skipItems(1)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isNull()
                assertThat(it.canLoginWithQrCode).isTrue()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - opening the app using link, and the app forces account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.QrCodeLogin.key to true),
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG) },
            )
        )
        presenter.test {
            skipItems(1)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(ACCOUNT_PROVIDER_FROM_CONFIG)
                assertThat(it.canLoginWithQrCode).isTrue()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - default account provider - login and clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = A_HOMESERVER_URL,
                loginHint = A_LOGIN_HINT,
            ),
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToHomeserverResult = { true },
            ),
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            skipItems(3)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(A_HOMESERVER_URL)
                authenticationService.givenChangeServerError(AN_EXCEPTION)
                it.eventSink(OnBoardingEvents.OnSignIn(A_HOMESERVER_URL))
                skipItems(1) // Loading

                // Check an error was returned
                val submittedState = awaitItem()
                assertThat(submittedState.loginMode).isInstanceOf(AsyncData.Failure::class.java)

                // Assert the error is then cleared
                submittedState.eventSink(OnBoardingEvents.ClearError)
                val clearedState = awaitItem()
                assertThat(clearedState.loginMode).isEqualTo(AsyncData.Uninitialized)
            }
        }
    }
}

private fun createPresenter(
    params: OnBoardingNode.Params = OnBoardingNode.Params(null, null),
    buildMeta: BuildMeta = aBuildMeta(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    rageshakeFeatureAvailability: () -> Boolean = { true },
    loginHelper: LoginHelper = createLoginHelper(),
) = OnBoardingPresenter(
    params = params,
    buildMeta = buildMeta,
    featureFlagService = featureFlagService,
    enterpriseService = enterpriseService,
    rageshakeFeatureAvailability = rageshakeFeatureAvailability,
    loginHelper = loginHelper,
)

fun createLoginHelper(
    oidcActionFlow: OidcActionFlow = FakeOidcActionFlow(),
    authenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
    defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory(),
    webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever(),
): LoginHelper = LoginHelper(
    oidcActionFlow = oidcActionFlow,
    authenticationService = authenticationService,
    defaultLoginUserStory = defaultLoginUserStory,
    webClientUrlForAuthenticationRetriever = webClientUrlForAuthenticationRetriever,
)
