/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.impl

import android.annotation.SuppressLint
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Default implementation of [SecretKeyRepository] that uses the Android Keystore to store the keys.
 * The generated key uses AES algorithm, with a key size of 128 bits, and the GCM block mode.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class KeyStoreSecretKeyRepository(
    private val keyStore: KeyStore,
) : SecretKeyRepository {
    private val hasKeyMap = ConcurrentHashMap<String, MutableStateFlow<Boolean>>()

    @Suppress("RunCatchingNotAllowed")
    override fun hasKey(alias: String): Flow<Boolean> {
        return hasKeyMap.getOrPut(alias) {
            MutableStateFlow(runCatching { keyStore.containsAlias(alias) }.getOrDefault(false))
        }.asStateFlow()
    }

    // False positive lint issue
    @SuppressLint("WrongConstant")
    override suspend fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey {
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
            generator.generateKey().also {
                hasKeyMap.getOrPut(alias) {
                    MutableStateFlow(true)
                }.emit(true)
            }
        } else {
            secretKeyEntry
        }
    }

    override suspend fun deleteKey(alias: String) {
        try {
            keyStore.deleteEntry(alias)
            hasKeyMap.getOrPut(alias) {
                MutableStateFlow(false)
            }.emit(false)
        } catch (e: KeyStoreException) {
            Timber.e(e)
        }
    }
}
