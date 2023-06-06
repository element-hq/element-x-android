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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.VideoInfo

sealed interface EventContent

data class MessageContent(
    val body: String,
    val inReplyTo: InReplyTo?,
    val isEdited: Boolean,
    val type: MessageType?
) : EventContent


sealed interface InReplyTo {
    data class NotLoaded(val eventId: EventId) : InReplyTo
    data class Ready(
        val eventId: EventId,
        val content: MessageContent,
        val senderId: UserId,
        val senderDisplayName: String?,
        val senderAvatarUrl: String?,
    ) : InReplyTo

    object Error : InReplyTo
}

object RedactedContent : EventContent

data class StickerContent(
    val body: String,
    val info: ImageInfo,
    val url: String
) : EventContent

data class UnableToDecryptContent(
    val data: Data
) : EventContent {
    sealed interface Data {
        data class OlmV1Curve25519AesSha2(
            val senderKey: String
        ) : Data

        data class MegolmV1AesSha2(
            val sessionId: String
        ) : Data

        object Unknown : Data
    }
}

data class RoomMembershipContent(
    val userId: UserId,
    val change: MembershipChange?
) : EventContent

data class ProfileChangeContent(
    val displayName: String?,
    val prevDisplayName: String?,
    val avatarUrl: String?,
    val prevAvatarUrl: String?
) : EventContent

data class StateContent(
    val stateKey: String,
    val content: OtherState
) : EventContent

data class FailedToParseMessageLikeContent(
    val eventType: String,
    val error: String
) : EventContent

data class FailedToParseStateContent(
    val eventType: String,
    val stateKey: String,
    val error: String
) : EventContent

object UnknownContent : EventContent

sealed interface MessageType

object UnknownMessageType : MessageType

enum class MessageFormat {
    HTML, UNKNOWN
}

data class FormattedBody(
    val format: MessageFormat,
    val body: String
)

data class EmoteMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class ImageMessageType(
    val body: String,
    val source: MediaSource,
    val info: ImageInfo?
) : MessageType

data class AudioMessageType(
    val body: String,
    val source: MediaSource,
    val info: AudioInfo?
) : MessageType

data class VideoMessageType(
    val body: String,
    val source: MediaSource,
    val info: VideoInfo?
) : MessageType

data class FileMessageType(
    val body: String,
    val source: MediaSource,
    val info: FileInfo?
) : MessageType

data class NoticeMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

data class TextMessageType(
    val body: String,
    val formatted: FormattedBody?
) : MessageType

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
        val topic: String?
    ) : OtherState

    object SpaceChild : OtherState

    object SpaceParent : OtherState

    data class Custom(
        val eventType: String
    ) : OtherState
}
