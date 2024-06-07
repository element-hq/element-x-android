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
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
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
        mentions: List<Mention>,
    ) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
    ): Result<Unit> = sendMessageLambda(body, htmlBody, mentions)

    var editMessageLambda: (
        originalEventId: EventId?,
        transactionId: TransactionId?,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun editMessage(
        originalEventId: EventId?,
        transactionId: TransactionId?,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
    ): Result<Unit> = editMessageLambda(
        originalEventId,
        transactionId,
        body,
        htmlBody,
        mentions
    )

    var enterSpecialModeLambda: (eventId: EventId?) -> Result<Unit> = {
        Result.success(Unit)
    }

    override suspend fun enterSpecialMode(eventId: EventId?): Result<Unit> = enterSpecialModeLambda(eventId)

    var replyMessageLambda: (
        eventId: EventId,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
        fromNotification: Boolean,
    ) -> Result<Unit> = { _, _, _, _, _ ->
        Result.success(Unit)
    }

    override suspend fun replyMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
        fromNotification: Boolean,
    ): Result<Unit> = replyMessageLambda(
        eventId,
        body,
        htmlBody,
        mentions,
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

    var toggleReactionLambda: (emoji: String, eventId: EventId) -> Result<Unit> = { _, _ -> Result.success(Unit) }
    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> = toggleReactionLambda(emoji, eventId)

    var forwardEventLambda: (eventId: EventId, roomIds: List<RoomId>) -> Result<Unit> = { _, _ -> Result.success(Unit) }
    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = forwardEventLambda(eventId, roomIds)

    var retrySendMessageLambda: (transactionId: TransactionId) -> Result<Unit> = { Result.success(Unit) }
    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> = retrySendMessageLambda(transactionId)

    var cancelSendLambda: (transactionId: TransactionId) -> Result<Unit> = { Result.success(Unit) }
    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = cancelSendLambda(transactionId)

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

    var closeCounter = 0
        private set

    override fun close() {
        closeCounter++
    }

    override fun toString() = "FakeTimeline: $name"
}
