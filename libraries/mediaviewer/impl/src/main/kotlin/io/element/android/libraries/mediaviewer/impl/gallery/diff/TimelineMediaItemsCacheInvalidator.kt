/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.diff

import io.element.android.libraries.androidutils.diff.DefaultDiffCacheInvalidator
import io.element.android.libraries.androidutils.diff.DiffCacheInvalidator
import io.element.android.libraries.androidutils.diff.MutableDiffCache
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

/**
 * [DiffCacheInvalidator] implementation for [MediaItem].
 * It uses [DefaultDiffCacheInvalidator] and invalidate the cache around the updated item so that those items are computed again.
 * This is needed because a timeline item is computed based on the previous and next items.
 */
internal class TimelineMediaItemsCacheInvalidator : DiffCacheInvalidator<MediaItem> {
    private val delegate = DefaultDiffCacheInvalidator<MediaItem>()

    override fun onChanged(position: Int, count: Int, cache: MutableDiffCache<MediaItem>) {
        delegate.onChanged(position, count, cache)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int, cache: MutableDiffCache<MediaItem>) {
        delegate.onMoved(fromPosition, toPosition, cache)
    }

    override fun onInserted(position: Int, count: Int, cache: MutableDiffCache<MediaItem>) {
        cache.invalidateAround(position)
        delegate.onInserted(position, count, cache)
    }

    override fun onRemoved(position: Int, count: Int, cache: MutableDiffCache<MediaItem>) {
        cache.invalidateAround(position)
        delegate.onRemoved(position, count, cache)
    }
}

/**
 * Invalidate the cache around the given position.
 * It invalidates the previous and next items.
 */
private fun MutableDiffCache<*>.invalidateAround(position: Int) {
    if (position > 0) {
        set(position - 1, null)
    }
    if (position < indices().last) {
        set(position + 1, null)
    }
}
