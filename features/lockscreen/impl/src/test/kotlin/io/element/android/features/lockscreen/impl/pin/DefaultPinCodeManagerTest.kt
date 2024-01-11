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
