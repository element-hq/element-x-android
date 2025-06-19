/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.timeline

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
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class FakeTimeline(
    private val name: String = "FakeTimeline",
    override val timelineItems: Flow<List<MatrixTimelineItem>> = MutableStateFlow(emptyList()),
    override val backwardPaginationStatus: MutableStateFlow<Timeline.PaginationStatus> = MutableStateFlow(
        Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = true
        )
    ),
    override val forwardPaginationStatus: MutableStateFlow<Timeline.PaginationStatus> = MutableStateFlow(
        Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = false
        )
    ),
    override val membershipChangeEventReceived: Flow<Unit> = MutableSharedFlow(),
    private val progressCallbackValues: List<Pair<Long, Long>> = emptyList(),
    private val cancelSendResult: (TransactionId) -> Result<Unit> = { lambdaError() },
) : Timeline {
    var sendMessageLambda: (
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ) -> Result<Unit> = { _, _, _ ->
        lambdaError()
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = simulateLongTask {
        cancelSendResult(transactionId)
    }

    override suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit> = simulateLongTask {
        sendMessageLambda(body, htmlBody, intentionalMentions)
    }

    var redactEventLambda: (eventOrTransactionId: EventOrTransactionId, reason: String?) -> Result<Unit> = { _, _ ->
        lambdaError()
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
        lambdaError()
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

    var editCaptionLambda: (
        eventOrTransactionId: EventOrTransactionId,
        caption: String?,
        formattedCaption: String?,
    ) -> Result<Unit> = { _, _, _ ->
        lambdaError()
    }

    override suspend fun editCaption(
        eventOrTransactionId: EventOrTransactionId,
        caption: String?,
        formattedCaption: String?,
    ): Result<Unit> = editCaptionLambda(
        eventOrTransactionId,
        caption,
        formattedCaption,
    )

    var replyMessageLambda: (
        replyParameters: ReplyParameters,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        lambdaError()
    }

    override suspend fun replyMessage(
        replyParameters: ReplyParameters,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean,
    ): Result<Unit> = replyMessageLambda(
        replyParameters,
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
        replyParameters: ReplyParameters?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendImageLambda(
            file,
            thumbnailFile,
            imageInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    var sendVideoLambda: (
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendVideoLambda(
            file,
            thumbnailFile,
            videoInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    var sendAudioLambda: (
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendAudioLambda(
            file,
            audioInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    var sendFileLambda: (
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendFileLambda(
            file,
            fileInfo,
            caption,
            formattedCaption,
            progressCallback,
            replyParameters,
        )
    }

    var sendVoiceMessageLambda: (
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ) -> Result<MediaUploadHandler> = { _, _, _, _, _ ->
        Result.success(FakeMediaUploadHandler())
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> = simulateLongTask {
        simulateSendMediaProgress(progressCallback)
        sendVoiceMessageLambda(
            file,
            audioInfo,
            waveform,
            progressCallback,
            replyParameters,
        )
    }

    var sendLocationLambda: (
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
        replyParameters: ReplyParameters?,
    ) -> Result<Unit> = { _, _, _, _, _, _ ->
        lambdaError()
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
        replyParameters: ReplyParameters?,
    ): Result<Unit> = simulateLongTask {
        sendLocationLambda(
            body,
            geoUri,
            description,
            zoomLevel,
            assetType,
            replyParameters,
        )
    }

    var toggleReactionLambda: (emoji: String, eventOrTransactionId: EventOrTransactionId) -> Result<Unit> = { _, _ -> lambdaError() }

    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit> = simulateLongTask {
        toggleReactionLambda(
            emoji,
            eventOrTransactionId,
        )
    }

    var forwardEventLambda: (eventId: EventId, roomIds: List<RoomId>) -> Result<Unit> = { _, _ -> lambdaError() }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = simulateLongTask {
        forwardEventLambda(eventId, roomIds)
    }

    var createPollLambda: (
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ) -> Result<Unit> = { _, _, _, _ ->
        lambdaError()
    }

    override suspend fun createPoll(question: String, answers: List<String>, maxSelections: Int, pollKind: PollKind): Result<Unit> = simulateLongTask {
        createPollLambda(
            question,
            answers,
            maxSelections,
            pollKind,
        )
    }

    var editPollLambda: (
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        lambdaError()
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind
    ): Result<Unit> = simulateLongTask {
        editPollLambda(
            pollStartId,
            question,
            answers,
            maxSelections,
            pollKind,
        )
    }

    var sendPollResponseLambda: (
        pollStartId: EventId,
        answers: List<String>,
    ) -> Result<Unit> = { _, _ ->
        lambdaError()
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>,
    ): Result<Unit> = simulateLongTask {
        sendPollResponseLambda(
            pollStartId,
            answers,
        )
    }

    var endPollLambda: (
        pollStartId: EventId,
        text: String,
    ) -> Result<Unit> = { _, _ ->
        lambdaError()
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String,
    ): Result<Unit> = simulateLongTask {
        endPollLambda(
            pollStartId,
            text,
        )
    }

    var sendReadReceiptLambda: (
        eventId: EventId,
        receiptType: ReceiptType,
    ) -> Result<Unit> = { _, _ ->
        lambdaError()
    }

    override suspend fun sendReadReceipt(
        eventId: EventId,
        receiptType: ReceiptType,
    ): Result<Unit> = sendReadReceiptLambda(eventId, receiptType)

    var paginateLambda: (direction: Timeline.PaginationDirection) -> Result<Boolean> = {
        Result.success(false)
    }

    override suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> = paginateLambda(direction)

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

    private suspend fun simulateSendMediaProgress(progressCallback: ProgressCallback?) {
        progressCallbackValues.forEach { (current, total) ->
            progressCallback?.onProgress(current, total)
            delay(1)
        }
    }

    override fun toString() = "FakeTimeline: $name"
}
