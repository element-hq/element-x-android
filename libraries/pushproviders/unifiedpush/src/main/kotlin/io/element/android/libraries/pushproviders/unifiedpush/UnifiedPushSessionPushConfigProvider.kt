/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import java.net.URL

interface UnifiedPushSessionPushConfigProvider {
    suspend fun provide(sessionId: SessionId): Config?
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushPushConfigProvider(
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
) : UnifiedPushSessionPushConfigProvider {
    override suspend fun provide(sessionId: SessionId): Config? {
        val clientSecret = pushClientSecret.getSecretForUser(sessionId)
        val url = unifiedPushStore.getPushGateway(clientSecret) ?: return null
        val pushKey = unifiedPushStore.getEndpoint(clientSecret) ?: return null
        val distributor = unifiedPushDistributorProvider.getDistributors()
            .find { it.value == unifiedPushStore.getDistributorValue(sessionId) }
        return Config(
            url = url,
            pushKey = pushKey,
            isRateLimited = isUrlRateLimited(url),
            distributor = requireNotNull(distributor),
        )
    }

    private fun isUrlRateLimited(url: String): Boolean {
        val actualUrl = tryOrNull { URL(url) } ?: return false
        return actualUrl.host in listOf(
            "ntfy.sh",
        )
    }
}
