/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline

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
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface Timeline : AutoCloseable {
    data class PaginationStatus(
        val isPaginating: Boolean,
        val hasMoreToLoad: Boolean,
    ) {
        val canPaginate: Boolean = !isPaginating && hasMoreToLoad
    }

    enum class PaginationDirection {
        BACKWARDS,
        FORWARDS
    }

    enum class Mode {
        LIVE,
        FOCUSED_ON_EVENT,
        PINNED_EVENTS
    }

    val membershipChangeEventReceived: Flow<Unit>
    suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit>
    suspend fun paginate(direction: PaginationDirection): Result<Boolean>
    fun paginationStatus(direction: PaginationDirection): StateFlow<PaginationStatus>
    val timelineItems: Flow<List<MatrixTimelineItem>>

    suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit>

    suspend fun editMessage(
        eventOrTransactionId: EventOrTransactionId,
        body: String, htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit>

    suspend fun replyMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean = false,
    ): Result<Unit>

    suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    suspend fun redactEvent(eventOrTransactionId: EventOrTransactionId, reason: String?): Result<Unit>

    suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit>

    suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit>

    suspend fun cancelSend(transactionId: TransactionId): Result<Unit> =
        redactEvent(transactionId.toEventOrTransactionId(), reason = null)

    /**
     * Share a location message in the room.
     *
     * @param body A human readable textual representation of the location.
     * @param geoUri A geo URI (RFC 5870) representing the location e.g. `geo:51.5008,0.1247;u=35`.
     *  Respectively: latitude, longitude, and (optional) uncertainty.
     * @param description Optional description of the location to display to the user.
     * @param zoomLevel Optional zoom level to display the map at.
     * @param assetType Optional type of the location asset.
     *  Set to SENDER if sharing own location. Set to PIN if sharing any location.
     */
    suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String? = null,
        zoomLevel: Int? = null,
        assetType: AssetType? = null,
    ): Result<Unit>

    /**
     * Create a poll in the room.
     *
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Edit a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Send a response to a poll.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param answers The list of answer ids to send.
     */
    suspend fun sendPollResponse(pollStartId: EventId, answers: List<String>): Result<Unit>

    /**
     * Ends a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param text Fallback text of the poll end event.
     */
    suspend fun endPoll(pollStartId: EventId, text: String): Result<Unit>

    suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    suspend fun loadReplyDetails(eventId: EventId): InReplyTo

    /**
     * Adds a new pinned event by sending an updated `m.room.pinned_events`
     * event containing the new event id.
     *
     * Returns `true` if we sent the request, `false` if the event was already
     * pinned.
     */
    suspend fun pinEvent(eventId: EventId): Result<Boolean>

    /**
     * Adds a new pinned event by sending an updated `m.room.pinned_events`
     * event without the event id we want to remove.
     *
     * Returns `true` if we sent the request, `false` if the event wasn't
     * pinned
     */
    suspend fun unpinEvent(eventId: EventId): Result<Boolean>
}
