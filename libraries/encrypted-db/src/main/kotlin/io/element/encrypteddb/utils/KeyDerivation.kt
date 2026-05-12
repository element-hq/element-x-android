/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.utils

import timber.log.Timber
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Derives a key from the given [ByteArray] using PBKDF2 with HMAC-SHA256.
 * @param length Desired length of the derived key in bits. Default is `256` bits (32 bytes).
 * @param iterations Number of iterations for the key derivation function. Default is `16,000` iterations.
 * @return The derived key as a [ByteArray]. If the derivation fails, returns the original [ByteArray].
 */
fun ByteArray.doKeyDerivation(
    length: Int = 256,
    iterations: Int = 16_000,
): ByteArray {
    return try {
        val salt = ByteArray(128)

        val secureRandom = SecureRandom()
        secureRandom.nextBytes(salt)

        val keySpec = PBEKeySpec(
            this.decodeToString().toCharArray(),
            salt,
            iterations,
            length,
        )
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).encoded
    } catch (e: Exception) {
        Timber.e(e, "Failed to derive key from passphrase")
        this
    }
}
