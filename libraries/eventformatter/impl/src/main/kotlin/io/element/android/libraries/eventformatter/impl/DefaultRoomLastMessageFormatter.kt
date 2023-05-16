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
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
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
import timber.log.Timber
import javax.inject.Inject
import io.element.android.libraries.ui.strings.R as StringR

@ContributesBinding(SessionScope::class)
class DefaultRoomLastMessageFormatter @Inject constructor(
    private val sp: StringProvider,
    private val matrixClient: MatrixClient,
) : RoomLastMessageFormatter {

    override fun format(event: EventTimelineItem, isDmRoom: Boolean): CharSequence? {
        val isOutgoing = event.sender == matrixClient.sessionId
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
                processRoomMembershipChange(content, senderDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                processProfileChangeContent(content, senderDisplayName, isOutgoing)
            }
            is StateContent -> {
                processRoomStateChange(content, senderDisplayName, isOutgoing)
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

    private fun processRoomMembershipChange(membershipContent: RoomMembershipContent, senderDisplayName: String, senderIsYou: Boolean): CharSequence? {
        val userId = membershipContent.userId
        val memberIsYou = userId == matrixClient.sessionId
        return when (val change = membershipContent.change) {
            MembershipChange.JOINED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_join_by_you)
            } else {
                sp.getString(R.string.state_event_room_join, userId.value)
            }
            MembershipChange.LEFT -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_leave_by_you)
            } else {
                sp.getString(R.string.state_event_room_leave, userId.value)
            }
            MembershipChange.BANNED, MembershipChange.KICKED_AND_BANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_ban_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_ban, senderDisplayName, userId.value)
            }
            MembershipChange.UNBANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_unban_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_unban, senderDisplayName, userId.value)
            }
            MembershipChange.KICKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_remove_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_remove, senderDisplayName, userId.value)
            }
            MembershipChange.INVITED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_invite_by_you, userId.value)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_you, senderDisplayName)
            } else {
                sp.getString(R.string.state_event_room_invite, senderDisplayName, userId.value)
            }
            MembershipChange.INVITATION_ACCEPTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_accepted_by_you)
            } else {
                sp.getString(R.string.state_event_room_invite_accepted, userId.value)
            }
            MembershipChange.INVITATION_REJECTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_reject_by_you)
            } else {
                sp.getString(R.string.state_event_room_reject, userId.value)
            }
            MembershipChange.INVITATION_REVOKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_third_party_revoked_invite_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_third_party_revoked_invite, senderDisplayName, userId.value)
            }
            MembershipChange.KNOCKED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock, userId.value)
            }
            MembershipChange.KNOCK_ACCEPTED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_accepted_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_knock_accepted, senderDisplayName, userId.value)
            }
            MembershipChange.KNOCK_RETRACTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_retracted_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock_retracted, userId.value)
            }
            MembershipChange.KNOCK_DENIED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_by_you, userId.value)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_you, senderDisplayName)
            } else {
                sp.getString(R.string.state_event_room_knock_denied, senderDisplayName, userId.value)
            }
            else -> {
                Timber.v("Filtering timeline item for room membership: $membershipContent")
                null
            }
        }
    }

    private fun processRoomStateChange(stateContent: StateContent, senderDisplayName: String, senderIsYou: Boolean): CharSequence? {
        return when (val content = stateContent.content) {
            is OtherState.RoomAvatar -> {
                val hasAvatarUrl = content.url != null
                when {
                    senderIsYou && hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_changed_by_you)
                    senderIsYou && !hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_removed_by_you)
                    !senderIsYou && hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_changed, senderDisplayName)
                    else -> sp.getString(R.string.state_event_room_avatar_removed, senderDisplayName)
                }
            }
            is OtherState.RoomCreate -> {
                if (senderIsYou) {
                    sp.getString(R.string.state_event_room_created_by_you)
                } else {
                    sp.getString(R.string.state_event_room_created, senderDisplayName)
                }
            }
            is OtherState.RoomEncryption -> sp.getString(StringR.string.common_encryption_enabled)
            is OtherState.RoomName -> {
                val hasRoomName = content.name != null
                when {
                    senderIsYou && hasRoomName -> sp.getString(R.string.state_event_room_name_changed_by_you, content.name)
                    senderIsYou && !hasRoomName -> sp.getString(R.string.state_event_room_name_removed_by_you)
                    !senderIsYou && hasRoomName -> sp.getString(R.string.state_event_room_name_changed, senderDisplayName, content.name)
                    else -> sp.getString(R.string.state_event_room_name_removed, senderDisplayName)
                }
            }
            is OtherState.RoomThirdPartyInvite -> {
                if (content.displayName == null) {
                    Timber.e("RoomThirdPartyInvite undisplayable due to missing name")
                    return null
                }
                if (senderIsYou) {
                    sp.getString(R.string.state_event_room_third_party_invite_by_you, content.displayName)
                } else {
                    sp.getString(R.string.state_event_room_third_party_invite, senderDisplayName, content.displayName)
                }
            }
            is OtherState.RoomTopic -> {
                val hasRoomTopic = content.topic != null
                when {
                    senderIsYou && hasRoomTopic -> sp.getString(R.string.state_event_room_topic_changed_by_you, content.topic)
                    senderIsYou && !hasRoomTopic -> sp.getString(R.string.state_event_room_topic_removed_by_you)
                    !senderIsYou && hasRoomTopic -> sp.getString(R.string.state_event_room_topic_changed, senderDisplayName, content.topic)
                    else -> sp.getString(R.string.state_event_room_topic_removed, senderDisplayName)
                }
            }
            else -> {
                Timber.v("Filtering timeline item for room state change: $content")
                null
            }
        }
    }

    private fun processProfileChangeContent(
        profileChangeContent: ProfileChangeContent,
        senderDisplayName: String,
        senderIsYou: Boolean
    ): String? = profileChangeContent.run {
        val displayNameChanged = displayName != prevDisplayName
        val avatarChanged = avatarUrl != prevAvatarUrl
        return when {
            avatarChanged && displayNameChanged -> {
                val message = processProfileChangeContent(profileChangeContent.copy(avatarUrl = null, prevAvatarUrl = null), senderDisplayName, senderIsYou)
                val avatarChangedToo = sp.getString(R.string.state_event_avatar_changed_too)
                "$message\n$avatarChangedToo"
            }
            displayNameChanged -> {
                if (displayName != null && prevDisplayName != null) {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_changed_from_by_you, prevDisplayName, displayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_changed_from, senderDisplayName, prevDisplayName, displayName)
                    }
                } else if (displayName != null) {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_set_by_you, displayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_set, senderDisplayName, displayName)
                    }
                } else {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_removed_by_you, prevDisplayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_removed, senderDisplayName, prevDisplayName)
                    }
                }
            }
            avatarChanged -> {
                if (senderIsYou) {
                    sp.getString(R.string.state_event_avatar_url_changed_by_you)
                } else {
                    sp.getString(R.string.state_event_avatar_url_changed, senderDisplayName)
                }
            }
            else -> null
        }
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
