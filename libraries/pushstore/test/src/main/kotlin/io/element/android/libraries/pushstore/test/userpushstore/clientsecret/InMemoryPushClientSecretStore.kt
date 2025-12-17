/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
