/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
