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

package io.element.android.libraries.cryptography.api

import javax.crypto.Cipher

/**
 * Factory to create [Cipher] instances for encryption and decryption.
 * The implementation should use a secure way to store the keys.
 */
interface CipherFactory {
    /**
     * Create a [Cipher] instance for encryption.
     * @param alias the alias of the key used for encryption.
     * @return the [Cipher] instance.
     */
    fun createEncryptionCipher(alias: String): Cipher

    /**
     * Create a [Cipher] instance for decryption.
     * @param alias the alias of the key used for encryption.
     * @param initializationVector the initialization vector used for encryption.
     * @return the [Cipher] instance.
     */
    fun createDecryptionCipher(alias: String, initializationVector: ByteArray): Cipher
}
