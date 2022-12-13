package io.element.android.x.features.messages.diff

import androidx.recyclerview.widget.DiffUtil
import io.element.android.x.matrix.timeline.MatrixTimelineItem

internal class MatrixTimelineItemsDiffCallback(
    private val oldList: List<MatrixTimelineItem>,
    private val newList: List<MatrixTimelineItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        return if (oldItem is MatrixTimelineItem.Event && newItem is MatrixTimelineItem.Event) {
            oldItem.uniqueId == newItem.uniqueId
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        return oldItem == newItem
    }
}
