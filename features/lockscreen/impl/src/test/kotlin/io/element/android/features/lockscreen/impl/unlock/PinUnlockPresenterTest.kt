/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.biometric.FakeBiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.fixtures.aPinCodeManager
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.pin.model.assertText
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import io.element.android.features.logout.test.FakeLogoutUseCase
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinUnlockPresenterTest {
    private val halfCompletePin = "12"
    private val completePin = "1235"

    @Test
    fun `present - success verify flow`() = runTest {
        val presenter = createPinUnlockPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Uninitialized::class.java)
                assertThat(state.showWrongPinTitle).isFalse()
                assertThat(state.showSignOutPrompt).isFalse()
                assertThat(state.isUnlocked).isFalse()
                assertThat(state.signOutAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Uninitialized::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('2')))
            }
            skipItems(1)
            awaitItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Back))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Empty))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('5')))
            }
            skipItems(4)
            awaitItem().also { state ->
                state.pinEntry.assertText(completePin)
                assertThat(state.isUnlocked).isTrue()
            }
        }
    }

    @Test
    fun `present - failure verify flow`() = runTest {
        val presenter = createPinUnlockPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
            }
            val numberOfAttempts = initialState.remainingAttempts.dataOrNull() ?: 0
            repeat(numberOfAttempts) {
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('2')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                initialState.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('4')))
            }
            skipItems(4 * numberOfAttempts + 2)
            awaitItem().also { state ->
                assertThat(state.remainingAttempts.dataOrNull()).isEqualTo(0)
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isFalse()
            }
        }
    }

    @Test
    fun `present - forgot pin flow`() = runTest {
        val signOutLambda = lambdaRecorder<Boolean, Unit> {}
        val signOut = FakeLogoutUseCase(signOutLambda)
        val presenter = createPinUnlockPresenter(logoutUseCase = signOut)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
                state.eventSink(PinUnlockEvents.OnForgetPin)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isTrue()
                state.eventSink(PinUnlockEvents.ClearSignOutPrompt)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isFalse()
                state.eventSink(PinUnlockEvents.OnForgetPin)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                state.eventSink(PinUnlockEvents.SignOut)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.signOutAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            assert(signOutLambda).isCalledOnce()
        }
    }

    private fun AsyncData<PinEntry>.assertText(text: String) {
        dataOrNull()?.assertText(text)
    }

    private suspend fun TestScope.createPinUnlockPresenter(
        biometricAuthenticatorManager: BiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(),
        callback: PinCodeManager.Callback = DefaultPinCodeManagerCallback(),
        logoutUseCase: FakeLogoutUseCase = FakeLogoutUseCase(logoutLambda = { "" }),
    ): PinUnlockPresenter {
        val pinCodeManager = aPinCodeManager().apply {
            addCallback(callback)
            createPinCode(completePin)
        }
        return PinUnlockPresenter(
            pinCodeManager = pinCodeManager,
            biometricAuthenticatorManager = biometricAuthenticatorManager,
            logoutUseCase = logoutUseCase,
            coroutineScope = this,
            pinUnlockHelper = PinUnlockHelper(biometricAuthenticatorManager, pinCodeManager),
        )
    }
}
