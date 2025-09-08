/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore

@ContributesBinding(AppScope::class)
@Inject
class DefaultPushClientSecret(
    private val pushClientSecretFactory: PushClientSecretFactory,
    private val pushClientSecretStore: PushClientSecretStore,
) : PushClientSecret {
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
}
