/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.EncryptionResult
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Default implementation of [EncryptionDecryptionService] using AES encryption.
 */
@ContributesBinding(AppScope::class)
class AESEncryptionDecryptionService : EncryptionDecryptionService {
    override fun createEncryptionCipher(key: SecretKey): Cipher {
        return Cipher.getInstance(AESEncryptionSpecs.CIPHER_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    override fun createDecryptionCipher(key: SecretKey, initializationVector: ByteArray): Cipher {
        val spec = GCMParameterSpec(128, initializationVector)
        return Cipher.getInstance(AESEncryptionSpecs.CIPHER_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, spec)
        }
    }

    override fun encrypt(key: SecretKey, input: ByteArray): EncryptionResult {
        val cipher = createEncryptionCipher(key)
        val encryptedData = cipher.doFinal(input)
        return EncryptionResult(encryptedData, cipher.iv)
    }

    override fun decrypt(key: SecretKey, encryptionResult: EncryptionResult): ByteArray {
        val cipher = createDecryptionCipher(key, encryptionResult.initializationVector)
        return cipher.doFinal(encryptionResult.encryptedByteArray)
    }
}
