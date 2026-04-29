/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.api.CacheStore

class InMemoryCacheStore(
    initialData: Map<String, CacheData> = emptyMap(),
) : CacheStore {
    val dataMap = initialData.toMutableMap()

    override suspend fun storeData(key: String, data: CacheData) {
        dataMap[key] = data
    }

    override suspend fun getData(key: String): CacheData? {
        return dataMap[key]
    }

    override suspend fun deleteData(key: String) {
        dataMap.remove(key)
    }
}
