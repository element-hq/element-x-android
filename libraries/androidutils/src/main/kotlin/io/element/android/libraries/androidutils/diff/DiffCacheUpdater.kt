/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.diff

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import timber.log.Timber
import kotlin.system.measureTimeMillis

/**
 * Class in charge of updating a [MutableDiffCache] according to the cache invalidation rules provided by the [DiffCacheInvalidator].
 * @param ListItem the type of the items in the list
 * @param CachedItem the type of the items in the cache
 * @param diffCache the cache to update
 * @param detectMoves true if DiffUtil should try to detect moved items, false otherwise
 * @param cacheInvalidator the invalidator to use to update the cache
 * @param areItemsTheSame the function to use to compare items
 */
class DiffCacheUpdater<ListItem, CachedItem>(
    private val diffCache: MutableDiffCache<CachedItem>,
    private val detectMoves: Boolean = false,
    private val cacheInvalidator: DiffCacheInvalidator<CachedItem> = DefaultDiffCacheInvalidator(),
    private val areItemsTheSame: (oldItem: ListItem?, newItem: ListItem?) -> Boolean,
) {
    private val lock = Object()
    private var prevOriginalList: List<ListItem> = emptyList()

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            cacheInvalidator.onInserted(position, count, diffCache)
        }

        override fun onRemoved(position: Int, count: Int) {
            cacheInvalidator.onRemoved(position, count, diffCache)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            cacheInvalidator.onMoved(fromPosition, toPosition, diffCache)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            cacheInvalidator.onChanged(position, count, diffCache)
        }
    }

    fun updateWith(newOriginalList: List<ListItem>) = synchronized(lock) {
        val timeToDiff = measureTimeMillis {
            val diffCallback = DefaultDiffCallback(prevOriginalList, newOriginalList, areItemsTheSame)
            val diffResult = DiffUtil.calculateDiff(diffCallback, detectMoves)
            prevOriginalList = newOriginalList
            diffResult.dispatchUpdatesTo(listUpdateCallback)
        }
        Timber.v("Time to apply diff on new list of ${newOriginalList.size} items: $timeToDiff ms")
    }
}
