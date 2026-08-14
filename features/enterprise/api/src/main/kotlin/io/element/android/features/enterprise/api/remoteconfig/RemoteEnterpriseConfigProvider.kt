/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

/**
 * Wrapper to fetch the remote enterprise configuration for a given [SessionId], either from a local cache or from a remote source.
 */
interface RemoteEnterpriseConfigProvider {
    /**
     * Returns the configuration of a session, served from the cache when possible.
     * A stale entry is reported as outdated while a refresh runs in the background, so this rarely waits on the network.
     *
     * @param sessionId the session whose configuration is requested.
     */
    suspend fun get(sessionId: SessionId): WellknownRetrieverResult<RemoteEnterpriseConfig>

    /**
     * Creates a provider, which needs a fetcher because the configuration is read over HTTP.
     */
    interface Factory {
        /**
         * @param urlContentFetcher used to perform the request; a client that is not authenticated yet also works.
         */
        fun create(urlContentFetcher: UrlContentFetcher): RemoteEnterpriseConfigProvider
    }
}
