/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.R
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountProviderDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService())
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                AccountProvider(
                    url = FakeEnterpriseService.A_FAKE_HOMESERVER,
                    title = FakeEnterpriseService.A_FAKE_HOMESERVER,
                    subtitleResourceId = null,
                    isPublic = false,
                    isMatrixOrg = false,
                    isValid = false,
                )
            )
        }
    }

    @Test
    fun `present - initial state - matrix org`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService(
            defaultHomeserverResult = { AuthenticationConfig.MATRIX_ORG_URL }
        ))
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                AccountProvider(
                    url = "https://matrix.org",
                    title = "matrix.org",
                    subtitleResourceId = R.string.screen_change_account_provider_matrix_org_subtitle,
                    isPublic = true,
                    isMatrixOrg = true,
                    isValid = false,
                )
            )
        }
    }

    @Test
    fun `present - user change and reset`() = runTest {
        val sut = AccountProviderDataSource(FakeEnterpriseService())
        sut.flow.test {
            val initialState = awaitItem()
            assertThat(initialState.url).isEqualTo(FakeEnterpriseService.A_FAKE_HOMESERVER)
            sut.userSelection(AccountProvider(url = "https://example.com"))
            val changedState = awaitItem()
            assertThat(changedState).isEqualTo(
                AccountProvider(
                    url = "https://example.com",
                    title = "example.com",
                    subtitleResourceId = null,
                    isPublic = false,
                    isMatrixOrg = false,
                    isValid = false,
                )
            )
            sut.reset()
            val resetState = awaitItem()
            assertThat(resetState.url).isEqualTo(FakeEnterpriseService.A_FAKE_HOMESERVER)
        }
    }
}
