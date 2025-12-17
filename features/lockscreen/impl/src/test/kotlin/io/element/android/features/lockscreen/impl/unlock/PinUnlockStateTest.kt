/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock

import androidx.biometric.BiometricPrompt
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticator
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockError
import io.element.android.libraries.architecture.AsyncData
import org.junit.Test

class PinUnlockStateTest {
    @Test
    fun `isSignOutPromptCancellable should have expected values`() {
        assertThat(aPinUnlockState(remainingAttempts = AsyncData.Uninitialized).isSignOutPromptCancellable).isTrue()
        assertThat(aPinUnlockState(remainingAttempts = AsyncData.Success(1)).isSignOutPromptCancellable).isTrue()
        assertThat(aPinUnlockState(remainingAttempts = AsyncData.Success(0)).isSignOutPromptCancellable).isFalse()
    }

    @Test
    fun `biometricUnlockErrorMessage and showBiometricUnlockError should have expected values`() {
        listOf(
            null,
            BiometricAuthenticator.AuthenticationResult.Failure(),
            BiometricAuthenticator.AuthenticationResult.Success,
        ).forEach { biometricUnlockResult ->
            aPinUnlockState(
                biometricUnlockResult = biometricUnlockResult,
            ).let {
                assertThat(it.biometricUnlockErrorMessage).isNull()
                assertThat(it.showBiometricUnlockError).isFalse()
            }
        }
        listOf(
            BiometricPrompt.ERROR_HW_UNAVAILABLE,
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
            BiometricPrompt.ERROR_TIMEOUT,
            BiometricPrompt.ERROR_NO_SPACE,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_VENDOR,
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
            BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
        ).forEach { code ->
            aPinUnlockState(
                biometricUnlockResult = BiometricAuthenticator.AuthenticationResult.Failure(
                    error = BiometricUnlockError(code, "Error message")
                ),
            ).let {
                assertThat(it.biometricUnlockErrorMessage).isNull()
                assertThat(it.showBiometricUnlockError).isFalse()
            }
        }
        listOf(
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
        ).forEach { code ->
            aPinUnlockState(
                biometricUnlockResult = BiometricAuthenticator.AuthenticationResult.Failure(
                    error = BiometricUnlockError(code, "Error message")
                ),
            ).let {
                assertThat(it.biometricUnlockErrorMessage).isEqualTo("Error message")
                assertThat(it.showBiometricUnlockError).isTrue()
            }
        }
    }
}
