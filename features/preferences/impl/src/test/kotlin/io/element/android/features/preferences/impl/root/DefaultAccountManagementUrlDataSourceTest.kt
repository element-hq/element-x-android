/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAccountManagementUrlDataSourceTest {
    @Test
    fun `when the account is not manage externally, the url should be emitted once`() = runTest {
        val sut = DefaultAccountManagementUrlDataSource(
            matrixClient = FakeMatrixClient(
                accountManagementUrlString = Result.success(null),
            ),
            sessionPreferencesStore = InMemorySessionPreferencesStore(),
        )

        sut.getAccountManagementUrl(null).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `when there is an error, the url should be emitted once`() = runTest {
        val store = InMemorySessionPreferencesStore()
        store.setString("account_management_url", "aStoredUrl")
        val sut = DefaultAccountManagementUrlDataSource(
            matrixClient = FakeMatrixClient(
                accountManagementUrlString = Result.failure(Exception("An error")),
            ),
            sessionPreferencesStore = store,
        )

        sut.getAccountManagementUrl(null).test {
            assertThat(awaitItem()).isEqualTo("aStoredUrl")
            awaitComplete()
        }
    }

    @Test
    fun `when the account is managed externally, the url should be emitted twice, and the store should be updated`() = runTest {
        val store = InMemorySessionPreferencesStore()
        store.setString("account_management_url", "aStoredUrl")
        val sut = DefaultAccountManagementUrlDataSource(
            matrixClient = FakeMatrixClient(
                accountManagementUrlString = Result.success("aUrl"),
            ),
            sessionPreferencesStore = store,
        )

        sut.getAccountManagementUrl(null).test {
            assertThat(awaitItem()).isEqualTo("aStoredUrl")
            assertThat(awaitItem()).isEqualTo("aUrl")
            awaitComplete()
        }
        assertThat(store.getString("account_management_url").first()).isEqualTo("aUrl")
    }

    @Test
    fun `test key converter`() = runTest {
        assertThat(AccountManagementAction.Profile.toKey()).isEqualTo("account_management_url_profile")
        assertThat(AccountManagementAction.SessionEnd(A_DEVICE_ID).toKey()).isEqualTo("account_management_url_session_end_ILAKNDNASDLK")
        assertThat(AccountManagementAction.SessionView(A_DEVICE_ID).toKey()).isEqualTo("account_management_url_session_view_ILAKNDNASDLK")
        assertThat(AccountManagementAction.SessionsList.toKey()).isEqualTo("account_management_url_sessions_list")
        assertThat(null.toKey()).isEqualTo("account_management_url")
    }
}
