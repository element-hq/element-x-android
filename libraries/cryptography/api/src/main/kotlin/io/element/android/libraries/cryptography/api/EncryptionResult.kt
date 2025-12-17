/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalEncodingApi::class)

package io.element.android.libraries.cryptography.api

import java.nio.ByteBuffer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
        return Base64.encode(cipherTextWithIv)
    }

    companion object {
        /**
         * @param base64 the base64 representation of the encrypted data.
         * @return the [EncryptionResult] from the base64 representation.
         */
        fun fromBase64(base64: String): EncryptionResult {
            val cipherTextWithIv = Base64.decode(base64)
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
