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

package io.element.android.features.lockscreen.impl.setup.biometric

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetupBiometricPresenterTest {
    @Test
    fun `present - allow flow`() = runTest {
        val lockScreenStore = InMemoryLockScreenStore()
        val presenter = createSetupBiometricPresenter(lockScreenStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.isBiometricSetupDone).isFalse()
                state.eventSink(SetupBiometricEvents.AllowBiometric)
            }
            awaitItem().also { state ->
                assertThat(state.isBiometricSetupDone).isTrue()
            }
        }
        assertThat(lockScreenStore.isBiometricUnlockAllowed().first()).isTrue()
    }

    @Test
    fun `present - skip flow`() = runTest {
        val lockScreenStore = InMemoryLockScreenStore()
        val presenter = createSetupBiometricPresenter(lockScreenStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.isBiometricSetupDone).isFalse()
                state.eventSink(SetupBiometricEvents.UsePin)
            }
            awaitItem().also { state ->
                assertThat(state.isBiometricSetupDone).isTrue()
            }
        }
        assertThat(lockScreenStore.isBiometricUnlockAllowed().first()).isFalse()
    }

    private fun createSetupBiometricPresenter(
        lockScreenStore: LockScreenStore = InMemoryLockScreenStore()
    ): SetupBiometricPresenter {
        return SetupBiometricPresenter(
            lockScreenStore = lockScreenStore,
        )
    }
}
