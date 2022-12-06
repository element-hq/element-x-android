package io.element.android.x.features.messages.diff

import androidx.recyclerview.widget.ListUpdateCallback
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.util.invalidateLast
import timber.log.Timber

internal class CacheInvalidator(private val itemStatesCache: MutableList<MessagesTimelineItemState?>) :
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
