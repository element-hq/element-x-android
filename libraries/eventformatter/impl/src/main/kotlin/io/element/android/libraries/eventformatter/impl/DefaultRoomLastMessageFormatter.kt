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

package io.element.android.libraries.eventformatter.impl

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.eventformatter.impl.isme.IsMe
import io.element.android.libraries.eventformatter.impl.mode.RenderingMode
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject
import io.element.android.libraries.ui.strings.R as StringR

@ContributesBinding(SessionScope::class)
class DefaultRoomLastMessageFormatter @Inject constructor(
    private val sp: StringProvider,
    private val isMe: IsMe,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
) : RoomLastMessageFormatter {

    override fun format(event: EventTimelineItem, isDmRoom: Boolean): CharSequence? {
        val isOutgoing = isMe(event.sender)
        val senderDisplayName = (event.senderProfile as? ProfileTimelineDetails.Ready)?.displayName ?: event.sender.value
        return when (val content = event.content) {
            is MessageContent -> processMessageContents(content, senderDisplayName, isDmRoom)
            RedactedContent -> {
                val message = sp.getString(StringR.string.common_message_removed)
                if (!isDmRoom) {
                    prefix(message, senderDisplayName)
                } else {
                    message
                }
            }
            is StickerContent -> {
                content.body
            }
            is UnableToDecryptContent -> {
                val message = sp.getString(StringR.string.common_decryption_error)
                if (!isDmRoom) {
                    prefix(message, senderDisplayName)
                } else {
                    message
                }
            }
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, senderDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisplayName, isOutgoing, RenderingMode.RoomList)
            }
            is FailedToParseMessageLikeContent, is FailedToParseStateContent, is UnknownContent -> {
                prefixIfNeeded(sp.getString(StringR.string.common_unsupported_event), senderDisplayName, isDmRoom)
            }
        }
    }

    private fun processMessageContents(messageContent: MessageContent, senderDisplayName: String, isDmRoom: Boolean): CharSequence? {
        val messageType: MessageType = messageContent.type ?: return null

        val internalMessage = when (messageType) {
            // Doesn't need a prefix
            is EmoteMessageType -> {
                return "- $senderDisplayName ${messageType.body}"
            }
            is TextMessageType -> {
                messageType.body
            }
            is VideoMessageType -> {
                sp.getString(StringR.string.common_video)
            }
            is ImageMessageType -> {
                sp.getString(StringR.string.common_image)
            }
            is FileMessageType -> {
                sp.getString(StringR.string.common_file)
            }
            is AudioMessageType -> {
                sp.getString(StringR.string.common_audio)
            }
            UnknownMessageType -> {
                sp.getString(StringR.string.common_unsupported_event)
            }
            is NoticeMessageType -> {
                messageType.body
            }
        }
        return prefixIfNeeded(internalMessage, senderDisplayName, isDmRoom)
    }

    private fun prefixIfNeeded(message: String, senderDisplayName: String, isDmRoom: Boolean): CharSequence = if (isDmRoom) {
        message
    } else {
        prefix(message, senderDisplayName)
    }

    private fun prefix(message: String, senderDisplayName: String): AnnotatedString {
        return buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(senderDisplayName)
            }
            append(": ")
            append(message)
        }
    }
}
