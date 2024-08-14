/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TimelineItemEventContent {
    val type: String
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
