/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody

@Immutable
sealed interface TimelineItemEventContent {
    val type: String
}

@Immutable
sealed interface TimelineItemEventContentWithAttachment : TimelineItemEventContent {
    val filename: String
    val caption: String?
    val formattedCaption: FormattedBody?

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
        is TimelineItemCallNotifyContent,
        TimelineItemUnknownContent -> false
    }

/**
 * Whether the event content has been edited.
 */
fun TimelineItemEventContent.isEdited(): Boolean =
    when (this) {
        is TimelineItemTextBasedContent -> isEdited
        is TimelineItemPollContent -> isEdited
        else -> false
    }
