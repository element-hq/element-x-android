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

package io.element.android.features.messages.timeline

import androidx.recyclerview.widget.DiffUtil
import io.element.android.features.messages.timeline.diff.CacheInvalidator
import io.element.android.features.messages.timeline.diff.MatrixTimelineItemsDiffCallback
import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.MessagesItemGroupPosition
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.features.messages.timeline.model.content.TimelineItemContent
import io.element.android.features.messages.timeline.model.content.TimelineItemEmoteContent
import io.element.android.features.messages.timeline.model.content.TimelineItemEncryptedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemImageContent
import io.element.android.features.messages.timeline.model.content.TimelineItemNoticeContent
import io.element.android.features.messages.timeline.model.content.TimelineItemRedactedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemTextContent
import io.element.android.features.messages.timeline.model.content.TimelineItemUnknownContent
import io.element.android.features.messages.timeline.util.invalidateLast
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.core.EventId
import io.element.android.libraries.matrix.media.MediaResolver
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.matrix.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.ui.MatrixItemHelper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat
import org.matrix.rustcomponents.sdk.MessageType
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class TimelineItemsFactory @Inject constructor(
    private val matrixItemHelper: MatrixItemHelper,
    private val room: MatrixRoom,
    private val dispatcher: CoroutineDispatcher,
) {

    private val timelineItems = MutableStateFlow<List<TimelineItem>>(emptyList())
    private val timelineItemsCache = arrayListOf<TimelineItem?>()

    // Items from rust sdk, used for diffing
    private var matrixTimelineItems: List<MatrixTimelineItem> = emptyList()

    private val lock = Mutex()
    private val cacheInvalidator = CacheInvalidator(timelineItemsCache)

    fun flow(): StateFlow<List<TimelineItem>> = timelineItems.asStateFlow()

    suspend fun replaceWith(
        timelineItems: List<MatrixTimelineItem>,
    ) = withContext(dispatcher) {
        lock.withLock {
            calculateAndApplyDiff(timelineItems)
            buildAndEmitTimelineItemStates(timelineItems)
        }
    }

    suspend fun pushItem(
        timelineItem: MatrixTimelineItem,
    ) = withContext(dispatcher) {
        lock.withLock {
            // Makes sure to invalidate last as we need to recompute some data (like groupPosition)
            timelineItemsCache.invalidateLast()
            timelineItemsCache.add(null)
            matrixTimelineItems = matrixTimelineItems + timelineItem
            buildAndEmitTimelineItemStates(matrixTimelineItems)
        }
    }

    private suspend fun buildAndEmitTimelineItemStates(timelineItems: List<MatrixTimelineItem>) {
        val newTimelineItemStates = ArrayList<TimelineItem>()
        for (index in timelineItemsCache.indices.reversed()) {
            val cacheItem = timelineItemsCache[index]
            if (cacheItem == null) {
                buildAndCacheItem(timelineItems, index)?.also { timelineItemState ->
                    newTimelineItemStates.add(timelineItemState)
                }
            } else {
                newTimelineItemStates.add(cacheItem)
            }
        }
        this.timelineItems.emit(newTimelineItemStates)
    }

    private fun calculateAndApplyDiff(newTimelineItems: List<MatrixTimelineItem>) {
        val timeToDiff = measureTimeMillis {
            val diffCallback =
                MatrixTimelineItemsDiffCallback(
                    oldList = matrixTimelineItems,
                    newList = newTimelineItems
                )
            val diffResult = DiffUtil.calculateDiff(diffCallback, false)
            matrixTimelineItems = newTimelineItems
            diffResult.dispatchUpdatesTo(cacheInvalidator)
        }
        Timber.v("Time to apply diff on new list of ${newTimelineItems.size} items: $timeToDiff ms")
    }

    private suspend fun buildAndCacheItem(
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): TimelineItem? {
        val timelineItemState =
            when (val currentTimelineItem = timelineItems[index]) {
                is MatrixTimelineItem.Event -> {
                    buildMessageEvent(
                        currentTimelineItem,
                        index,
                        timelineItems,
                    )
                }
                is MatrixTimelineItem.Virtual -> TimelineItem.Virtual(
                    "virtual_item_$index"
                )
                MatrixTimelineItem.Other -> null
            }
        timelineItemsCache[index] = timelineItemState
        return timelineItemState
    }

    private suspend fun buildMessageEvent(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
    ): TimelineItem.MessageEvent {
        val currentSender = currentTimelineItem.event.sender()
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderDisplayName = room.userDisplayName(currentSender).getOrNull()
        val senderAvatarUrl = room.userAvatarUrl(currentSender).getOrNull()
        val senderAvatarData = AvatarData(
            id = currentSender,
            name = senderDisplayName,
            url = senderAvatarUrl,
            size = AvatarSize.SMALL
        )
        return TimelineItem.MessageEvent(
            id = EventId(currentTimelineItem.uniqueId),
            senderId = currentSender,
            senderDisplayName = senderDisplayName,
            senderAvatar = senderAvatarData,
            content = currentTimelineItem.computeContent(),
            isMine = currentTimelineItem.event.isOwn(),
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState()
        )
    }

    private fun MatrixTimelineItem.Event.computeReactionsState(): TimelineItemReactions {
        val aggregatedReactions = event.reactions().map {
            AggregatedReaction(key = it.key, count = it.count.toString(), isHighlighted = false)
        }
        return TimelineItemReactions(aggregatedReactions.toImmutableList())
    }

    private fun MatrixTimelineItem.Event.computeContent(): TimelineItemContent {
        val content = event.content()
        content.asUnableToDecrypt()?.let { encryptedMessage ->
            return TimelineItemEncryptedContent(encryptedMessage)
        }
        if (content.isRedactedMessage()) {
            return TimelineItemRedactedContent
        }
        val contentAsMessage = content.asMessage()
        return when (val messageType = contentAsMessage?.msgtype()) {
            is MessageType.Emote -> TimelineItemEmoteContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            is MessageType.Image -> {
                val height = messageType.content.info?.height?.toFloat()
                val width = messageType.content.info?.width?.toFloat()
                val aspectRatio = if (height != null && width != null) {
                    width / height
                } else {
                    0.7f
                }
                TimelineItemImageContent(
                    body = messageType.content.body,
                    imageMeta = MediaResolver.Meta(
                        source = messageType.content.source,
                        kind = MediaResolver.Kind.Content
                    ),
                    blurhash = messageType.content.info?.blurhash,
                    aspectRatio = aspectRatio
                )
            }
            is MessageType.Notice -> TimelineItemNoticeContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            is MessageType.Text -> TimelineItemTextContent(
                body = messageType.content.body,
                htmlDocument = messageType.content.formatted?.toHtmlDocument()
            )
            else -> TimelineItemUnknownContent
        }
    }

    private fun FormattedBody.toHtmlDocument(): Document? {
        return takeIf { it.format == MessageFormat.HTML }?.body?.let { formattedBody ->
            Jsoup.parse(formattedBody)
        }
    }

    private fun computeGroupPosition(
        currentTimelineItem: MatrixTimelineItem.Event,
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): MessagesItemGroupPosition {
        val prevTimelineItem =
            timelineItems.getOrNull(index - 1) as? MatrixTimelineItem.Event
        val nextTimelineItem =
            timelineItems.getOrNull(index + 1) as? MatrixTimelineItem.Event
        val currentSender = currentTimelineItem.event.sender()
        val previousSender = prevTimelineItem?.event?.sender()
        val nextSender = nextTimelineItem?.event?.sender()

        return when {
            previousSender != currentSender && nextSender == currentSender -> MessagesItemGroupPosition.First
            previousSender == currentSender && nextSender == currentSender -> MessagesItemGroupPosition.Middle
            previousSender == currentSender && nextSender != currentSender -> MessagesItemGroupPosition.Last
            else -> MessagesItemGroupPosition.None
        }
    }
}
