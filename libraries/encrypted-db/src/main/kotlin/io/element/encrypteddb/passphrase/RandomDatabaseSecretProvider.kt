/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.passphrase

import android.content.Context
import io.element.android.libraries.androidutils.crypto.ClientSecret
import io.element.encrypteddb.crypto.EncryptedFile
import java.io.File
import java.security.SecureRandom

/**
 * Provides a secure secret for SQLCipher by generating a random secret and storing it into an [EncryptedFile].
 * @param context Android [Context], used by [EncryptedFile] for cryptographic operations.
 * @param file Destination file where the key will be stored.
 * @param secretSizeBytes Length of the generated secret.
 */
class RandomDatabaseSecretProvider(
    private val context: Context,
    private val file: File,
    private val secretSizeBytes: Int = 32,
) : DatabaseSecretProvider {
    override fun getSecret(): ClientSecret {
        val encryptedFile = EncryptedFile(context, file)
        val bytes = if (!file.exists()) {
            val secret = generateSecret()
            encryptedFile.openFileOutput().use { it.write(secret) }
            secret
        } else {
            encryptedFile.openFileInput().use { it.readBytes() }
        }
        return ClientSecret.fromRawBytes(bytes)
    }

    override fun reset(): Boolean {
        return file.delete()
    }

    private fun generateSecret(): ByteArray {
        // Generate a random secret of the specified size using a secure random generator.
        return ByteArray(size = secretSizeBytes)
            .also { SecureRandom().nextBytes(it) }
    }
}
