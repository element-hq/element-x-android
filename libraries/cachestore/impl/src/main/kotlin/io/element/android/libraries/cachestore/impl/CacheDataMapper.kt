/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cachestore.impl

import io.element.android.libraries.cachestore.api.CacheData
import io.element.android.libraries.cachestore.CacheData as DbCacheData

internal fun CacheData.toDbModel(key: String): DbCacheData {
    return DbCacheData(
        key = key,
        value_ = value,
        updatedAt = updatedAt,
    )
}

internal fun DbCacheData.toApiModel(): CacheData {
    return CacheData(
        value = value_,
        updatedAt = updatedAt,
    )
}
