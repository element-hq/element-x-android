/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

/**
 * Returns a list containing first [count] elements matching the given [predicate].
 * If the list contains less elements matching the [predicate], then all of them are returned.
 *
 * @param T the type of elements contained in the list.
 * @param count the maximum number of elements to take.
 * @param predicate the predicate used to match elements.
 * @return a list containing first [count] elements matching the given [predicate].
 */
inline fun <T> Iterable<T>.filterUpTo(count: Int, predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            result.add(element)
            if (result.size == count) {
                break
            }
        }
    }
    return result
}
