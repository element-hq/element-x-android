/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.api

import javax.crypto.Cipher
import javax.crypto.SecretKey

/**
 * Simple service to provide encryption and decryption operations.
 */
interface EncryptionDecryptionService {
    /**
     * Creates a cipher ready to encrypt, for the callers that need the cipher itself, such as to bind it to a biometric prompt.
     *
     * @param key the key to encrypt with.
     */
    fun createEncryptionCipher(key: SecretKey): Cipher

    /**
     * Creates a cipher ready to decrypt, for the callers that need the cipher itself.
     *
     * @param key the key to decrypt with.
     * @param initializationVector the vector produced when the data was encrypted.
     */
    fun createDecryptionCipher(key: SecretKey, initializationVector: ByteArray): Cipher

    /**
     * Encrypts data in one call.
     *
     * @param key the key to encrypt with.
     * @param input the plaintext bytes.
     * @return the ciphertext along with the initialization vector needed to decrypt it.
     */
    fun encrypt(key: SecretKey, input: ByteArray): EncryptionResult

    /**
     * Decrypts data in one call.
     *
     * @param key the key the data was encrypted with.
     * @param encryptionResult the ciphertext and its initialization vector, as returned by [encrypt].
     */
    fun decrypt(key: SecretKey, encryptionResult: EncryptionResult): ByteArray
}
