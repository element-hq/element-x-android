/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.media.MediaUploadHandlerImpl
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.media.toMSC3246range
import io.element.android.libraries.matrix.impl.poll.toInner
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.location.toInner
import io.element.android.libraries.matrix.impl.room.map
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.postprocessor.LastForwardIndicatorsPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.LoadingIndicatorsPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.RoomBeginningPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.TimelineEncryptedHistoryPostProcessor
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.SendAttachmentJoinHandle
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.messageEventContentFromHtml
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk_ui.EventItemOrigin
import uniffi.matrix_sdk_ui.LiveBackPaginationStatus
import java.io.File
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

private const val INITIAL_MAX_SIZE = 50
private const val PAGINATION_SIZE = 50

class RustTimeline(
    private val inner: InnerTimeline,
    isLive: Boolean,
    systemClock: SystemClock,
    roomCoroutineScope: CoroutineScope,
    isKeyBackupEnabled: Boolean,
    private val matrixRoom: MatrixRoom,
    private val dispatcher: CoroutineDispatcher,
    lastLoginTimestamp: Date?,
    private val roomContentForwarder: RoomContentForwarder,
    private val onNewSyncedEvent: () -> Unit,
) : Timeline {
    private val initLatch = CompletableDeferred<Unit>()
    private val isInit = AtomicBoolean(false)

    private val _timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val encryptedHistoryPostProcessor = TimelineEncryptedHistoryPostProcessor(
        lastLoginTimestamp = lastLoginTimestamp,
        isRoomEncrypted = matrixRoom.isEncrypted,
        isKeyBackupEnabled = isKeyBackupEnabled,
        dispatcher = dispatcher,
    )

    private val roomBeginningPostProcessor = RoomBeginningPostProcessor()
    private val loadingIndicatorsPostProcessor = LoadingIndicatorsPostProcessor(systemClock)
    private val lastForwardIndicatorsPostProcessor = LastForwardIndicatorsPostProcessor(isLive)

    private val timelineItemFactory = MatrixTimelineItemMapper(
        fetchDetailsForEvent = this::fetchDetailsForEvent,
        roomCoroutineScope = roomCoroutineScope,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = TimelineEventContentMapper(
                eventMessageMapper = EventMessageMapper()
            )
        )
    )

    private val timelineDiffProcessor = MatrixTimelineDiffProcessor(
        timelineItems = _timelineItems,
        timelineItemFactory = timelineItemFactory,
    )

    private val backPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = true)
    )

    private val forwardPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = !isLive)
    )

    init {
        roomCoroutineScope.launch(dispatcher) {
            inner.timelineDiffFlow { initialList ->
                postItems(initialList)
            }.onEach { diffs ->
                if (diffs.any { diff -> diff.eventOrigin() == EventItemOrigin.SYNC }) {
                    onNewSyncedEvent()
                }
                postDiffs(diffs)
            }.launchIn(this)

            launch {
                fetchMembers()
            }

            if (isLive) {
                // When timeline is live, we need to listen to the back pagination status as
                // sdk can automatically paginate backwards.
                inner.liveBackPaginationStatus()
                    .onEach { backPaginationStatus ->
                        updatePaginationStatus(Timeline.PaginationDirection.BACKWARDS) {
                            when (backPaginationStatus) {
                                is LiveBackPaginationStatus.Idle -> it.copy(isPaginating = false, hasMoreToLoad = !backPaginationStatus.hitStartOfTimeline)
                                is LiveBackPaginationStatus.Paginating -> it.copy(isPaginating = true, hasMoreToLoad = true)
                            }
                        }
                    }
                    .launchIn(this)
            }
        }
    }

    override val membershipChangeEventReceived: Flow<Unit> = timelineDiffProcessor.membershipChangeEventReceived

    override suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit> {
        return runCatching {
            inner.sendReadReceipt(receiptType.toRustReceiptType(), eventId.value)
        }
    }

    private fun updatePaginationStatus(direction: Timeline.PaginationDirection, update: (Timeline.PaginationStatus) -> Timeline.PaginationStatus) {
        when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backPaginationStatus.getAndUpdate(update)
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus.getAndUpdate(update)
        }
    }

    // Use NonCancellable to avoid breaking the timeline when the coroutine is cancelled.
    override suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> = withContext(NonCancellable) {
        withContext(dispatcher) {
            initLatch.await()
            runCatching {
                if (!canPaginate(direction)) throw TimelineException.CannotPaginate
                updatePaginationStatus(direction) { it.copy(isPaginating = true) }
                when (direction) {
                    Timeline.PaginationDirection.BACKWARDS -> inner.paginateBackwards(PAGINATION_SIZE.toUShort())
                    Timeline.PaginationDirection.FORWARDS -> inner.focusedPaginateForwards(PAGINATION_SIZE.toUShort())
                }
            }.onFailure { error ->
                updatePaginationStatus(direction) { it.copy(isPaginating = false) }
                if (error is TimelineException.CannotPaginate) {
                    Timber.d("Can't paginate $direction on room ${matrixRoom.roomId} with paginationStatus: ${backPaginationStatus.value}")
                } else {
                    Timber.e(error, "Error paginating $direction on room ${matrixRoom.roomId}")
                }
            }.onSuccess { hasReachedEnd ->
                updatePaginationStatus(direction) { it.copy(isPaginating = false, hasMoreToLoad = !hasReachedEnd) }
            }
        }
    }

    private fun canPaginate(direction: Timeline.PaginationDirection): Boolean {
        if (!isInit.get()) return false
        return when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backPaginationStatus.value.canPaginate
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus.value.canPaginate
        }
    }

    override fun paginationStatus(direction: Timeline.PaginationDirection): StateFlow<Timeline.PaginationStatus> {
        return when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backPaginationStatus
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus
        }
    }

    override val timelineItems: Flow<List<MatrixTimelineItem>> = combine(
        _timelineItems,
        backPaginationStatus.map { it.hasMoreToLoad }.distinctUntilChanged(),
        forwardPaginationStatus.map { it.hasMoreToLoad }.distinctUntilChanged(),
    ) { timelineItems, hasMoreToLoadBackward, hasMoreToLoadForward ->
        withContext(dispatcher) {
            timelineItems
                .let { items -> encryptedHistoryPostProcessor.process(items) }
                .let { items ->
                    roomBeginningPostProcessor.process(
                        items = items,
                        isDm = matrixRoom.isDm,
                        hasMoreToLoadBackwards = hasMoreToLoadBackward
                    )
                }
                .let { items -> loadingIndicatorsPostProcessor.process(items, hasMoreToLoadBackward, hasMoreToLoadForward) }
                // Keep lastForwardIndicatorsPostProcessor last
                .let { items -> lastForwardIndicatorsPostProcessor.process(items) }
        }
    }

    override fun close() {
        inner.close()
        specialModeEventTimelineItem?.destroy()
    }

    private suspend fun fetchMembers() = withContext(dispatcher) {
        initLatch.await()
        try {
            inner.fetchMembers()
        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching members for room ${matrixRoom.roomId}")
        }
    }

    private suspend fun postItems(items: List<TimelineItem>) = coroutineScope {
        // Split the initial items in multiple list as there is no pagination in the cached data, so we can post timelineItems asap.
        items.chunked(INITIAL_MAX_SIZE).reversed().forEach {
            ensureActive()
            timelineDiffProcessor.postItems(it)
        }
        isInit.set(true)
        initLatch.complete(Unit)
    }

    private suspend fun postDiffs(diffs: List<TimelineDiff>) {
        initLatch.await()
        timelineDiffProcessor.postDiffs(diffs)
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit> = withContext(dispatcher) {
        messageEventContentFromParts(body, htmlBody).withMentions(mentions.map()).use { content ->
            runCatching {
                inner.send(content)
            }
        }
    }

    override suspend fun editMessage(
        originalEventId: EventId?,
        transactionId: TransactionId?,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
    ): Result<Unit> =
        withContext(dispatcher) {
            if (originalEventId != null) {
                runCatching {
                    val editedEvent = specialModeEventTimelineItem ?: inner.getEventTimelineItemByEventId(originalEventId.value)
                    editedEvent.use {
                        inner.edit(
                            newContent = messageEventContentFromParts(body, htmlBody).withMentions(mentions.map()),
                            editItem = it,
                        )
                    }
                    specialModeEventTimelineItem = null
                }
            } else {
                runCatching {
                    transactionId?.let { cancelSend(it) }
                    inner.send(messageEventContentFromParts(body, htmlBody))
                }
            }
        }

    private var specialModeEventTimelineItem: EventTimelineItem? = null

    override suspend fun enterSpecialMode(eventId: EventId?): Result<Unit> = withContext(dispatcher) {
        runCatching {
            specialModeEventTimelineItem?.destroy()
            specialModeEventTimelineItem = null
            specialModeEventTimelineItem = eventId?.let { inner.getEventTimelineItemByEventId(it.value) }
        }.onFailure {
            Timber.e(it, "Unable to retrieve event for special mode. Are you using the correct timeline?")
        }
    }

    override suspend fun replyMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
        fromNotification: Boolean,
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val msg = messageEventContentFromParts(body, htmlBody).withMentions(mentions.map())
            if (fromNotification) {
                // When replying from a notification, do not interfere with `specialModeEventTimelineItem`
                val inReplyTo = inner.getEventTimelineItemByEventId(eventId.value)
                inReplyTo.use { eventTimelineItem ->
                    inner.sendReply(msg, eventTimelineItem)
                }
            } else {
                val inReplyTo = specialModeEventTimelineItem ?: inner.getEventTimelineItemByEventId(eventId.value)
                inReplyTo.use { eventTimelineItem ->
                    inner.sendReply(msg, eventTimelineItem)
                }
                specialModeEventTimelineItem = null
            }
        }
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            inner.sendImage(
                url = file.path,
                thumbnailUrl = thumbnailFile?.path,
                imageInfo = imageInfo.map(),
                caption = body,
                formattedCaption = formattedBody?.let {
                    FormattedBody(body = it, format = MessageFormat.Html)
                },
                progressWatcher = progressCallback?.toProgressWatcher()
            )
        }
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            inner.sendVideo(
                url = file.path,
                thumbnailUrl = thumbnailFile?.path,
                videoInfo = videoInfo.map(),
                caption = body,
                formattedCaption = formattedBody?.let {
                    FormattedBody(body = it, format = MessageFormat.Html)
                },
                progressWatcher = progressCallback?.toProgressWatcher()
            )
        }
    }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            inner.sendAudio(
                url = file.path,
                audioInfo = audioInfo.map(),
                // Maybe allow a caption in the future?
                caption = null,
                formattedCaption = null,
                progressWatcher = progressCallback?.toProgressWatcher()
            )
        }
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            inner.sendFile(file.path, fileInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.toggleReaction(key = emoji, eventId = eventId.value)
        }
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = withContext(dispatcher) {
        runCatching {
            roomContentForwarder.forward(fromTimeline = inner, eventId = eventId, toRoomIds = roomIds)
        }.onFailure {
            Timber.e(it)
        }
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.retrySend(transactionId.value)
        }
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.cancelSend(transactionId.value)
        }
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.sendLocation(
                body = body,
                geoUri = geoUri,
                description = description,
                zoomLevel = zoomLevel?.toUByte(),
                assetType = assetType?.toInner(),
            )
        }
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.createPoll(
                question = question,
                answers = answers,
                maxSelections = maxSelections.toUByte(),
                pollKind = pollKind.toInner(),
            )
        }
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val pollStartEvent =
                inner.getEventTimelineItemByEventId(
                    eventId = pollStartId.value
                )
            pollStartEvent.use {
                inner.editPoll(
                    question = question,
                    answers = answers,
                    maxSelections = maxSelections.toUByte(),
                    pollKind = pollKind.toInner(),
                    editItem = pollStartEvent,
                )
            }
        }
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.sendPollResponse(
                pollStartId = pollStartId.value,
                answers = answers,
            )
        }
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.endPoll(
                pollStartId = pollStartId.value,
                text = text,
            )
        }
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendAttachment(listOf(file)) {
        inner.sendVoiceMessage(
            url = file.path,
            audioInfo = audioInfo.map(),
            waveform = waveform.toMSC3246range(),
            // Maybe allow a caption in the future?
            caption = null,
            formattedCaption = null,
            progressWatcher = progressCallback?.toProgressWatcher(),
        )
    }

    private fun messageEventContentFromParts(body: String, htmlBody: String?): RoomMessageEventContentWithoutRelation =
        if (htmlBody != null) {
            messageEventContentFromHtml(body, htmlBody)
        } else {
            messageEventContentFromMarkdown(body)
        }

    private fun sendAttachment(files: List<File>, handle: () -> SendAttachmentJoinHandle): Result<MediaUploadHandler> {
        return runCatching {
            MediaUploadHandlerImpl(files, handle())
        }
    }

    private suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> {
        return runCatching {
            inner.fetchDetailsForEvent(eventId.value)
        }
    }
}
