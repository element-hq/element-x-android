/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl

import io.element.android.libraries.cachestore.CacheData
import java.util.Date

internal fun aCacheData(
    key: String = "aKey",
    value: String = "aValue",
    updatedAt: Date = Date(),
) = CacheData(
    key = key,
    value_ = value,
    updatedAt = updatedAt.time,
)
