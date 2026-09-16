/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.test

import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SimpleSecretKeyRepository : SecretKeyRepository {
    private var secretKeyForAlias = HashMap<String, SecretKey>()

    private val hasKeyMap = ConcurrentHashMap<String, MutableStateFlow<Boolean>>()

    override fun hasKey(alias: String): Flow<Boolean> {
        return hasKeyMap.getOrPut(alias) {
            MutableStateFlow(false)
        }.asStateFlow()
    }

    override suspend fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey {
        return secretKeyForAlias.getOrPut(alias) {
            generateKey().also {
                hasKeyMap.getOrPut(alias) {
                    MutableStateFlow(true)
                }.emit(true)
            }
        }
    }

    override suspend fun deleteKey(alias: String) {
        secretKeyForAlias.remove(alias)
        hasKeyMap.getOrPut(alias) {
            MutableStateFlow(false)
        }.emit(false)
    }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AESEncryptionSpecs.ALGORITHM)
        keyGenerator.init(AESEncryptionSpecs.KEY_SIZE)
        return keyGenerator.generateKey()
    }
}
