/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.impl.mode.RenderingMode
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.messages.toPlainText
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider

@ContributesBinding(SessionScope::class)
class DefaultRoomLatestEventFormatter(
    private val sp: StringProvider,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
    private val permalinkParser: PermalinkParser,
) : RoomLatestEventFormatter {
    companion object {
        // Max characters to display in the last message. This works around https://github.com/element-hq/element-x-android/issues/2105
        private const val MAX_SAFE_LENGTH = 500
    }

    override fun format(latestEvent: LatestEventValue, isDmRoom: Boolean): CharSequence? {
        return when (latestEvent) {
            LatestEventValue.None -> null
            is LatestEventValue.Local -> formatContent(
                content = latestEvent.content,
                isDmRoom = isDmRoom,
                isOutgoing = true,
                senderId = latestEvent.senderId,
                senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
            )
            is LatestEventValue.Remote -> formatContent(
                content = latestEvent.content,
                isDmRoom = isDmRoom,
                isOutgoing = latestEvent.isOwn,
                senderId = latestEvent.senderId,
                senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
            )
        }
    }

    private fun formatContent(
        content: EventContent,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
        senderId: UserId,
        senderDisambiguatedDisplayName: String
    ): CharSequence? {
        return when (content) {
            is MessageContent -> content.process(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            RedactedContent -> {
                val message = sp.getString(CommonStrings.common_message_removed)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is StickerContent -> {
                val message = sp.getString(CommonStrings.common_sticker) + " (" + content.bestDescription + ")"
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is UnableToDecryptContent -> {
                val message = sp.getString(CommonStrings.common_waiting_for_decryption_key)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, senderId, senderDisambiguatedDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing, RenderingMode.RoomList)
            }
            is PollContent -> {
                val message = sp.getString(CommonStrings.common_poll_summary, content.question)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is FailedToParseMessageLikeContent, is FailedToParseStateContent, is UnknownContent -> {
                val message = sp.getString(CommonStrings.common_unsupported_event)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is LegacyCallInviteContent -> sp.getString(CommonStrings.common_unsupported_call)
            is CallNotifyContent -> sp.getString(CommonStrings.common_call_started)
        }?.take(MAX_SAFE_LENGTH)
    }

    private fun MessageContent.process(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean
    ): CharSequence {
        val message = when (val messageType: MessageType = type) {
            // Doesn't need a prefix
            is EmoteMessageType -> {
                return "* $senderDisambiguatedDisplayName ${messageType.body}"
            }
            is TextMessageType -> {
                messageType.toPlainText(permalinkParser)
            }
            is VideoMessageType -> {
                messageType.bestDescription.prefixWith(sp.getString(CommonStrings.common_video))
            }
            is ImageMessageType -> {
                messageType.bestDescription.prefixWith(sp.getString(CommonStrings.common_image))
            }
            is StickerMessageType -> {
                messageType.bestDescription.prefixWith(sp.getString(CommonStrings.common_sticker))
            }
            is LocationMessageType -> {
                sp.getString(CommonStrings.common_shared_location)
            }
            is FileMessageType -> {
                messageType.bestDescription.prefixWith(sp.getString(CommonStrings.common_file))
            }
            is AudioMessageType -> {
                messageType.bestDescription.prefixWith(sp.getString(CommonStrings.common_audio))
            }
            is VoiceMessageType -> {
                // In this case, do not use bestDescription, because the filename is useless, only use the caption if available.
                messageType.caption?.prefixWith(sp.getString(CommonStrings.common_voice_message))
                    ?: sp.getString(CommonStrings.common_voice_message)
            }
            is OtherMessageType -> {
                messageType.body
            }
            is NoticeMessageType -> {
                messageType.body
            }
        }
        return message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
    }

    private fun CharSequence.prefixIfNeeded(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
    ): CharSequence = if (isDmRoom) {
        this
    } else {
        prefixWith(
            if (isOutgoing) {
                sp.getString(CommonStrings.common_you)
            } else {
                senderDisambiguatedDisplayName
            }
        )
    }
}
