/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { emptyList() }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                        isValid = true,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isTrue()
        }
    }

    @Test
    fun `present - fixed list of account providers`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = {
                    listOf(AN_ACCOUNT_PROVIDER, AN_ACCOUNT_PROVIDER_2)
                }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                        isValid = true,
                    ),
                    AccountProvider(
                        url = "https://element.io",
                        title = "element.io",
                        subtitle = null,
                        isPublic = false,
                        isMatrixOrg = false,
                        isValid = true,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isFalse()
        }
    }

    @Test
    fun `present - opened list of account providers`() = runTest {
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter = { aChangeServerState() },
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = {
                    listOf(AN_ACCOUNT_PROVIDER, EnterpriseService.ANY_ACCOUNT_PROVIDER)
                }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        url = "https://matrix.org",
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                        isValid = true,
                    )
                )
            )
            assertThat(initialState.canSearchForAccountProviders).isTrue()
        }
    }
}
