/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.cryptography.api

import javax.crypto.Cipher
import javax.crypto.SecretKey

/**
 * Simple service to provide encryption and decryption operations.
 */
interface EncryptionDecryptionService {
    fun createEncryptionCipher(key: SecretKey): Cipher
    fun createDecryptionCipher(key: SecretKey, initializationVector: ByteArray): Cipher
    fun encrypt(key: SecretKey, input: ByteArray): EncryptionResult
    fun decrypt(key: SecretKey, encryptionResult: EncryptionResult): ByteArray
}
