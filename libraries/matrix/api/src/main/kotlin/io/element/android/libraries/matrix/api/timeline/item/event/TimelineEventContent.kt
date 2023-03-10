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

package io.element.android.libraries.matrix.api.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo

sealed interface TimelineEventContent

data class TimelineEventMessageContent(
    val body: String,
    val inReplyTo: UserId?,
    val isEdited: Boolean,
    val content: MessageContent?
) : TimelineEventContent

object RedactedContent : TimelineEventContent

data class StickerContent(
    val body: String,
    val info: ImageInfo,
    val url: String
) : TimelineEventContent

sealed interface EncryptedMessage {
    data class OlmV1Curve25519AesSha2(
        val senderKey: String
    ) : EncryptedMessage

    data class MegolmV1AesSha2(
        val sessionId: String
    ) : EncryptedMessage

    object Unknown : EncryptedMessage
}

data class UnableToDecryptContent(
    val message: EncryptedMessage
) : TimelineEventContent

data class RoomMembership(
    val userId: UserId,
    val change: MembershipChange?
) : TimelineEventContent

data class ProfileChange(
    val displayName: String?,
    val prevDisplayName: String?,
    val avatarUrl: String?,
    val prevAvatarUrl: String?
) : TimelineEventContent

data class State(
    val stateKey: String,
    val content: OtherState
) : TimelineEventContent

data class FailedToParseMessageLike(
    val eventType: String,
    val error: String
) : TimelineEventContent

data class FailedToParseState(
    val eventType: String,
    val stateKey: String,
    val error: String
) : TimelineEventContent

object UnknownContent : TimelineEventContent

sealed interface MessageContent

object UnknownMessageContent : MessageContent

enum class MessageFormat {
    HTML, UNKNOWN
}

data class FormattedBody(
    val format: MessageFormat,
    val body: String
)

data class EmoteMessageContent(
    val body: String,
    val formatted: FormattedBody?
) : MessageContent

data class ImageMessageContent(
    val body: String,
    val url: String,
    val info: ImageInfo?
) : MessageContent

data class AudioMessageContent(
    var body: String,
    var url: String,
    var info: AudioInfo?
) : MessageContent

data class VideoMessageContent(
    val body: String,
    val url: String,
    val info: VideoInfo?
) : MessageContent

data class FileMessageContent(
    val body: String,
    val url: String,
    val info: FileInfo?
) : MessageContent

data class NoticeMessageContent(
    val body: String,
    val formatted: FormattedBody?
) : MessageContent

data class TextMessageContent(
    val body: String,
    val formatted: FormattedBody?
) : MessageContent

enum class MembershipChange {
    NONE,
    ERROR,
    JOINED,
    LEFT,
    BANNED,
    UNBANNED,
    KICKED,
    INVITED,
    KICKED_AND_BANNED,
    INVITATION_ACCEPTED,
    INVITATION_REJECTED,
    INVITATION_REVOKED,
    KNOCKED,
    KNOCK_ACCEPTED,
    KNOCK_RETRACTED,
    KNOCK_DENIED,
    NOT_IMPLEMENTED;
}

sealed interface OtherState {
    object PolicyRuleRoom : OtherState

    object PolicyRuleServer : OtherState

    object PolicyRuleUser : OtherState

    object RoomAliases : OtherState

    data class RoomAvatar(
        val url: String?
    ) : OtherState

    object RoomCanonicalAlias : OtherState

    object RoomCreate : OtherState

    object RoomEncryption : OtherState

    object RoomGuestAccess : OtherState

    object RoomHistoryVisibility : OtherState

    object RoomJoinRules : OtherState

    data class RoomName(
        val name: String?
    ) : OtherState

    object RoomPinnedEvents : OtherState

    object RoomPowerLevels : OtherState

    object RoomServerAcl : OtherState

    data class RoomThirdPartyInvite(
        val displayName: String?
    ) : OtherState

    object RoomTombstone : OtherState

    data class RoomTopic(
        val `topic`: String?
    ) : OtherState

    object SpaceChild : OtherState

    object SpaceParent : OtherState

    data class Custom(
        val eventType: String
    ) : OtherState
}
