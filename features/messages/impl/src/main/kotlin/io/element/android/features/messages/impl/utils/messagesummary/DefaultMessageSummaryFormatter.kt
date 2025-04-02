/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils.messagesummary

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.core.extensions.toSafeLength
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.ui.strings.CommonStrings
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultMessageSummaryFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
) : MessageSummaryFormatter {
    override fun format(event: TimelineItem.Event): String {
        return when (event.content) {
            is TimelineItemTextBasedContent -> event.content.plainText
            is TimelineItemProfileChangeContent -> event.content.body
            is TimelineItemStateContent -> event.content.body
            is TimelineItemLocationContent -> context.getString(CommonStrings.common_shared_location)
            is TimelineItemEncryptedContent -> context.getString(CommonStrings.common_unable_to_decrypt)
            is TimelineItemRedactedContent -> context.getString(CommonStrings.common_message_removed)
            is TimelineItemPollContent -> event.content.question
            is TimelineItemVoiceContent -> context.getString(CommonStrings.common_voice_message)
            is TimelineItemUnknownContent -> context.getString(CommonStrings.common_unsupported_event)
            is TimelineItemImageContent -> context.getString(CommonStrings.common_image)
            is TimelineItemStickerContent -> context.getString(CommonStrings.common_sticker)
            is TimelineItemVideoContent -> context.getString(CommonStrings.common_video)
            is TimelineItemFileContent -> context.getString(CommonStrings.common_file)
            is TimelineItemAudioContent -> context.getString(CommonStrings.common_audio)
            is TimelineItemLegacyCallInviteContent -> context.getString(CommonStrings.common_unsupported_call)
            is TimelineItemCallNotifyContent -> context.getString(CommonStrings.common_call_started)
        }
            // Truncate the message to a safe length to avoid crashes in Compose
            .toSafeLength()
    }
}
