/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.cachestore.api.CacheData

fun aCacheData(
    value: String = "aValue",
    updatedAt: Long = 0,
) = CacheData(
    value = value,
    updatedAt = updatedAt,
)
