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
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.ui.strings.CommonStrings
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultMessageSummaryFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
) : MessageSummaryFormatter {
    companion object {
        // Max characters to display in the summary message. This works around https://github.com/element-hq/element-x-android/issues/2105
        private const val MAX_SAFE_LENGTH = 500
    }

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
            is TimelineItemLegacyCallInviteContent -> context.getString(CommonStrings.common_call_invite)
            is TimelineItemCallNotifyContent -> context.getString(CommonStrings.common_call_started)
        }.take(MAX_SAFE_LENGTH)
    }
}
