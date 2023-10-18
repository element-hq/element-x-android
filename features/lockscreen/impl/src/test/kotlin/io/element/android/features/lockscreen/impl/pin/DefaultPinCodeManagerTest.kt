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

import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryPinCodeStore
import io.element.android.libraries.cryptography.impl.AESEncryptionDecryptionService
import io.element.android.libraries.cryptography.test.SimpleSecretKeyProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPinCodeManagerTest {

    private val pinCodeStore = InMemoryPinCodeStore()
    private val secretKeyProvider = SimpleSecretKeyProvider()
    private val encryptionDecryptionService = AESEncryptionDecryptionService()
    private val pinCodeManager = DefaultPinCodeManager(secretKeyProvider, encryptionDecryptionService, pinCodeStore)

    @Test
    fun given_a_pin_code_when_create_and_delete_assert_no_pin_code_left() = runTest {
        pinCodeManager.createPinCode("1234")
        assertThat(pinCodeManager.isPinCodeAvailable()).isTrue()
        pinCodeManager.deletePinCode()
        assertThat(pinCodeManager.isPinCodeAvailable()).isFalse()
    }

    @Test
    fun given_a_pin_code_when_create_and_verify_with_the_same_pin_succeed() = runTest {
        val pinCode = "1234"
        pinCodeManager.createPinCode(pinCode)
        assertThat(pinCodeManager.verifyPinCode(pinCode)).isTrue()
    }

    @Test
    fun given_a_pin_code_when_create_and_verify_with_a_different_pin_fails() = runTest {
        pinCodeManager.createPinCode("1234")
        assertThat(pinCodeManager.verifyPinCode("1235")).isFalse()
    }
}
