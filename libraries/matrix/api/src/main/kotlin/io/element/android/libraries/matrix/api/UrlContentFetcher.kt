/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

/**
 * An interface to fetch content from a URL using a simple GET request.
 */
fun interface UrlContentFetcher {
    /**
     * Execute generic GET requests through the SDKs internal HTTP client.
     */
    suspend fun getUrl(url: String): Result<ByteArray>
}
