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

package io.element.android.libraries.pushstore.test.userpushstore.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore

class InMemoryPushClientSecretStore : PushClientSecretStore {
    private val secrets = mutableMapOf<SessionId, String>()

    fun getSecrets(): Map<SessionId, String> = secrets

    override suspend fun storeSecret(userId: SessionId, clientSecret: String) {
        secrets[userId] = clientSecret
    }

    override suspend fun getSecret(userId: SessionId): String? {
        return secrets[userId]
    }

    override suspend fun resetSecret(userId: SessionId) {
        secrets.remove(userId)
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        return secrets.keys.firstOrNull { secrets[it] == clientSecret }
    }
}
