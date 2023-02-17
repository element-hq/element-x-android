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

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKeys
import java.security.Key
import java.security.KeyStore

class KeyStorePassphraseProvider(
    private val alias: String,
    private val keyStore: KeyStore,
) : PassphraseProvider {

    override fun getPassphrase(): ByteArray = getKey().encoded

    private fun getKey(): Key {
        keyStore.load(null)
        if (!keyStore.containsAlias(alias)) {
            generateKey()
        }
        return keyStore.getKey(alias, null)
    }

    private fun generateKey() {
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        MasterKeys.getOrCreate(spec)
    }
}
