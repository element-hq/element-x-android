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

package io.element.android.libraries.cryptography.impl

import android.security.keystore.KeyProperties
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator

class AESEncryptionDecryptionServiceTest {

    private val encryptionDecryptionService = AESEncryptionDecryptionService()

    @Test
    fun `given a valid key then encrypt decrypt work`() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(128)
        val key = keyGenerator.generateKey()
        val input = "Hello World".toByteArray()
        val encryptionResult = encryptionDecryptionService.encrypt(key, input)
        val decrypted = encryptionDecryptionService.decrypt(key, encryptionResult)
        assertThat(decrypted).isEqualTo(input)
    }

    @Test
    fun `given a wrong key then decrypt fail`() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(128)
        val encryptionKey = keyGenerator.generateKey()
        val input = "Hello World".toByteArray()
        val encryptionResult = encryptionDecryptionService.encrypt(encryptionKey, input)
        val decryptionKey = keyGenerator.generateKey()
        assertThrows(GeneralSecurityException::class.java) {
            encryptionDecryptionService.decrypt(decryptionKey, encryptionResult)
        }
    }
}
