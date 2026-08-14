/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

/**
 * On-disk cache of the Element `.well-known` documents already fetched, so the app has a configuration to work with while offline.
 */
interface ElementWellknownStore {
    /**
     * Reads the cached document of a domain.
     * Entries older than a day are reported as outdated rather than missing, so the caller can use them while refreshing;
     * a document that no longer parses is reported as an error.
     *
     * @param domain the server whose cached document is requested.
     */
    suspend fun get(domain: String): WellknownRetrieverResult<ElementWellKnown>

    /**
     * Stores the document of a domain and stamps it as fetched now, replacing any previous entry.
     *
     * @param domain the server the document belongs to.
     * @param wellknown the raw document, stored as-is rather than parsed.
     */
    suspend fun update(domain: String, wellknown: String): Result<Unit>

    /**
     * Removes the cached document of a domain, used when the stored value turns out to be unusable.
     *
     * @param domain the server whose entry is removed.
     */
    suspend fun delete(domain: String): Result<Unit>

    /**
     * Creates a store, one per configuration source so that their entries never collide.
     */
    fun interface Factory {
        /**
         * @param prefix namespaces the cache keys, or `null` for the default well-known endpoint.
         */
        fun create(prefix: String?): ElementWellknownStore
    }
}
