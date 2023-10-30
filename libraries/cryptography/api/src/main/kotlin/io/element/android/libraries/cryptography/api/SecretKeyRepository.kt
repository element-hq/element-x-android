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

package io.element.android.libraries.cryptography.api

import javax.crypto.SecretKey

/**
 * Simple interface to get, create and delete a secret key for a given alias.
 * Implementation should be able to store the generated key securely.
 */
interface SecretKeyRepository {
    /**
     * Get or create a secret key for a given alias.
     * @param alias the alias to use
     * @param requiresUserAuthentication true if the key should be protected by user authentication
     */
    fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey

    /**
     * Delete the secret key for a given alias.
     * @param alias the alias to use
     */
    fun deleteKey(alias: String)
}
