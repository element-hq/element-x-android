/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.cache

/**
 * A FIFO circular buffer of T.
 * This class is not thread safe.
 */
class CircularCache<T : Any>(cacheSize: Int, factory: (Int) -> Array<T?>) {
    companion object {
        inline fun <reified T : Any> create(cacheSize: Int) = CircularCache(cacheSize) { Array<T?>(cacheSize) { null } }
    }

    private val cache = factory(cacheSize)
    private var writeIndex = 0

    fun contains(value: T): Boolean = cache.contains(value)

    fun put(value: T) {
        if (writeIndex == cache.size) {
            writeIndex = 0
        }
        cache[writeIndex] = value
        writeIndex++
    }
}
