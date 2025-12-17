/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.crypto

import android.content.Context
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * This class is used to write and read encrypted data to/from a file.
 *
 * It's a simplified version of the same class in [androidx.security.crypto](https://developer.android.com/reference/androidx/security/crypto/package-summary).
 *
 * It uses hardcoded constants that are used in that library, for backwards compatibility reasons.
 */
internal class EncryptedFile(
    private val context: Context,
    private val file: File
) {
    companion object {
        /**
         * The file content is encrypted using StreamingAead with AES-GCM, with the file name as associated data.
         */
        private const val KEYSET_ENCRYPTION_SCHEME = "AES256_GCM_HKDF_4KB"

        /**
         * The keyset is stored in a shared preference file with this name.
         */
        private const val KEYSET_PREF_FILE_NAME = "__androidx_security_crypto_encrypted_file_pref__"

        /**
         * The keyset is stored in a shared preference with this key, inside the specified file.
         */
        private const val KEYSET_ALIAS = "__androidx_security_crypto_encrypted_file_keyset__"

        /**
         * The URI referencing the master key in the Android Keystore used to encrypt/decrypt the keyset.
         */
        private const val MASTER_KEY_URI = "android-keystore://_androidx_security_master_key_"
    }

    private val androidKeysetManager by lazy {
        val keysetManagerBuilder = AndroidKeysetManager.Builder()
            .withKeyTemplate(KeyTemplates.get(KEYSET_ENCRYPTION_SCHEME))
            .withSharedPref(context, KEYSET_ALIAS, KEYSET_PREF_FILE_NAME)
            .withMasterKeyUri(MASTER_KEY_URI)

        keysetManagerBuilder.build()
    }

    private val streamingAead: StreamingAead by lazy {
        val streamingAeadKeysetHandle = androidKeysetManager.keysetHandle
        streamingAeadKeysetHandle.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
    }

    init {
        StreamingAeadConfig.register()
    }

    fun openFileOutput(): FileOutputStream {
        val fos = FileOutputStream(file)
        val stream = streamingAead.newEncryptingStream(fos, file.name.toByteArray())
        return EncryptedFileOutputStream(fos.fd, stream)
    }

    fun openFileInput(): FileInputStream {
        val fis = FileInputStream(file)
        val stream = streamingAead.newDecryptingStream(fis, file.name.toByteArray())
        return EncryptedFileInputStream(fis.fd, stream)
    }
}
