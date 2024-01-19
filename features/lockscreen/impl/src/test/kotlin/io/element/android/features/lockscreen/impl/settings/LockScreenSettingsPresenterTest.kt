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

package io.element.android.features.lockscreen.impl.settings

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.biometric.FakeBiometricUnlockManager
import io.element.android.features.lockscreen.impl.fixtures.aLockScreenConfig
import io.element.android.features.lockscreen.impl.fixtures.aPinCodeManager
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LockScreenSettingsPresenterTest {
    @Test
    fun `present - remove pin flow`() = runTest {
        val presenter = createLockScreenSettingsPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            consumeItemsUntilPredicate { state ->
                state.showRemovePinOption
            }.last().also { state ->
                state.eventSink(LockScreenSettingsEvents.OnRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isTrue()
                state.eventSink(LockScreenSettingsEvents.CancelRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isFalse()
                state.eventSink(LockScreenSettingsEvents.OnRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isTrue()
                state.eventSink(LockScreenSettingsEvents.ConfirmRemovePin)
            }
            consumeItemsUntilPredicate {
                it.showRemovePinOption.not()
            }.last().also { state ->
                assertThat(state.showRemovePinConfirmation).isFalse()
                assertThat(state.showRemovePinOption).isFalse()
            }
        }
    }

    private suspend fun createLockScreenSettingsPresenter(
        coroutineScope: CoroutineScope,
        lockScreenConfig: LockScreenConfig = aLockScreenConfig(),
    ): LockScreenSettingsPresenter {
        val lockScreenStore = InMemoryLockScreenStore()
        val pinCodeManager = aPinCodeManager(lockScreenStore = lockScreenStore).apply {
            createPinCode("1234")
        }
        return LockScreenSettingsPresenter(
            lockScreenStore = lockScreenStore,
            pinCodeManager = pinCodeManager,
            coroutineScope = coroutineScope,
            lockScreenConfig = lockScreenConfig,
            biometricUnlockManager = FakeBiometricUnlockManager(),
        )
    }
}
