/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.impl

import android.annotation.SuppressLint
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import io.element.android.libraries.di.AppScope
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

/**
 * Default implementation of [SecretKeyRepository] that uses the Android Keystore to store the keys.
 * The generated key uses AES algorithm, with a key size of 128 bits, and the GCM block mode.
 */
@ContributesBinding(AppScope::class)
class KeyStoreSecretKeyRepository @Inject constructor(
    private val keyStore: KeyStore,
) : SecretKeyRepository {
    // False positive lint issue
    @SuppressLint("WrongConstant")
    override fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey {
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
                .setUserAuthenticationRequired(requiresUserAuthentication)
                .build()
            generator.init(keyGenSpec)
            generator.generateKey()
        } else {
            secretKeyEntry
        }
    }

    override fun deleteKey(alias: String) {
        try {
            keyStore.deleteEntry(alias)
        } catch (e: KeyStoreException) {
            Timber.e(e)
        }
    }
}
