/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlin.time.Duration

@Immutable
sealed interface TimelineItemEventContent {
    val type: String
}

interface TimelineItemEventMutableContent {
    /** Whether the event has been edited. */
    val isEdited: Boolean
}

@Immutable
sealed interface TimelineItemEventContentWithAttachment :
    TimelineItemEventContent,
    TimelineItemEventMutableContent {
    val filename: String
    val fileSize: Long?
    val caption: String?
    val formattedCaption: CharSequence?
    val mediaSource: MediaSource
    val mimeType: String
    val formattedFileSize: String
    val fileExtension: String

    val bestDescription: String
        get() = caption ?: filename
}

/**
 * Only text based content can be copied.
 */
fun TimelineItemEventContent.canBeCopied(): Boolean =
    this is TimelineItemTextBasedContent

/**
 * Returns true if the event content can be forwarded.
 */
fun TimelineItemEventContent.canBeForwarded(): Boolean =
    when (this) {
        is TimelineItemTextBasedContent,
        is TimelineItemImageContent,
        is TimelineItemFileContent,
        is TimelineItemAudioContent,
        is TimelineItemVideoContent,
        is TimelineItemLocationContent,
        is TimelineItemVoiceContent -> true
        // Stickers can't be forwarded (yet) so we don't show the option
        // See https://github.com/element-hq/element-x-android/issues/2161
        is TimelineItemStickerContent -> false
        else -> false
    }

/**
 * Return true if user can react (i.e. send a reaction) on the event content.
 * This does not take into account the power level of the user.
 */
fun TimelineItemEventContent.canReact(): Boolean =
    when (this) {
        is TimelineItemTextBasedContent,
        is TimelineItemAudioContent,
        is TimelineItemEncryptedContent,
        is TimelineItemFileContent,
        is TimelineItemImageContent,
        is TimelineItemStickerContent,
        is TimelineItemLocationContent,
        is TimelineItemPollContent,
        is TimelineItemVoiceContent,
        is TimelineItemVideoContent -> true
        is TimelineItemStateContent,
        is TimelineItemRedactedContent,
        is TimelineItemLegacyCallInviteContent,
        is TimelineItemRtcNotificationContent,
        TimelineItemUnknownContent -> false
    }

/**
 * Whether the event content has been edited.
 */
fun TimelineItemEventContent.isEdited(): Boolean = when (this) {
    is TimelineItemEventMutableContent -> isEdited
    else -> false
}

/**
 * Whether the event content has been redacted.
 */
fun TimelineItemEventContent.isRedacted(): Boolean = this is TimelineItemRedactedContent

fun TimelineItemEventContentWithAttachment.duration(): Duration? {
    return when (this) {
        is TimelineItemAudioContent -> duration
        is TimelineItemVideoContent -> duration
        is TimelineItemVoiceContent -> duration
        else -> null
    }
}
