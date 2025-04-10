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
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
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
            accountProviderDataSource = AccountProviderDataSource()
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
                        descriptionResourceId = R.string.screen_change_account_provider_matrix_org_subtitle,
                        isPublic = true,
                    )
                )
            )
            assertThat(initialState.accountProviders[0].isMatrixOrg()).isTrue()
        }
    }
}
