/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

import io.element.android.libraries.matrix.api.UrlContentFetcher

/**
 * Retrieves the Element specific `.well-known` configuration of a server, which is how a deployment advertises its own settings.
 */
interface WellknownRetriever {
    /**
     * Returns the configuration of a host, served from the local cache when possible.
     * A cached value that has gone stale is returned as outdated while a refresh runs in the background, so this rarely waits on the network.
     * An enterprise build with a hardcoded configuration always short-circuits and returns that instead.
     *
     * @param host the server to look up; a full URL is accepted and reduced to its host.
     * @param source which endpoint to read the configuration from, which also selects the cache it is stored in.
     */
    suspend fun getElementWellKnown(
        host: String,
        source: EnterpriseRemoteConfigSource
    ): WellknownRetrieverResult<ElementWellKnown>

    /**
     * Creates a retriever, which needs a fetcher because the configuration is read over HTTP.
     */
    interface Factory {
        /**
         * @param urlContentFetcher used to perform the request; a client that is not authenticated yet also works.
         */
        fun create(urlContentFetcher: UrlContentFetcher): WellknownRetriever
    }
}
