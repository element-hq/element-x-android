/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
