/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline

/**
 * Creates a [MediaSender] for the room of the current room scope; see [MediaSenderRoomFactory] when the room is not in scope.
 */
fun interface MediaSenderFactory {
    /**
     * Create a [MediaSender] for the given [Timeline.Mode], in the Room Scope.
     *
     * @param timelineMode the timeline the media should be sent to, which matters for threads in particular.
     */
    fun create(
        timelineMode: Timeline.Mode,
    ): MediaSender
}

/**
 * Creates a [MediaSender] for an explicitly given room, used outside the room scope, for instance when sharing into a room.
 */
fun interface MediaSenderRoomFactory {
    /**
     * Create a [MediaSender] for the given [JoinedRoom], with timeline mode Live.
     *
     * @param room the room the media should be sent to.
     */
    fun create(
        room: JoinedRoom,
    ): MediaSender
}

/**
 * Sends media to a room, taking care of the pre-processing the timeline API does not do: resizing, transcoding and stripping metadata.
 *
 * Sending is a two-step process, and the steps can be used separately so the UI can show a preview between them.
 * The temporary files produced by pre-processing are only released by [cleanUp].
 */
interface MediaSender {
    /**
     * Resizes, transcodes and strips the metadata of a media file without sending anything yet.
     *
     * @param uri the media picked by the user.
     * @param mimeType the MIME type of that media.
     * @param mediaOptimizationConfig how aggressively the media should be compressed.
     */
    suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo>

    /**
     * Sends media that has already gone through [preProcessMedia].
     *
     * @param mediaUploadInfo the result of the pre-processing step.
     * @param caption the text shown along with the media, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit>

    /**
     * Pre-processes and sends a media file in one go, suspending until the upload finishes.
     *
     * @param uri the media picked by the user.
     * @param mimeType the MIME type of that media.
     * @param caption the text shown along with the media, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     * @param mediaOptimizationConfig how aggressively the media should be compressed.
     */
    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String? = null,
        formattedCaption: String? = null,
        inReplyToEventId: EventId? = null,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<Unit>

    /**
     * Sends a recorded voice message, which is never compressed and carries a waveform instead of a caption.
     *
     * @param uri the recorded audio file.
     * @param mimeType the MIME type of that recording.
     * @param waveForm the amplitudes used to draw the waveform.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        inReplyToEventId: EventId? = null,
    ): Result<Unit>

    /**
     * Sends several already pre-processed media items as a single gallery message.
     *
     * @param mediaUploadInfos the pre-processed items, in the order they should appear.
     * @param caption the text shown along with the gallery, or `null` for none.
     * @param formattedCaption the caption as HTML, or `null` to send it unformatted.
     * @param inReplyToEventId the event this message replies to, or `null` if it is not a reply.
     */
    suspend fun sendGallery(
        mediaUploadInfos: List<MediaUploadInfo>,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit>

    /**
     * Aborts the uploads started by this sender that are still in flight.
     *
     * Only cancelling the coroutine that awaits an upload leaves it running, so that closing a screen does not abort a send in progress.
     * This is the explicit counterpart, for when the user asks for the send to stop.
     */
    fun cancelOngoingUploads()

    /** Deletes the temporary files produced by pre-processing; must be called even when nothing was ultimately sent. */
    fun cleanUp()
}
