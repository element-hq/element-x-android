/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

import kotlin.math.min

/**
 * Returns a collection containing at most [maxSize] elements from the original collection.
 * If the original collection has fewer than [maxSize] elements, it returns all of them.
 * If [maxSize] is less than or equal to 0, it returns an empty collection.
 */
fun <T> Collection<T>.takeAtMost(maxSize: Int): Collection<T> {
    val maxLength = min(maxSize, this.size)
    if (maxLength <= 0) return emptyList()

    return take(maxLength)
}
