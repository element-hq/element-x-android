/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushClientSecret @Inject constructor(
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
