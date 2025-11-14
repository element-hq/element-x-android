/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.impl.media.MediaUploadHandlerImpl
import io.element.android.libraries.matrix.impl.media.map
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.matrix.rustcomponents.sdk.UploadParameters
import org.matrix.rustcomponents.sdk.UploadSource
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk.RoomPaginationStatus
import java.io.File
import org.matrix.rustcomponents.sdk.EventOrTransactionId as RustEventOrTransactionId
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

private const val PAGINATION_SIZE = 50

class RustTimeline(
    private val inner: InnerTimeline,
    override val mode: Timeline.Mode,
    private val systemClock: SystemClock,
    private val joinedRoom: JoinedRoom,
    private val coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val roomContentForwarder: RoomContentForwarder,
) : Timeline {
    private val _timelineItems: MutableSharedFlow<List<MatrixTimelineItem>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    private val _membershipChangeEventReceived = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _onSyncedEventReceived: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1)

    private val timelineEventContentMapper = TimelineEventContentMapper()
    private val inReplyToMapper = InReplyToMapper(timelineEventContentMapper)
    private val timelineItemMapper = MatrixTimelineItemMapper(
        fetchDetailsForEvent = this::fetchDetailsForEvent,
        coroutineScope = coroutineScope,
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = timelineEventContentMapper
        ),
    )
    private val timelineDiffProcessor = MatrixTimelineDiffProcessor(
        timelineItems = _timelineItems,
        membershipChangeEventReceivedFlow = _membershipChangeEventReceived,
        syncedEventReceivedFlow = _onSyncedEventReceived,
        timelineItemMapper = timelineItemMapper,
    )
    private val timelineItemsSubscriber = TimelineItemsSubscriber(
        timeline = inner,
        timelineCoroutineScope = coroutineScope,
        timelineDiffProcessor = timelineDiffProcessor,
        dispatcher = dispatcher,
    )

    private val roomBeginningPostProcessor = RoomBeginningPostProcessor(mode)
    private val loadingIndicatorsPostProcessor = LoadingIndicatorsPostProcessor(systemClock)
    private val lastForwardIndicatorsPostProcessor = LastForwardIndicatorsPostProcessor(mode)
    private val typingNotificationPostProcessor = TypingNotificationPostProcessor(mode)

    override val backwardPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = mode != Timeline.Mode.PinnedEvents)
    )

    override val forwardPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = mode is Timeline.Mode.FocusedOnEvent)
    )

    init {
        when (mode) {
            is Timeline.Mode.Live, is Timeline.Mode.FocusedOnEvent -> coroutineScope.fetchMembers()
            else -> Unit
        }

        if (mode == Timeline.Mode.Live) {
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
                        is RoomPaginationStatus.Idle -> it.copy(isPaginating = false, hasMoreToLoad = !backPaginationStatus.hitTimelineStart)
                        is RoomPaginationStatus.Paginating -> it.copy(isPaginating = true, hasMoreToLoad = true)
                    }
                }
            }
            .launchIn(this)
    }

    override val membershipChangeEventReceived: Flow<Unit> = _membershipChangeEventReceived
        .onStart { timelineItemsSubscriber.subscribeIfNeeded() }
        .onCompletion { timelineItemsSubscriber.unsubscribeIfNeeded() }

    override val onSyncedEventReceived: Flow<Unit> = _onSyncedEventReceived
        .onStart { timelineItemsSubscriber.subscribeIfNeeded() }
        .onCompletion { timelineItemsSubscriber.unsubscribeIfNeeded() }

    override suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.sendReadReceipt(receiptType.toRustReceiptType(), eventId.value)
        }
    }

    override suspend fun markAsRead(receiptType: ReceiptType): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.markAsRead(receiptType.toRustReceiptType())
        }
    }

    private fun updatePaginationStatus(direction: Timeline.PaginationDirection, update: (Timeline.PaginationStatus) -> Timeline.PaginationStatus) {
        when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backwardPaginationStatus.getAndUpdate(update)
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus.getAndUpdate(update)
        }
    }

    // Use NonCancellable to avoid breaking the timeline when the coroutine is cancelled.
    override suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> = withContext(NonCancellable) {
        withContext(dispatcher) {
            runCatchingExceptions {
                if (!canPaginate(direction)) throw TimelineException.CannotPaginate
                updatePaginationStatus(direction) { it.copy(isPaginating = true) }
                when (direction) {
                    Timeline.PaginationDirection.BACKWARDS -> inner.paginateBackwards(PAGINATION_SIZE.toUShort())
                    Timeline.PaginationDirection.FORWARDS -> inner.paginateForwards(PAGINATION_SIZE.toUShort())
                }
            }.onFailure { error ->
                if (error is TimelineException.CannotPaginate) {
                    Timber.d("Can't paginate $direction on room ${joinedRoom.roomId} with paginationStatus: ${backwardPaginationStatus.value}")
                } else {
                    updatePaginationStatus(direction) { it.copy(isPaginating = false) }
                    Timber.e(error, "Error paginating $direction on room ${joinedRoom.roomId}")
                }
            }.onSuccess { hasReachedEnd ->
                updatePaginationStatus(direction) { it.copy(isPaginating = false, hasMoreToLoad = !hasReachedEnd) }
            }
        }
    }

    private fun canPaginate(direction: Timeline.PaginationDirection): Boolean {
        return when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backwardPaginationStatus.value.canPaginate
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus.value.canPaginate
        }
    }

    override val timelineItems: Flow<List<MatrixTimelineItem>> = combine(
        _timelineItems,
        backwardPaginationStatus,
        forwardPaginationStatus,
        joinedRoom.roomInfoFlow.map { it.creators to it.isDm }.distinctUntilChanged(),
    ) {
        timelineItems,
        backwardPaginationStatus,
        forwardPaginationStatus,
        (roomCreators, isDm),
        ->
        withContext(dispatcher) {
            timelineItems
                .let { items ->
                    roomBeginningPostProcessor.process(
                        items = items,
                        isDm = isDm,
                        roomCreator = roomCreators.firstOrNull(),
                        hasMoreToLoadBackwards = backwardPaginationStatus.hasMoreToLoad,
                    )
                }
                .let { items ->
                    loadingIndicatorsPostProcessor.process(
                        items = items,
                        hasMoreToLoadBackward = backwardPaginationStatus.hasMoreToLoad,
                        hasMoreToLoadForward = forwardPaginationStatus.hasMoreToLoad,
                    )
                }
                .let { items ->
                    typingNotificationPostProcessor.process(items = items)
                }
                // Keep lastForwardIndicatorsPostProcessor last
                .let { items ->
                    lastForwardIndicatorsPostProcessor.process(items = items)
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
        try {
            inner.fetchMembers()
        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching members for room ${joinedRoom.roomId}")
        }
    }

    override suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> = withContext(dispatcher) {
        MessageEventContent.from(body, htmlBody, intentionalMentions).use { content ->
            runCatchingExceptions<Unit> {
                inner.send(content)
            }
        }
    }

    override suspend fun redactEvent(eventOrTransactionId: EventOrTransactionId, reason: String?): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
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
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
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

    override suspend fun editCaption(
        eventOrTransactionId: EventOrTransactionId,
        caption: String?,
        formattedCaption: String?,
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions<Unit> {
            val editedContent = EditedContent.MediaCaption(
                caption = caption,
                formattedCaption = formattedCaption?.let {
                    FormattedBody(body = it, format = MessageFormat.Html)
                },
                mentions = null,
            )
            withContext(Dispatchers.IO) {
                inner.edit(
                    newContent = editedContent,
                    eventOrTransactionId = eventOrTransactionId.toRustEventOrTransactionId(),
                )
            }
        }
    }

    override suspend fun replyMessage(
        repliedToEventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            val msg = MessageEventContent.from(body, htmlBody, intentionalMentions)
            inner.sendReply(
                msg = msg,
                eventId = repliedToEventId.value,
            )
        }
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler> {
        Timber.d("Sending image ${file.path.hash()}")
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            inner.sendImage(
                params = UploadParameters(
                    source = UploadSource.File(file.path),
                    caption = caption,
                    formattedCaption = formattedCaption?.let {
                        FormattedBody(body = it, format = MessageFormat.Html)
                    },
                    mentions = null,
                    inReplyTo = inReplyToEventId?.value,
                ),
                thumbnailSource = thumbnailFile?.path?.let(UploadSource::File),
                imageInfo = imageInfo.map(),
            )
        }
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler> {
        Timber.d("Sending video ${file.path.hash()}")
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            inner.sendVideo(
                params = UploadParameters(
                    source = UploadSource.File(file.path),
                    caption = caption,
                    formattedCaption = formattedCaption?.let {
                        FormattedBody(body = it, format = MessageFormat.Html)
                    },
                    mentions = null,
                    inReplyTo = inReplyToEventId?.value,
                ),
                thumbnailSource = thumbnailFile?.path?.let(UploadSource::File),
                videoInfo = videoInfo.map(),
            )
        }
    }

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler> {
        Timber.d("Sending audio ${file.path.hash()}")
        return sendAttachment(listOf(file)) {
            inner.sendAudio(
                params = UploadParameters(
                    source = UploadSource.File(file.path),
                    caption = caption,
                    formattedCaption = formattedCaption?.let {
                        FormattedBody(body = it, format = MessageFormat.Html)
                    },
                    mentions = null,
                    inReplyTo = inReplyToEventId?.value,
                ),
                audioInfo = audioInfo.map(),
            )
        }
    }

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler> {
        Timber.d("Sending file ${file.path.hash()}")
        return sendAttachment(listOf(file)) {
            inner.sendFile(
                params = UploadParameters(
                    source = UploadSource.File(file.path),
                    caption = caption,
                    formattedCaption = formattedCaption?.let {
                        FormattedBody(body = it, format = MessageFormat.Html)
                    },
                    mentions = null,
                    inReplyTo = inReplyToEventId?.value,
                ),
                fileInfo = fileInfo.map(),
            )
        }
    }

    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Boolean> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.toggleReaction(
                key = emoji,
                itemId = eventOrTransactionId.toRustEventOrTransactionId(),
            )
        }
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
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
        inReplyToEventId: EventId?,
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.sendLocation(
                body = body,
                geoUri = geoUri,
                description = description,
                zoomLevel = zoomLevel?.toUByte(),
                assetType = assetType?.toInner(),
                repliedToEventId = inReplyToEventId?.value,
            )
        }
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            inner.sendVoiceMessage(
                params = UploadParameters(
                    source = UploadSource.File(file.path),
                    // Maybe allow a caption in the future?
                    caption = null,
                    formattedCaption = null,
                    mentions = null,
                    inReplyTo = inReplyToEventId?.value,
                ),
                audioInfo = audioInfo.map(),
                waveform = waveform,
            )
        }
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
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
        runCatchingExceptions {
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
        }
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
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
        runCatchingExceptions {
            inner.endPoll(
                pollStartEventId = pollStartId.value,
                text = text,
            )
        }
    }

    private fun sendAttachment(files: List<File>, handle: () -> SendAttachmentJoinHandle): Result<MediaUploadHandler> {
        return runCatchingExceptions {
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
        runCatchingExceptions {
            inner.pinEvent(eventId = eventId.value)
        }
    }

    override suspend fun unpinEvent(eventId: EventId): Result<Boolean> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.unpinEvent(eventId = eventId.value)
        }
    }

    override suspend fun getLatestEventId(): Result<EventId?> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.latestEventId()?.let(::EventId)
        }
    }

    private suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            inner.fetchDetailsForEvent(eventId.value)
        }
    }
}
