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

package io.element.encrypteddb.passphrase

import android.content.Context
import androidx.security.crypto.EncryptedFile
import java.io.File
import java.security.SecureRandom

/**
 * Provides a secure passphrase for SQLCipher by generating a random secret and storing it into an [EncryptedFile].
 * @param secretSize Length of the generated secret.
 * @param file Destination file where the key will be stored.
 * @param context Android [Context], used by [EncryptedFile] for cryptographic operations.
 * @param alias Alias of the key used to encrypt & decrypt the [EncryptedFile]'s contents.
 */
class RandomSecretPassphraseProvider(
    private val context: Context,
    private val file: File,
    private val alias: String,
    private val secretSize: Int = 256,
) : PassphraseProvider {

    override fun getPassphrase(): ByteArray {
        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            alias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
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
