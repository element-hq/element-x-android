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
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind

sealed interface EventContent

data class MessageContent(
    val body: String,
    val inReplyTo: InReplyTo?,
    val isEdited: Boolean,
    val isThreaded: Boolean,
    val type: MessageType?
) : EventContent

sealed interface InReplyTo {
    /** The event details are not loaded yet. We can fetch them. */
    data class NotLoaded(val eventId: EventId) : InReplyTo

    /** The event details are pending to be fetched. We should **not** fetch them again. */
    data object Pending : InReplyTo

    /** The event details are available. */
    data class Ready(
        val eventId: EventId,
        val content: EventContent,
        val senderId: UserId,
        val senderDisplayName: String?,
        val senderAvatarUrl: String?,
    ) : InReplyTo

    /**
     * Fetching the event details failed.
     *
     * We can try to fetch them again **with a proper retry strategy**, but not blindly:
     *
     * If the reason for the failure is consistent on the server, we'd enter a loop
     * where we keep trying to fetch the same event.
     * */
    data object Error : InReplyTo
}

data object RedactedContent : EventContent

data class StickerContent(
    val body: String,
    val info: ImageInfo,
    val url: String
) : EventContent

data class PollContent(
    val question: String,
    val kind: PollKind,
    val maxSelections: ULong,
    val answers: List<PollAnswer>,
    val votes: Map<String, List<UserId>>,
    val endTime: ULong?
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

        data object Unknown : Data
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

data object UnknownContent : EventContent

sealed interface MessageType

data object UnknownMessageType : MessageType

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

data class LocationMessageType(
    val body: String,
    val geoUri: String,
    val description: String?,
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
    data object PolicyRuleRoom : OtherState
    data object PolicyRuleServer : OtherState
    data object PolicyRuleUser : OtherState
    data object RoomAliases : OtherState
    data class RoomAvatar(val url: String?) : OtherState
    data object RoomCanonicalAlias : OtherState
    data object RoomCreate : OtherState
    data object RoomEncryption : OtherState
    data object RoomGuestAccess : OtherState
    data object RoomHistoryVisibility : OtherState
    data object RoomJoinRules : OtherState
    data class RoomName(val name: String?) : OtherState
    data object RoomPinnedEvents : OtherState
    data object RoomPowerLevels : OtherState
    data object RoomServerAcl : OtherState
    data class RoomThirdPartyInvite(val displayName: String?) : OtherState
    data object RoomTombstone : OtherState
    data class RoomTopic(val topic: String?) : OtherState
    data object SpaceChild : OtherState
    data object SpaceParent : OtherState
    data class Custom(val eventType: String) : OtherState
}
