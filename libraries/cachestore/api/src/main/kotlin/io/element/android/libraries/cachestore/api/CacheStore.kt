/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cachestore.api

/**
 * A generic on-disk key/value cache, where each entry records when it was written so callers can decide for themselves when it is stale.
 */
interface CacheStore {
    /**
     * Stores an entry, replacing any previous one under the same key.
     *
     * @param key the entry to write.
     * @param data the value along with the time it was obtained.
     */
    suspend fun storeData(key: String, data: CacheData)

    /**
     * Reads an entry without applying any expiry of its own; judging staleness is up to the caller.
     *
     * @param key the entry to read.
     * @return the value and its timestamp, or `null` when nothing is stored under that key.
     */
    suspend fun getData(key: String): CacheData?

    /**
     * Removes one entry.
     *
     * @param key the entry to delete.
     */
    suspend fun deleteData(key: String)

    /** Empties the whole cache. */
    suspend fun deleteAll()
}
