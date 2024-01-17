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
