/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cachestore.api

interface CacheStore {
    suspend fun storeData(key: String, data: CacheData)
    suspend fun getData(key: String): CacheData?
    suspend fun deleteData(key: String)
}
