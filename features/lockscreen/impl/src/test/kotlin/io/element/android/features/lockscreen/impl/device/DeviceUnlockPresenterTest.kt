/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.lockscreen.impl.device

import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.FakeBiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.FakeBiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.fixtures.aPinCodeManager
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.unlock.PinUnlockHelper
import io.element.android.features.lockscreen.test.FakeDeviceUnlockEntryPointCallback
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeviceUnlockPresenterTest {
    @Test
    fun `present - when unlock requested and device unlock available, use biometric authenticator`() = runTest {
        val setupLambda = lambdaRecorder<Unit> { }
        val authenticateLambda = lambdaRecorder<BiometricAuthenticator.AuthenticationResult> {
            BiometricAuthenticator.AuthenticationResult.Success
        }
        val fakeBiometricAuthenticator = FakeBiometricAuthenticator(
            setupLambda = setupLambda,
            authenticateLambda = authenticateLambda,
        )
        val biometricAuthenticatorManager = FakeBiometricAuthenticatorManager(
            canUseDeviceUnlock = true,
            createBiometricAuthenticator = { fakeBiometricAuthenticator },
        )
        val callbackHolder = DeviceUnlockCallbackHolder()
        val callback = FakeDeviceUnlockEntryPointCallback()

        createDeviceUnlockPresenter(
            biometricAuthenticatorManager = biometricAuthenticatorManager,
            callbackHolder = callbackHolder,
        ).test {
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isFalse()
            }
            callbackHolder.requestUnlock(callback)
            advanceUntilIdle()
            setupLambda.assertions().isCalledOnce()
            authenticateLambda.assertions().isCalledOnce()
            skipItems(1)
        }
    }

    @Test
    fun `present - when unlock requested and device unlock unavailable and app pin is configured, show app pin`() = runTest {
        val callbackHolder = DeviceUnlockCallbackHolder()
        val callback = FakeDeviceUnlockEntryPointCallback()
        val pinCodeManager = aPinCodeManager().apply {
            createPinCode("1234")
        }
        createDeviceUnlockPresenter(
            biometricAuthenticatorManager = FakeBiometricAuthenticatorManager(canUseDeviceUnlock = false),
            callbackHolder = callbackHolder,
            pinCodeManager = pinCodeManager,
        ).test {
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isFalse()
            }

            callbackHolder.requestUnlock(callback)
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isTrue()
            }
        }
    }

    @Test
    fun `present - when unlock requested and no security, unlock immediately`() = runTest {
        val callbackHolder = DeviceUnlockCallbackHolder()
        val onUnlockedLambda = lambdaRecorder<Unit> { }
        val callback = FakeDeviceUnlockEntryPointCallback(
            onUnlockedLambda = onUnlockedLambda,
        )
        createDeviceUnlockPresenter(
            biometricAuthenticatorManager = FakeBiometricAuthenticatorManager(canUseDeviceUnlock = false),
            callbackHolder = callbackHolder,
        ).test {
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isFalse()
            }
            callbackHolder.requestUnlock(callback)
            skipItems(2)
            assertThat(callbackHolder.deviceUnlockCallback.value).isNull()
            onUnlockedLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - CancelPinCode event cancels unlock request`() = runTest {
        val callbackHolder = DeviceUnlockCallbackHolder()
        val onCancelLambda = lambdaRecorder<Unit> { }
        val callback = FakeDeviceUnlockEntryPointCallback(
            onCancelLambda = onCancelLambda,
        )
        val pinCodeManager = aPinCodeManager().apply {
            createPinCode("1234")
        }
        createDeviceUnlockPresenter(
            biometricAuthenticatorManager = FakeBiometricAuthenticatorManager(canUseDeviceUnlock = false),
            callbackHolder = callbackHolder,
            pinCodeManager = pinCodeManager,
        ).test {
            awaitItem()
            callbackHolder.requestUnlock(callback)
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isTrue()
                state.eventSink(DeviceUnlockEvent.CancelPinCode)
            }
            awaitItem().also { state ->
                assertThat(state.showApplicationPinCode).isFalse()
            }
            skipItems(1)
            onCancelLambda.assertions().isCalledOnce()
            assertThat(callbackHolder.deviceUnlockCallback.value).isNull()
        }
    }

    private fun createDeviceUnlockPresenter(
        biometricAuthenticatorManager: FakeBiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(),
        callbackHolder: DeviceUnlockCallbackHolder = DeviceUnlockCallbackHolder(),
        pinCodeManager: PinCodeManager = aPinCodeManager(),
    ): DeviceUnlockPresenter {
        val pinUnlockHelper = PinUnlockHelper(
            biometricAuthenticatorManager = biometricAuthenticatorManager,
            pinCodeManager = pinCodeManager,
        )
        return DeviceUnlockPresenter(
            pinUnlockHelper = pinUnlockHelper,
            biometricAuthenticatorManager = biometricAuthenticatorManager,
            deviceUnlockCallbackHolder = callbackHolder,
            pinCodeManager = pinCodeManager,
        )
    }
}
