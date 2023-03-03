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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.LogoutPreferencePresenter
import io.element.android.features.rageshake.preferences.RageshakePreferencesPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.FakeMatrixClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PreferencesRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val logoutPresenter = LogoutPreferencePresenter(FakeMatrixClient())
        val rageshakePresenter = RageshakePreferencesPresenter(FakeRageShake(), FakeRageshakeDataStore())
        val presenter = PreferencesRootPresenter(
            logoutPresenter, rageshakePresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.logoutState.logoutAction).isEqualTo(Async.Uninitialized)
            assertThat(initialState.rageshakeState.isEnabled).isTrue()
            assertThat(initialState.rageshakeState.isSupported).isTrue()
            assertThat(initialState.rageshakeState.sensitivity).isEqualTo(1.0f)
            assertThat(initialState.myUser).isEqualTo(Async.Uninitialized)
        }
    }
}
