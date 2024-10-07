/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.cryptography.test

import io.element.android.libraries.cryptography.api.AESEncryptionSpecs
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SimpleSecretKeyRepository : SecretKeyRepository {
    private var secretKeyForAlias = HashMap<String, SecretKey>()

    override fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey {
        return secretKeyForAlias.getOrPut(alias) {
            generateKey()
        }
    }

    override fun deleteKey(alias: String) {
        secretKeyForAlias.remove(alias)
    }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AESEncryptionSpecs.ALGORITHM)
        keyGenerator.init(AESEncryptionSpecs.KEY_SIZE)
        return keyGenerator.generateKey()
    }
}
