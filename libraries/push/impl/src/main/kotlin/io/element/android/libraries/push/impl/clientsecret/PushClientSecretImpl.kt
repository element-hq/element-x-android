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

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class PushClientSecretImpl @Inject constructor(
    private val pushClientSecretFactory: PushClientSecretFactory,
    private val pushClientSecretStore: PushClientSecretStore,
) : PushClientSecret {
    override suspend fun getSecretForUser(userId: String): String {
        val existingSecret = pushClientSecretStore.getSecret(userId)
        if (existingSecret != null) {
            return existingSecret
        }
        val newSecret = pushClientSecretFactory.create()
        pushClientSecretStore.storeSecret(userId, newSecret)
        return newSecret
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): String? {
        return pushClientSecretStore.getUserIdFromSecret(clientSecret)
    }

    override suspend fun resetSecretForUser(userId: String) {
        pushClientSecretStore.resetSecret(userId)
    }
}
