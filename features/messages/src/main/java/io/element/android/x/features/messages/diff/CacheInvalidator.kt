package io.element.android.x.features.messages.diff

import androidx.recyclerview.widget.ListUpdateCallback
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import timber.log.Timber

internal class CacheInvalidator(private val timelineItemCache: MutableList<MessagesTimelineItemState?>) :
    ListUpdateCallback {

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        Timber.v("onChanged(position= $position, count= $count")
        (position until position + count).forEach {
            // Invalidate cache
            timelineItemCache[it] = null
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        Timber.v("onMoved(fromPosition= $fromPosition, toPosition= $toPosition")
        val model = timelineItemCache.removeAt(fromPosition)
        timelineItemCache.add(toPosition, model)
    }

    override fun onInserted(position: Int, count: Int) {
        Timber.v("onInserted(position= $position, count= $count")
        repeat(count) {
            timelineItemCache.add(position, null)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        Timber.v("onRemoved(position= $position, count= $count")
        repeat(count) {
            timelineItemCache.removeAt(position)
        }
    }

}
