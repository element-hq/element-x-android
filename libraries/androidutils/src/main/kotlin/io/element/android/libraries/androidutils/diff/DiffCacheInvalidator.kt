/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.diff

/**
 * [DiffCacheInvalidator] is used to invalidate the cache when the list is updated.
 * It is used by [DiffCacheUpdater].
 * Check the default implementation [DefaultDiffCacheInvalidator].
 */
interface DiffCacheInvalidator<T> {
    fun onChanged(position: Int, count: Int, cache: MutableDiffCache<T>)

    fun onMoved(fromPosition: Int, toPosition: Int, cache: MutableDiffCache<T>)

    fun onInserted(position: Int, count: Int, cache: MutableDiffCache<T>)

    fun onRemoved(position: Int, count: Int, cache: MutableDiffCache<T>)
}

/**
 * Default implementation of [DiffCacheInvalidator].
 * It invalidates the cache by setting values to null.
 */
class DefaultDiffCacheInvalidator<T> : DiffCacheInvalidator<T> {
    override fun onChanged(position: Int, count: Int, cache: MutableDiffCache<T>) {
        for (i in position until position + count) {
            // Invalidate cache
            cache[i] = null
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int, cache: MutableDiffCache<T>) {
        val model = cache.removeAt(fromPosition)
        cache.add(toPosition, model)
    }

    override fun onInserted(position: Int, count: Int, cache: MutableDiffCache<T>) {
        repeat(count) {
            cache.add(position, null)
        }
    }

    override fun onRemoved(position: Int, count: Int, cache: MutableDiffCache<T>) {
        repeat(count) {
            cache.removeAt(position)
        }
    }
}
