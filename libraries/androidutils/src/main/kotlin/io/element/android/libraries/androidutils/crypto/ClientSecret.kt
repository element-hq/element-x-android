/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.crypto

/**
 * Represents a client secret used to encrypt/decrypt data from databases, which can be either a passphrase or a raw key.
 */
sealed interface ClientSecret {
    /**
     * A passphrase that can be used to derive a key for encryption/decryption.
     */
    data class Passphrase(val value: String) : ClientSecret {
        override fun formattedAsString(): String = value
    }

    /**
     * A raw key that can be directly used for encryption/decryption. The key is represented as a byte array, and is formatted as a string in the form of
     * `x'...'` where the bytes are encoded as hex characters.
     */
    data class RawKey(val bytes: ByteArray) : ClientSecret {
        override fun formattedAsString() = "x'${bytes.toHexString()}'"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return bytes.contentEquals((other as RawKey).bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }

        override fun toString(): String {
            return formattedAsString()
        }
    }

    /**
     * Format the client secret as a string that can be parsed back with [fromString].
     * For a passphrase, this is just the passphrase value. For a raw key, this is the hex-encoded representation of the key formatted as `x'...'`.
     */
    fun formattedAsString(): String

    companion object {
        /**
         * Parse a string representation of a client secret, which can be either a passphrase or a raw key formatted as `x'...'`.
         */
        fun fromString(secret: String): ClientSecret {
            val regex = Regex("^x'([0-9a-fA-F]+)'$")
            val rawKeyMatch = regex.matchEntire(secret)
            return if (rawKeyMatch != null) {
                RawKey(rawKeyMatch.groupValues[1].hexToByteArray())
            } else {
                Passphrase(secret)
            }
        }

        /**
         * Create a [ClientSecret] from raw bytes, which will be treated as a raw key.
         */
        fun fromRawBytes(bytes: ByteArray): ClientSecret = when (bytes.size) {
            32 -> RawKey(bytes)
            else -> Passphrase(bytes.toHexString())
        }
    }
}
