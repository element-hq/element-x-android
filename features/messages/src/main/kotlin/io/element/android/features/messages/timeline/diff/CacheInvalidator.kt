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

package io.element.android.features.messages.timeline.diff

import androidx.recyclerview.widget.ListUpdateCallback
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.util.invalidateLast
import timber.log.Timber

internal class CacheInvalidator(private val itemStatesCache: MutableList<TimelineItem?>) :
    ListUpdateCallback {

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        Timber.v("onChanged(position= $position, count= $count")
        (position until position + count).forEach {
            // Invalidate cache
            itemStatesCache[it] = null
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        Timber.v("onMoved(fromPosition= $fromPosition, toPosition= $toPosition")
        val model = itemStatesCache.removeAt(fromPosition)
        itemStatesCache.add(toPosition, model)
    }

    override fun onInserted(position: Int, count: Int) {
        Timber.v("onInserted(position= $position, count= $count")
        itemStatesCache.invalidateLast()
        repeat(count) {
            itemStatesCache.add(position, null)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        Timber.v("onRemoved(position= $position, count= $count")
        itemStatesCache.invalidateLast()
        repeat(count) {
            itemStatesCache.removeAt(position)
        }
    }
}
