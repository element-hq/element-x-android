/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.features.login.impl.screens.onboarding.createLoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_3
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_1 = AN_ACCOUNT_PROVIDER_2
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_2 = AN_ACCOUNT_PROVIDER_3
        val accountProvider1 = AccountProvider(
            url = ACCOUNT_PROVIDER_FROM_CONFIG_1.ensureProtocol(),
            subtitle = null,
            isPublic = false,
            isMatrixOrg = false,
        )
        val accountProvider2 = AccountProvider(
            url = ACCOUNT_PROVIDER_FROM_CONFIG_2.ensureProtocol(),
            subtitle = null,
            isPublic = false,
            isMatrixOrg = false,
        )
    }

    @Test
    fun `present - ensure initial conditions`() {
        assertThat(
            setOf(
                ACCOUNT_PROVIDER_FROM_CONFIG_1,
                ACCOUNT_PROVIDER_FROM_CONFIG_2,
            ).size
        ).isEqualTo(2)
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG_1, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).containsExactly(
                accountProvider1,
                accountProvider2,
            )
            assertThat(initialState.selectedAccountProvider).isNull()
        }
    }

    @Test
    fun `present - Continue when no account provider is selected has no effect`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG_1, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
            ),
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvents.Continue)
                expectNoEvents()
            }
        }
    }

    @Test
    fun `present - select account provider and continue - error then clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG_1, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
            ),
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvents.SelectAccountProvider(accountProvider1))
            }
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isEqualTo(accountProvider1)
                it.eventSink(ChooseAccountProviderEvents.Continue)
                skipItems(1) // Loading

                // Check an error was returned
                val submittedState = awaitItem()
                assertThat(submittedState.loginMode).isInstanceOf(AsyncData.Failure::class.java)

                // Assert the error is then cleared
                submittedState.eventSink(ChooseAccountProviderEvents.ClearError)
                val clearedState = awaitItem()
                assertThat(clearedState.loginMode).isEqualTo(AsyncData.Uninitialized)
            }
        }
    }

    @Test
    fun `present - default account provider - select account provider during login has no effect`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG_1, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
            ),
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvents.SelectAccountProvider(accountProvider1))
            }
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isEqualTo(accountProvider1)
                it.eventSink(ChooseAccountProviderEvents.Continue)
            }
            awaitItem().also {
                assertThat(it.loginMode.isLoading()).isTrue()
                it.eventSink(ChooseAccountProviderEvents.SelectAccountProvider(accountProvider2))
            }
            expectNoEvents()
        }
    }
}

private fun createPresenter(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    loginHelper: LoginHelper = createLoginHelper(),
) = ChooseAccountProviderPresenter(
    enterpriseService = enterpriseService,
    loginHelper = loginHelper,
)
