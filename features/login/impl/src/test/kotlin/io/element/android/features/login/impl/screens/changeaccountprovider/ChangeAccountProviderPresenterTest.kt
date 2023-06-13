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

package io.element.android.features.login.impl.screens.changeaccountprovider

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.changeserver.ChangeServerPresenter
import io.element.android.libraries.matrix.test.auth.aFakeAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeAccountProviderPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val changeServerPresenter = ChangeServerPresenter(
            aFakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderPresenter(
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).isEqualTo(
                listOf(
                    AccountProvider(
                        title = "matrix.org",
                        subtitle = null,
                        isPublic = true,
                        isMatrixOrg = true,
                        isValid = true,
                        supportSlidingSync = true,
                    )
                )
            )
        }
    }
}
