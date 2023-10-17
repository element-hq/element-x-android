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

import android.util.Base64
import java.nio.ByteBuffer

/**
 * Holds the result of an encryption operation.
 */
class EncryptionResult(
    val encryptedByteArray: ByteArray,
    val initializationVector: ByteArray
) {
    fun toBase64(): String {
        val initializationVectorSize = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(initializationVector.size).array()
        val cipherTextWithIv: ByteArray =
            ByteBuffer.allocate(Int.SIZE_BYTES + initializationVector.size + encryptedByteArray.size)
                .put(initializationVectorSize)
                .put(initializationVector)
                .put(encryptedByteArray)
                .array()
        return Base64.encodeToString(cipherTextWithIv, Base64.NO_WRAP)
    }

    companion object {
        /**
         * @param base64 the base64 representation of the encrypted data.
         * @return the [EncryptionResult] from the base64 representation.
         */
        fun fromBase64(base64: String): EncryptionResult {
            val cipherTextWithIv = Base64.decode(base64, Base64.NO_WRAP)
            val buffer = ByteBuffer.wrap(cipherTextWithIv)
            val initializationVectorSize = buffer.int
            val initializationVector = ByteArray(initializationVectorSize)
            buffer.get(initializationVector)
            val encryptedByteArray = ByteArray(buffer.remaining())
            buffer.get(encryptedByteArray)
            return EncryptionResult(encryptedByteArray, initializationVector)
        }
    }
}
