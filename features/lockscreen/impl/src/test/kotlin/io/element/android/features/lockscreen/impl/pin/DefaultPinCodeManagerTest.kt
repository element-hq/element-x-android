/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import io.element.android.libraries.cryptography.impl.AESEncryptionDecryptionService
import io.element.android.libraries.cryptography.test.SimpleSecretKeyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPinCodeManagerTest {
    @Test
    fun `given a pin code when create and delete assert no pin code left`() = runTest {
        val pinCodeManager = createDefaultPinCodeManager()
        pinCodeManager.hasPinCode().test {
            assertThat(awaitItem()).isFalse()
            pinCodeManager.createPinCode("1234")
            assertThat(awaitItem()).isTrue()
            pinCodeManager.deletePinCode()
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `given a pin code when create and verify with the same pin succeed`() = runTest {
        val pinCodeManager = createDefaultPinCodeManager()
        val pinCode = "1234"
        pinCodeManager.createPinCode(pinCode)
        assertThat(pinCodeManager.verifyPinCode(pinCode)).isTrue()
    }

    @Test
    fun `given a pin code when create and verify with a different pin fails`() = runTest {
        val pinCodeManager = createDefaultPinCodeManager()
        pinCodeManager.createPinCode("1234")
        assertThat(pinCodeManager.verifyPinCode("1235")).isFalse()
    }
}

fun createDefaultPinCodeManager(
    lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    secretKeyRepository: SecretKeyRepository = SimpleSecretKeyRepository(),
    encryptionDecryptionService: EncryptionDecryptionService = AESEncryptionDecryptionService(),
) = DefaultPinCodeManager(
    lockScreenStore = lockScreenStore,
    secretKeyRepository = secretKeyRepository,
    encryptionDecryptionService = encryptionDecryptionService,
)
