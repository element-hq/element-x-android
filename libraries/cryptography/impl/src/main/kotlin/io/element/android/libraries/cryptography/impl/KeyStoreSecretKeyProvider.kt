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

package io.element.android.libraries.cryptography.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.SecretKeyProvider
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val ANDROID_KEYSTORE = "AndroidKeyStore"

/**
 * Default implementation of [SecretKeyProvider] that uses the Android Keystore to store the keys.
 * The generated key uses AES algorithm, with a key size of 128 bits, and the GCM block mode.
 */
class KeyStoreSecretKeyProvider : SecretKeyProvider {

    override fun getOrCreateKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        val secretKeyEntry = (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)
            ?.secretKey
        return if (secretKeyEntry == null) {
            val generator = KeyGenerator.getInstance(AESEncryptionSpecs.ALGORITHM, ANDROID_KEYSTORE)
            val keyGenSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(AESEncryptionSpecs.BLOCK_MODE)
                .setEncryptionPaddings(AESEncryptionSpecs.PADDINGS)
                .setKeySize(AESEncryptionSpecs.KEY_SIZE)
                .build()
            generator.init(keyGenSpec)
            generator.generateKey()
        } else {
            secretKeyEntry
        }
    }
}
