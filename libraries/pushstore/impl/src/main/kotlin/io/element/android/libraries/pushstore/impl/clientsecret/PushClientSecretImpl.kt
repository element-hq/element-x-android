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

package io.element.android.libraries.pushstore.impl.clientsecret

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, boundType = PushClientSecret::class)
class PushClientSecretImpl @Inject constructor(
    private val pushClientSecretFactory: PushClientSecretFactory,
    private val pushClientSecretStore: PushClientSecretStore,
    private val sessionObserver: SessionObserver,
) : PushClientSecret, SessionListener {
    init {
        observeSessions()
    }

    override suspend fun getSecretForUser(userId: SessionId): String {
        val existingSecret = pushClientSecretStore.getSecret(userId)
        if (existingSecret != null) {
            return existingSecret
        }
        val newSecret = pushClientSecretFactory.create()
        pushClientSecretStore.storeSecret(userId, newSecret)
        return newSecret
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        return pushClientSecretStore.getUserIdFromSecret(clientSecret)
    }

    private fun observeSessions() {
        sessionObserver.addListener(this)
    }

    override suspend fun onSessionCreated(userId: String) {
        // Nothing to do
    }

    override suspend fun onSessionDeleted(userId: String) {
        // Delete the secret
        pushClientSecretStore.resetSecret(SessionId(userId))
    }
}
