/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl.remoteconfig

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfig
import io.element.android.features.enterprise.api.remoteconfig.RemoteEnterpriseConfigProvider
import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

@ContributesBinding(AppScope::class)
class DefaultRemoteEnterpriseConfigProvider : RemoteEnterpriseConfigProvider {
    override suspend fun get(sessionId: SessionId): WellknownRetrieverResult<RemoteEnterpriseConfig> {
        return WellknownRetrieverResult.NotFound
    }

    @ContributesBinding(AppScope::class)
    class Factory : RemoteEnterpriseConfigProvider.Factory {
        override fun create(urlContentFetcher: UrlContentFetcher): RemoteEnterpriseConfigProvider {
            return DefaultRemoteEnterpriseConfigProvider()
        }
    }
}
