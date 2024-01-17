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

package io.element.android.features.messages.impl.timeline.diff

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.diff.DefaultDiffCacheInvalidator
import io.element.android.libraries.androidutils.diff.DiffCacheInvalidator
import io.element.android.libraries.androidutils.diff.MutableDiffCache

/**
 * [DiffCacheInvalidator] implementation for [TimelineItem].
 * It uses [DefaultDiffCacheInvalidator] and invalidate the cache around the updated item so that those items are computed again.
 * This is needed because a timeline item is computed based on the previous and next items.
 */
internal class TimelineItemsCacheInvalidator : DiffCacheInvalidator<TimelineItem> {
    private val delegate = DefaultDiffCacheInvalidator<TimelineItem>()

    override fun onChanged(position: Int, count: Int, cache: MutableDiffCache<TimelineItem>) {
        delegate.onChanged(position, count, cache)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int, cache: MutableDiffCache<TimelineItem>) {
        delegate.onMoved(fromPosition, toPosition, cache)
    }

    override fun onInserted(position: Int, count: Int, cache: MutableDiffCache<TimelineItem>) {
        cache.invalidateAround(position)
        delegate.onInserted(position, count, cache)
    }

    override fun onRemoved(position: Int, count: Int, cache: MutableDiffCache<TimelineItem>) {
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
