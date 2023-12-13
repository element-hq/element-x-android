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

package io.element.android.features.lockscreen.impl.unlock

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
import io.element.android.features.lockscreen.impl.biometric.FakeBiometricUnlockManager
import io.element.android.features.lockscreen.impl.fixtures.aPinCodeManager
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.pin.model.assertText
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinUnlockPresenterTest {

    private val halfCompletePin = "12"
    private val completePin = "1235"

    @Test
    fun `present - success verify flow`() = runTest {
        val presenter = createPinUnlockPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(Async.Uninitialized::class.java)
                assertThat(state.showWrongPinTitle).isFalse()
                assertThat(state.showSignOutPrompt).isFalse()
                assertThat(state.isUnlocked).isFalse()
                assertThat(state.signOutAction).isInstanceOf(Async.Uninitialized::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(Async.Uninitialized::class.java)
            }
            consumeItemsUntilPredicate {
                it.pinEntry is Async.Success && it.remainingAttempts is Async.Success
            }.last().also { state ->
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('2')))
            }
            awaitLastSequentialItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Back))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Empty))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('5')))
            }
            awaitLastSequentialItem().also { state ->
                state.pinEntry.assertText(completePin)
                assertThat(state.isUnlocked).isTrue()
            }
        }
    }

    @Test
    fun `present - failure verify flow`() = runTest {
        val presenter = createPinUnlockPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate {
                it.pinEntry is Async.Success && it.remainingAttempts is Async.Success
            }.last()
            val numberOfAttempts = initialState.remainingAttempts.dataOrNull() ?: 0
            repeat(numberOfAttempts) {
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('2')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('4')))
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.remainingAttempts.dataOrNull()).isEqualTo(0)
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isFalse()
            }
        }
    }

    @Test
    fun `present - forgot pin flow`() = runTest {
        val presenter = createPinUnlockPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            consumeItemsUntilPredicate {
                it.pinEntry is Async.Success && it.remainingAttempts is Async.Success
            }.last().also { state ->
                state.eventSink(PinUnlockEvents.OnForgetPin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isTrue()
                state.eventSink(PinUnlockEvents.ClearSignOutPrompt)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showSignOutPrompt).isFalse()
                state.eventSink(PinUnlockEvents.OnForgetPin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                state.eventSink(PinUnlockEvents.SignOut)
            }
            consumeItemsUntilPredicate { state ->
                state.signOutAction is Async.Success
            }
        }
    }

    private fun Async<PinEntry>.assertText(text: String) {
        dataOrNull()?.assertText(text)
    }

    private suspend fun createPinUnlockPresenter(
        scope: CoroutineScope,
        biometricUnlockManager: BiometricUnlockManager = FakeBiometricUnlockManager(),
        callback: PinCodeManager.Callback = DefaultPinCodeManagerCallback(),
    ): PinUnlockPresenter {
        val pinCodeManager = aPinCodeManager().apply {
            addCallback(callback)
            createPinCode(completePin)
        }
        return PinUnlockPresenter(
            pinCodeManager = pinCodeManager,
            biometricUnlockManager = biometricUnlockManager,
            matrixClient = FakeMatrixClient(),
            coroutineScope = scope,
            pinUnlockHelper = PinUnlockHelper(biometricUnlockManager, pinCodeManager),
        )
    }
}
