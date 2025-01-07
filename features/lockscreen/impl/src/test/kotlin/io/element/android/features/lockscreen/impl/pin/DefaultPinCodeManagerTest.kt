/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import io.element.android.libraries.cryptography.impl.AESEncryptionDecryptionService
import io.element.android.libraries.cryptography.test.SimpleSecretKeyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPinCodeManagerTest {
    private val lockScreenStore = InMemoryLockScreenStore()
    private val secretKeyRepository = SimpleSecretKeyRepository()
    private val encryptionDecryptionService = AESEncryptionDecryptionService()
    private val pinCodeManager = DefaultPinCodeManager(secretKeyRepository, encryptionDecryptionService, lockScreenStore)

    @Test
    fun `given a pin code when create and delete assert no pin code left`() = runTest {
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
        val pinCode = "1234"
        pinCodeManager.createPinCode(pinCode)
        assertThat(pinCodeManager.verifyPinCode(pinCode)).isTrue()
    }

    @Test
    fun `given a pin code when create and verify with a different pin fails`() = runTest {
        pinCodeManager.createPinCode("1234")
        assertThat(pinCodeManager.verifyPinCode("1235")).isFalse()
    }
}
