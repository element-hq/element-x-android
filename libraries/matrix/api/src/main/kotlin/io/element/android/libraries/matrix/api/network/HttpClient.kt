/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.network

/**
 * A simple HTTP client interface to be used by the Matrix SDK.
 */
interface HttpClient {
    /**
     * The URL of the actual Matrix homeserver this client targets.
     */
    val homeserverUrl: String

    /**
     * The domain of the public server for the homeserver, if known.
     */
    val serverName: String?

    /**
     * Execute generic GET requests through the SDKs internal HTTP client.
     *
     * @param url the absolute URL to fetch. Use [homeserverUrl] or [serverName] to build it.
     * @return the response body, or a failure with an [HttpClientException].
     */
    suspend fun getUrl(url: String): Result<ByteArray>

    interface Factory {
        /**
         * Create a new instance of [HttpClient].
         */
        fun create(homeserverUrl: String, serverName: String?): HttpClient
    }
}
