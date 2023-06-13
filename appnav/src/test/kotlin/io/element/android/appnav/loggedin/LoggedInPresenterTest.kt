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

package io.element.android.appnav.loggedin

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.aFakeMatrixClient
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoggedInPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permissionsState.permission).isEmpty()
        }
    }

    private fun TestScope.createPresenter(): LoggedInPresenter {
        return LoggedInPresenter(
            matrixClient = aFakeMatrixClient(),
            permissionsPresenterFactory = object : PermissionsPresenter.Factory {
                override fun create(permission: String): PermissionsPresenter {
                    return NoopPermissionsPresenter()
                }
            },
            pushService = object : PushService {
                override fun notificationStyleChanged() {
                }

                override fun getAvailablePushProviders(): List<PushProvider> {
                    return emptyList()
                }

                override suspend fun registerWith(matrixClient: MatrixClient, pushProvider: PushProvider, distributor: Distributor) {
                }

                override suspend fun testPush() {
                }
            }
        )
    }
}
