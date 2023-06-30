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

package io.element.android.features.preferences.impl.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.analytics.impl.preferences.DefaultAnalyticsPreferencesPresenter
import io.element.android.features.analytics.test.A_BUILD_META
import io.element.android.features.analytics.test.FakeAnalyticsService
import io.element.android.features.logout.impl.DefaultLogoutPreferencePresenter
import io.element.android.features.rageshake.impl.preferences.DefaultRageshakePreferencesPresenter
import io.element.android.features.rageshake.test.rageshake.FakeRageShake
import io.element.android.features.rageshake.test.rageshake.FakeRageshakeDataStore
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.user.CurrentUserProvider
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PreferencesRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient()
        val logoutPresenter = DefaultLogoutPreferencePresenter(matrixClient)
        val presenter = PreferencesRootPresenter(
            logoutPresenter,
            CurrentUserProvider(matrixClient),
            A_BUILD_META.buildType
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isNull()
            val loadedState = awaitItem()
            assertThat(loadedState.logoutState.logoutAction).isEqualTo(Async.Uninitialized)
            assertThat(loadedState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(loadedState.showDeveloperSettings).isEqualTo(true)
        }
    }
}
