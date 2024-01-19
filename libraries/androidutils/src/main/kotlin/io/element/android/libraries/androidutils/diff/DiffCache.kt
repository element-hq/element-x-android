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
 * A cache that can be used to store some data that can be invalidated when a diff is applied.
 * The cache is invalidated by the [DiffCacheInvalidator].
 */
interface DiffCache<E> {
    fun get(index: Int): E?
    fun indices(): IntRange
    fun isEmpty(): Boolean
}

/**
 * A [DiffCache] that can be mutated by adding, removing or updating elements.
 */
interface MutableDiffCache<E> : DiffCache<E> {
    fun removeAt(index: Int): E?
    fun add(index: Int, element: E?)
    operator fun set(index: Int, element: E?)
}

/**
 * A [MutableDiffCache] backed by a [MutableList].
 *
 */
class MutableListDiffCache<E>(private val mutableList: MutableList<E?> = ArrayList()) : MutableDiffCache<E> {
    override fun removeAt(index: Int): E? {
        return mutableList.removeAt(index)
    }

    override fun get(index: Int): E? {
        return mutableList.getOrNull(index)
    }

    override fun indices(): IntRange {
        return mutableList.indices
    }

    override fun isEmpty(): Boolean {
        return mutableList.isEmpty()
    }

    override operator fun set(index: Int, element: E?) {
        mutableList[index] = element
    }

    override fun add(index: Int, element: E?) {
        mutableList.add(index, element)
    }
}
