/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.collections

/**
 * Keep all the items in the list except for the last [count] items that match [predicate].
 */
fun <T> List<T>.takeExceptLast(count: Int, predicate: (T) -> Boolean): List<T> {
    val matchingIndices = indices.filter { predicate(this[it]) }
    val indicesToRemove = matchingIndices.dropLast(count).toSet()
    return filterIndexed { index, _ -> index !in indicesToRemove }
}
