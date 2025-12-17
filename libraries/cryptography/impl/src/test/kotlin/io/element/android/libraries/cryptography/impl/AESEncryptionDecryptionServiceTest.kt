/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
