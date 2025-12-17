/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.passphrase

import android.content.Context
import io.element.encrypteddb.crypto.EncryptedFile
import java.io.File
import java.security.SecureRandom

/**
 * Provides a secure passphrase for SQLCipher by generating a random secret and storing it into an [EncryptedFile].
 * @param context Android [Context], used by [EncryptedFile] for cryptographic operations.
 * @param file Destination file where the key will be stored.
 * @param secretSize Length of the generated secret.
 */
class RandomSecretPassphraseProvider(
    private val context: Context,
    private val file: File,
    private val secretSize: Int = 256,
) : PassphraseProvider {
    override fun getPassphrase(): ByteArray {
        val encryptedFile = EncryptedFile(context, file)
        return if (!file.exists()) {
            val secret = generateSecret()
            encryptedFile.openFileOutput().use { it.write(secret) }
            secret
        } else {
            encryptedFile.openFileInput().use { it.readBytes() }
        }
    }

    private fun generateSecret(): ByteArray {
        val buffer = ByteArray(size = secretSize)
        SecureRandom().nextBytes(buffer)
        return buffer
    }
}
