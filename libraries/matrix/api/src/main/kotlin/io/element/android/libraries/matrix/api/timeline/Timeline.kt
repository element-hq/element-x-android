/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.GalleryItemInfo
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
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * A window over the events of a room, restricted by its [Mode], and the entry point for sending events to that room.
 *
 * A timeline owns SDK resources and must be closed once no longer needed, except the live timeline, which is owned by its room.
 * Sending returns successfully as soon as the event has been handed to the room's send queue, not when the server has accepted it.
 */
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

    @Parcelize
    @Immutable
    sealed interface Mode : Parcelable {
        data object Live : Mode
        data class FocusedOnEvent(val eventId: EventId) : Mode
        data object PinnedEvents : Mode
        data object Media : Mode
        data class Thread(val threadRootId: ThreadId) : Mode
    }

    /** What this timeline is a view over: the live room, a single event and its context, the pinned events, the media, or a thread. */
    val mode: Mode

    /**
     * Emits every time a membership change event is received in this timeline, without saying which one.
     * Collecting it keeps the timeline subscribed to the SDK, so it should not be collected for longer than needed.
     */
    val membershipChangeEventReceived: Flow<Unit>

    /**
     * Emits every time an event coming from sync is received in this timeline, without saying which one.
     * As with [membershipChangeEventReceived], collecting it keeps the timeline subscribed to the SDK.
     */
    val onSyncedEventReceived: Flow<Unit>

    /**
     * Sends a read receipt for a single event, which is how the unread state of the room is cleared.
     *
     * @param eventId the event the receipt points at.
     * @param receiptType whether the receipt is public, private or only marks the fully read position.
     */
    suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit>

    /**
     * Marks the whole timeline as read by sending a receipt on its latest event.
     *
     * @param receiptType whether the receipt is public, private or only marks the fully read position.
     */
    suspend fun markAsRead(receiptType: ReceiptType): Result<Unit>

    /**
     * Loads one more page of events and updates the matching pagination status flow.
     * Fails with a `CannotPaginate` error when the corresponding status does not allow it, for instance because a pagination is already running.
     *
     * @param direction whether to load older or newer events.
     * @return whether the end of the timeline has been reached in that direction.
     */
    suspend fun paginate(direction: PaginationDirection): Result<Boolean>

    /** Whether older events are being loaded and whether more remain; pinned event timelines start with nothing more to load. */
    val backwardPaginationStatus: StateFlow<PaginationStatus>

    /** Whether newer events are being loaded and whether more remain; only a timeline focused on an event starts with more to load. */
    val forwardPaginationStatus: StateFlow<PaginationStatus>

    /**
     * The items to display, in timeline order, re-emitted on every change.
     * Beyond the room events this also carries the virtual items the UI needs, such as date separators, loading indicators and the room beginning.
     */
    val timelineItems: Flow<List<MatrixTimelineItem>>

    /**
     * Sends a text message to the room this timeline belongs to.
     *
     * @param body the message content, as plain text.
     * @param htmlBody the message content as HTML, or `null` to send it unformatted.
     * @param intentionalMentions the users and rooms the message deliberately mentions, so that they are notified.
     * @param msgType the Matrix message type to use, which distinguishes a normal message from an emote or a notice.
     * @param asPlainText whether to send the message without any formatting, ignoring markdown in [body].
     */
    suspend fun sendMessage(
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        msgType: MsgType = MsgType.MSG_TYPE_TEXT,
        asPlainText: Boolean = false,
    ): Result<Unit>

    /**
     * Replaces the content of a message, whether it has already been sent or is still local in the send queue.
     *
     * @param eventOrTransactionId the event to edit, identified by its event id once sent or by its transaction id while still local.
     * @param body the new content, as plain text.
     * @param htmlBody the new content as HTML, or `null` to send it unformatted.
     * @param intentionalMentions the users and rooms the new content deliberately mentions.
     */
    suspend fun editMessage(
        eventOrTransactionId: EventOrTransactionId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
    ): Result<Unit>

    /**
     * Replaces the caption of an already sent media message, leaving the media itself untouched.
     *
     * @param eventOrTransactionId the media event to edit, identified by its event id or by its transaction id while still local.
     * @param caption the new caption, or `null` to remove it.
     * @param formattedCaption the new caption as HTML, or `null` to send it unformatted.
     */
    suspend fun editCaption(
        eventOrTransactionId: EventOrTransactionId,
        caption: String?,
        formattedCaption: String?,
    ): Result<Unit>

    /**
     * Sends a message as a reply to another event.
     *
     * @param repliedToEventId the event being replied to.
     * @param body the reply content, as plain text.
     * @param htmlBody the reply content as HTML, or `null` to send it unformatted.
     * @param intentionalMentions the users and rooms the reply deliberately mentions.
     * @param fromNotification whether the reply was written from a notification rather than from the timeline.
     * @param msgType the Matrix message type to use.
     */
    suspend fun replyMessage(
        repliedToEventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>,
        fromNotification: Boolean = false,
        msgType: MsgType = MsgType.MSG_TYPE_TEXT,
    ): Result<Unit>

    /**
     * Starts sending an image. The returned [MediaUploadHandler] tracks the upload and must be awaited by the caller to know the outcome.
     *
     * @param file the image to upload.
     * @param thumbnailFile a pre-generated thumbnail, or `null` to send none.
     * @param imageInfo the dimensions, size and MIME type of the image.
     * @param caption the text shown along with the image, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

    /**
     * Starts sending a video, with the same upload contract as [sendImage].
     *
     * @param file the video to upload.
     * @param thumbnailFile a pre-generated thumbnail, or `null` to send none.
     * @param videoInfo the duration, dimensions, size and MIME type of the video.
     * @param caption the text shown along with the video, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

    /**
     * Starts sending an audio file, with the same upload contract as [sendImage]; use [sendVoiceMessage] for a recorded voice message.
     *
     * @param file the audio file to upload.
     * @param audioInfo the duration, size and MIME type of the audio.
     * @param caption the text shown along with the audio, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

    /**
     * Starts sending an arbitrary file, with the same upload contract as [sendImage].
     *
     * @param file the file to upload.
     * @param fileInfo the size and MIME type of the file.
     * @param caption the text shown along with the file, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

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
     * @param inReplyToEventId Optional [EventId] for the event this message should reply to.
     */
    suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String? = null,
        zoomLevel: Int? = null,
        assetType: AssetType? = null,
        inReplyToEventId: EventId?,
    ): Result<Unit>

    /**
     * Starts sending a recorded voice message, with the same upload contract as [sendImage].
     *
     * @param file the recorded audio to upload.
     * @param audioInfo the duration, size and MIME type of the recording.
     * @param waveform the amplitudes used to draw the waveform, normalised between 0 and 1.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

    /**
     * Starts sending several media items as a single gallery message, with the same upload contract as [sendImage].
     *
     * @param items the media making up the gallery, in the order they should appear.
     * @param caption the text shown along with the gallery, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendGallery(
        items: List<GalleryItemInfo>,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<MediaUploadHandler>

    /**
     * Redacts an event, i.e. asks the server to remove its content; it also removes a local echo that has not been sent yet.
     *
     * @param eventOrTransactionId the event to redact, identified by its event id once sent or by its transaction id while still local.
     * @param reason the explanation recorded in the redaction event, or `null` for none.
     */
    suspend fun redactEvent(eventOrTransactionId: EventOrTransactionId, reason: String?): Result<Unit>

    /**
     * Adds the given reaction to an event, or removes it if the current user had already reacted with it.
     *
     * @param emoji the reaction key, usually an emoji.
     * @param eventOrTransactionId the event to react to, identified by its event id or by its transaction id while still local.
     * @return whether the reaction is now present.
     */
    suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Boolean>

    /**
     * Forwards the content of an event to other rooms, as new messages sent by the current user.
     *
     * @param eventId the event whose content is forwarded.
     * @param roomIds the rooms to forward it to.
     */
    suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit>

    /**
     * Cancels an event that is still waiting in the send queue, by redacting its local echo.
     *
     * @param transactionId the transaction id of the local echo to drop.
     */
    suspend fun cancelSend(transactionId: TransactionId): Result<Unit> =
        redactEvent(transactionId.toEventOrTransactionId(), reason = null)

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

    /**
     * Resolves the content of a replied-to event so it can be rendered above the reply.
     * Served from the loaded timeline items when possible, otherwise fetched through the SDK.
     *
     * @param eventId the event being replied to.
     */
    suspend fun loadReplyDetails(eventId: EventId): InReplyTo

    /**
     * Returns true if [eventId] is currently loaded in this timeline's window, even if the display
     * layer does not render it (e.g. filtered or state events). This distinguishes "in the window
     * but not displayed" from "genuinely outside the loaded window".
     *
     * @param eventId the event to look for.
     */
    suspend fun isEventLoaded(eventId: EventId): Boolean

    /**
     * Adds a new pinned event by sending an updated `m.room.pinned_events`
     * event containing the new event id.
     *
     * Returns `true` if we sent the request, `false` if the event was already
     * pinned.
     *
     * @param eventId the event to pin.
     */
    suspend fun pinEvent(eventId: EventId): Result<Boolean>

    /**
     * Adds a new pinned event by sending an updated `m.room.pinned_events`
     * event without the event id we want to remove.
     *
     * Returns `true` if we sent the request, `false` if the event wasn't
     * pinned
     *
     * @param eventId the event to unpin.
     */
    suspend fun unpinEvent(eventId: EventId): Result<Boolean>

    /**
     * Get the latest event id of the timeline, or `null` when it holds no event yet.
     */
    suspend fun getLatestEventId(): Result<EventId?>
}
