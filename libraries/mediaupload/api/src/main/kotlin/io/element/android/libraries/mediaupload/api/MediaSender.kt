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

fun interface MediaSenderFactory {
    /**
     * Create a [MediaSender] for the given [Timeline.Mode], in the Room Scope.
     */
    fun create(
        timelineMode: Timeline.Mode,
    ): MediaSender
}

fun interface MediaSenderRoomFactory {
    /**
     * Create a [MediaSender] for the given [JoinedRoom], with timeline mode Live.
     */
    fun create(
        room: JoinedRoom,
    ): MediaSender
}

interface MediaSender {
    suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo>

    suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit>

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String? = null,
        formattedCaption: String? = null,
        inReplyToEventId: EventId? = null,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<Unit>

    suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        inReplyToEventId: EventId? = null,
    ): Result<Unit>

    fun cleanUp()
}
