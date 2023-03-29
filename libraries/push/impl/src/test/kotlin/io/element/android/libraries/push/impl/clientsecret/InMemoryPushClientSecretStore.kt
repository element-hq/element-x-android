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

package io.element.android.libraries.push.impl.clientsecret

class InMemoryPushClientSecretStore : PushClientSecretStore {
    private val secrets = mutableMapOf<String, String>()

    fun getSecrets(): Map<String, String> = secrets

    override suspend fun storeSecret(userId: String, clientSecret: String) {
        secrets[userId] = clientSecret
    }

    override suspend fun getSecret(userId: String): String? {
        return secrets[userId]
    }

    override suspend fun resetSecret(userId: String) {
        secrets.remove(userId)
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): String? {
        return secrets.keys.firstOrNull { secrets[it] == clientSecret }
    }
}
