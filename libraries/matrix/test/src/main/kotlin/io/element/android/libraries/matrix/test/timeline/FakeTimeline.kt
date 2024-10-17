/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.timeline

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
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FakeTimeline(
    private val name: String = "FakeTimeline",
    override val timelineItems: Flow<List<MatrixTimelineItem>> = MutableStateFlow(emptyList()),
    private val backwardPaginationStatus: MutableStateFlow<Timeline.PaginationStatus> = MutableStateFlow(
        Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = true
        )
    ),
    private val forwardPaginationStatus: MutableStateFlow<Timeline.PaginationStatus> = MutableStateFlow(
        Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = false
        )
    ),
    override val membershipChangeEventReceived: Flow<Unit> = MutableSharedFlow(),
) : Timeline {
    var sendMessageLambda: (
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> = sendMessageLambda(body, htmlBody, intentionalMentions)

    var redactEventLambda: (eventOrTransactionId: EventOrTransactionId, reason: String?) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    }

    override suspend fun redactEvent(
        eventOrTransactionId: EventOrTransactionId,
        reason: String?
    ): Result<Unit> = redactEventLambda(eventOrTransactionId, reason)

    var editMessageLambda: (
        eventOrTransactionId: EventOrTransactionId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ) -> Result<Unit> = { _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun editMessage(
        eventOrTransactionId: EventOrTransactionId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> = editMessageLambda(
        eventOrTransactionId,
        body,
        htmlBody,
        intentionalMentions
    )

    var replyMessageLambda: (
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun replyMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ): Result<Unit> = replyMessageLambda(
        eventId,
        body,
        htmlBody,
        intentionalMentions,
        fromNotification,
    )

    var sendImageLambda: (
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendImageLambda(
        file,
        thumbnailFile,
        imageInfo,
        body,
        formattedBody,
        progressCallback
    )

    var sendVideoLambda: (
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendVideoLambda(
        file,
        thumbnailFile,
        videoInfo,
        body,
        formattedBody,
        progressCallback
    )

    var sendAudioLambda: (
        file: File,
        audioInfo: AudioInfo,
        progressCallback: ProgressCallback?,
    ) -> Result<MediaUploadHandler> = { _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendAudioLambda(
        file,
        audioInfo,
        progressCallback
    )

    var sendFileLambda: (
        file: File,
        fileInfo: FileInfo,
        progressCallback: ProgressCallback?,
    ) -> Result<MediaUploadHandler> = { _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendFileLambda(
        file,
        fileInfo,
        progressCallback
    )

    var toggleReactionLambda: (emoji: String, eventOrTransactionId: EventOrTransactionId) -> Result<Unit> = { _, _ -> Result.success(Unit) }
    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit> = toggleReactionLambda(
        emoji,
        eventOrTransactionId
    )

    var forwardEventLambda: (eventId: EventId, roomIds: List<RoomId>) -> Result<Unit> = { _, _ -> Result.success(Unit) }
    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = forwardEventLambda(eventId, roomIds)

    var sendLocationLambda: (
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> = sendLocationLambda(
        body,
        geoUri,
        description,
        zoomLevel,
        assetType
    )

    var createPollLambda: (
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ) -> Result<Unit> = { _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = createPollLambda(
        question,
        answers,
        maxSelections,
        pollKind
    )

    var editPollLambda: (
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = editPollLambda(
        pollStartId,
        question,
        answers,
        maxSelections,
        pollKind
    )

    var sendPollResponseLambda: (
        pollStartId: EventId,
        answers: List<String>,
    ) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>,
    ): Result<Unit> = sendPollResponseLambda(pollStartId, answers)

    var endPollLambda: (
        pollStartId: EventId,
        text: String,
    ) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String,
    ): Result<Unit> = endPollLambda(pollStartId, text)

    var sendVoiceMessageLambda: (
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendVoiceMessageLambda(
        file,
        audioInfo,
        waveform,
        progressCallback
    )

    var sendReadReceiptLambda: (
        eventId: EventId,
        receiptType: ReceiptType,
    ) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    }

    override suspend fun sendReadReceipt(
        eventId: EventId,
        receiptType: ReceiptType,
    ): Result<Unit> = sendReadReceiptLambda(eventId, receiptType)

    var paginateLambda: (direction: Timeline.PaginationDirection) -> Result<Boolean> = {
        Result.success(false)
    }

    override suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> = paginateLambda(direction)

    override fun paginationStatus(direction: Timeline.PaginationDirection): StateFlow<Timeline.PaginationStatus> {
        return when (direction) {
            Timeline.PaginationDirection.BACKWARDS -> backwardPaginationStatus
            Timeline.PaginationDirection.FORWARDS -> forwardPaginationStatus
        }
    }

    var loadReplyDetailsLambda: (eventId: EventId) -> InReplyTo = {
        InReplyTo.NotLoaded(it)
    }

    override suspend fun loadReplyDetails(eventId: EventId) = loadReplyDetailsLambda(eventId)

    var pinEventLambda: (eventId: EventId) -> Result<Boolean> = { lambdaError() }
    override suspend fun pinEvent(eventId: EventId): Result<Boolean> {
        return pinEventLambda(eventId)
    }

    var unpinEventLambda: (eventId: EventId) -> Result<Boolean> = { lambdaError() }
    override suspend fun unpinEvent(eventId: EventId): Result<Boolean> {
        return unpinEventLambda(eventId)
    }

    var closeCounter = 0
        private set

    override fun close() {
        closeCounter++
    }

    override fun toString() = "FakeTimeline: $name"
}
