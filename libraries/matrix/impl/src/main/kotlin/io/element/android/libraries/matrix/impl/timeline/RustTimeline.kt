/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.media.MediaUploadHandlerImpl
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.media.toMSC3246range
import io.element.android.libraries.matrix.impl.poll.toInner
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.location.toInner
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.postprocessor.LastForwardIndicatorsPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.LoadingIndicatorsPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.RoomBeginningPostProcessor
import io.element.android.libraries.matrix.impl.timeline.postprocessor.TypingNotificationPostProcessor
import io.element.android.libraries.matrix.impl.timeline.reply.InReplyToMapper
import io.element.android.libraries.matrix.impl.util.MessageEventContent
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.EditedContent
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat
import org.matrix.rustcomponents.sdk.PollData
import org.matrix.rustcomponents.sdk.SendAttachmentJoinHandle
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk_ui.LiveBackPaginationStatus
import java.io.File
import org.matrix.rustcomponents.sdk.EventOrTransactionId as RustEventOrTransactionId
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

private const val PAGINATION_SIZE = 50

class RustTimeline(
    private val inner: InnerTimeline,
    mode: Timeline.Mode,
    systemClock: SystemClock,
    private val matrixRoom: MatrixRoom,
    private val coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val roomContentForwarder: RoomContentForwarder,
    onNewSyncedEvent: () -> Unit,
) : Timeline {
    private val initLatch = CompletableDeferred<Unit>()
    private val isTimelineInitialized = MutableStateFlow(false)

    private val _timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    private val timelineEventContentMapper = TimelineEventContentMapper()
    private val inReplyToMapper = InReplyToMapper(timelineEventContentMapper)
    private val timelineItemMapper = MatrixTimelineItemMapper(
        fetchDetailsForEvent = this::fetchDetailsForEvent,
        coroutineScope = coroutineScope,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = timelineEventContentMapper
        )
    )
    private val timelineDiffProcessor = MatrixTimelineDiffProcessor(
        timelineItems = _timelineItems,
        timelineItemFactory = timelineItemMapper,
    )
    private val timelineItemsSubscriber = TimelineItemsSubscriber(
        timeline = inner,
        timelineCoroutineScope = coroutineScope,
        timelineDiffProcessor = timelineDiffProcessor,
        initLatch = initLatch,
        isTimelineInitialized = isTimelineInitialized,
        dispatcher = dispatcher,
        onNewSyncedEvent = onNewSyncedEvent,
    )

    private val roomBeginningPostProcessor = RoomBeginningPostProcessor(mode)
    private val loadingIndicatorsPostProcessor = LoadingIndicatorsPostProcessor(systemClock)
    private val lastForwardIndicatorsPostProcessor = LastForwardIndicatorsPostProcessor(mode)
    private val typingNotificationPostProcessor = TypingNotificationPostProcessor(mode)

    private val backPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = mode != Timeline.Mode.PINNED_EVENTS)
    )

    private val forwardPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = mode == Timeline.Mode.FOCUSED_ON_EVENT)
    )

    init {
        coroutineScope.fetchMembers()
        if (mode == Timeline.Mode.LIVE) {
            // When timeline is live, we need to listen to the back pagination status as
            // sdk can automatically paginate backwards.
            coroutineScope.registerBackPaginationStatusListener()
        }
    }

    private fun CoroutineScope.registerBackPaginationStatusListener() {
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
        if (!isTimelineInitialized.value) return false
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
        matrixRoom.roomInfoFlow.map { it.creator },
        isTimelineInitialized,
    ) { timelineItems,
        hasMoreToLoadBackward,
        hasMoreToLoadForward,
        roomCreator,
        isTimelineInitialized ->
        withContext(dispatcher) {
            timelineItems
                .let { items ->
                    roomBeginningPostProcessor.process(
                        items = items,
                        isDm = matrixRoom.isDm,
                        roomCreator = roomCreator,
                        hasMoreToLoadBackwards = hasMoreToLoadBackward,
                    )
                }
                .let { items ->
                    loadingIndicatorsPostProcessor.process(
                        items = items,
                        isTimelineInitialized = isTimelineInitialized,
                        hasMoreToLoadBackward = hasMoreToLoadBackward,
                        hasMoreToLoadForward = hasMoreToLoadForward
                    )
                }
                .let { items ->
                    typingNotificationPostProcessor.process(items = items)
                }
                // Keep lastForwardIndicatorsPostProcessor last
                .let { items ->
                    lastForwardIndicatorsPostProcessor.process(
                        items = items,
                        isTimelineInitialized = isTimelineInitialized,
                    )
                }
        }
    }.onStart {
        timelineItemsSubscriber.subscribeIfNeeded()
    }.onCompletion {
        timelineItemsSubscriber.unsubscribeIfNeeded()
    }

    override fun close() {
        coroutineScope.cancel()
        inner.close()
    }

    private fun CoroutineScope.fetchMembers() = launch(dispatcher) {
        initLatch.await()
        try {
            inner.fetchMembers()
        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching members for room ${matrixRoom.roomId}")
        }
    }

    override suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> = withContext(dispatcher) {
        MessageEventContent.from(body, htmlBody, intentionalMentions).use { content ->
            runCatching<Unit> {
                inner.send(content)
            }
        }
    }

    override suspend fun redactEvent(eventOrTransactionId: EventOrTransactionId, reason: String?): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.redactEvent(
                eventOrTransactionId = eventOrTransactionId.toRustEventOrTransactionId(),
                reason = reason,
            )
        }
    }

    override suspend fun editMessage(
        eventOrTransactionId: EventOrTransactionId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> =
        withContext(dispatcher) {
            runCatching<Unit> {
                val editedContent = EditedContent.RoomMessage(
                    content = MessageEventContent.from(
                        body = body,
                        htmlBody = htmlBody,
                        intentionalMentions = intentionalMentions
                    ),
                )
                inner.edit(
                    newContent = editedContent,
                    eventOrTransactionId = eventOrTransactionId.toRustEventOrTransactionId(),
                )
            }
        }

    override suspend fun replyMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val msg = MessageEventContent.from(body, htmlBody, intentionalMentions)
            inner.sendReply(msg, eventId.value)
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
                storeInCache = true,
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
                storeInCache = true,
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
                storeInCache = true,
                progressWatcher = progressCallback?.toProgressWatcher()
            )
        }
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            inner.sendFile(file.path, fileInfo.map(), false, progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.toggleReaction(
                key = emoji,
                itemId = eventOrTransactionId.toRustEventOrTransactionId(),
            )
        }
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = withContext(dispatcher) {
        runCatching {
            roomContentForwarder.forward(fromTimeline = inner, eventId = eventId, toRoomIds = roomIds)
        }.onFailure {
            Timber.e(it)
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
            val editedContent = EditedContent.PollStart(
                pollData = PollData(
                    question = question,
                    answers = answers,
                    maxSelections = maxSelections.toUByte(),
                    pollKind = pollKind.toInner(),
                ),
            )
            inner.edit(
                newContent = editedContent,
                eventOrTransactionId = RustEventOrTransactionId.EventId(pollStartId.value),
            )
        }.map { }
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            inner.sendPollResponse(
                pollStartEventId = pollStartId.value,
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
                pollStartEventId = pollStartId.value,
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
            storeInCache = true,
            progressWatcher = progressCallback?.toProgressWatcher(),
        )
    }

    private fun sendAttachment(files: List<File>, handle: () -> SendAttachmentJoinHandle): Result<MediaUploadHandler> {
        return runCatching {
            MediaUploadHandlerImpl(files, handle())
        }
    }

    override suspend fun loadReplyDetails(eventId: EventId): InReplyTo = withContext(dispatcher) {
        val timelineItem = _timelineItems.first().firstOrNull { timelineItem ->
            timelineItem is MatrixTimelineItem.Event && timelineItem.eventId == eventId
        } as? MatrixTimelineItem.Event

        if (timelineItem != null) {
            InReplyTo.Ready(
                eventId = eventId,
                content = timelineItem.event.content,
                senderId = timelineItem.event.sender,
                senderProfile = timelineItem.event.senderProfile,
            )
        } else {
            inner.loadReplyDetails(eventId.value).use(inReplyToMapper::map)
        }
    }

    override suspend fun pinEvent(eventId: EventId): Result<Boolean> = withContext(dispatcher) {
        runCatching {
            inner.pinEvent(eventId = eventId.value)
        }
    }

    override suspend fun unpinEvent(eventId: EventId): Result<Boolean> = withContext(dispatcher) {
        runCatching {
            inner.unpinEvent(eventId = eventId.value)
        }
    }

    private suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> {
        return runCatching {
            inner.fetchDetailsForEvent(eventId.value)
        }
    }
}
