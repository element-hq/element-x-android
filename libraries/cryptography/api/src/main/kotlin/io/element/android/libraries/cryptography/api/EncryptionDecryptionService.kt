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
