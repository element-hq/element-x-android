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

package io.element.android.features.roomlist.impl

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
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
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aProfileChangeMessageContent
import io.element.android.libraries.matrix.test.room.aRoomMessage
import io.element.android.libraries.matrix.test.room.anEventTimelineItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class DefaultRoomLastMessageFormatterTests {

    private lateinit var context: Context
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var formatter: DefaultRoomLastMessageFormatter

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() as Context
        fakeMatrixClient = FakeMatrixClient()
        formatter = DefaultRoomLastMessageFormatter(context, fakeMatrixClient)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Redacted content`() {
        val expected = "Message removed"
        val senderName = "Someone"
        sequenceOf(false, true).forEach { isDm ->
            val message = createRoomMessage(false, senderName, RedactedContent)
            val result = formatter.processMessageItem(message, isDm)
            if (isDm) {
                Truth.assertThat(result).isEqualTo(expected)
            } else {
                Truth.assertThat(result).isInstanceOf(AnnotatedString::class.java)
                Truth.assertThat(result.toString()).isEqualTo("$senderName: $expected")
            }
        }
    }

    @Test
    @Config(qualifiers = "en")
    fun `Sticker content`() {
        val body = "body"
        val info = ImageInfo(null, null, null, null, null, null, null)
        val message = createRoomMessage(false, null, StickerContent(body, info, "url"))
        val result = formatter.processMessageItem(message, false)
        Truth.assertThat(result).isEqualTo(body)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Unable to decrypt content`() {
        val expected = "Decryption error"
        val senderName = "Someone"
        sequenceOf(false, true).forEach { isDm ->
            val message = createRoomMessage(false, senderName, UnableToDecryptContent(UnableToDecryptContent.Data.Unknown))
            val result = formatter.processMessageItem(message, isDm)
            if (isDm) {
                Truth.assertThat(result).isEqualTo(expected)
            } else {
                Truth.assertThat(result).isInstanceOf(AnnotatedString::class.java)
                Truth.assertThat(result.toString()).isEqualTo("$senderName: $expected")
            }
        }
    }

    @Test
    @Config(qualifiers = "en")
    fun `FailedToParseMessageLike, FailedToParseState & Unknown content`() {
        val expected = "Unsupported event"
        val senderName = "Someone"
        sequenceOf(false, true).forEach { isDm ->
            sequenceOf(
                FailedToParseMessageLikeContent("", ""),
                FailedToParseStateContent("", "", ""),
                UnknownContent,
            ).forEach { type ->
                val message = createRoomMessage(false, senderName, type)
                val result = formatter.processMessageItem(message, isDm)
                if (isDm) {
                    Truth.assertWithMessage("$type was not properly handled").that(result).isEqualTo(expected)
                } else {
                    Truth.assertWithMessage("$type does not create an AnnotatedString").that(result).isInstanceOf(AnnotatedString::class.java)
                    Truth.assertWithMessage("$type was not properly handled").that(result.toString()).isEqualTo("$senderName: $expected")
                }
            }
        }
    }

    // region Message contents

    @Test
    @Config(qualifiers = "en")
    fun `Message contents`() {
        val body = "Shared body"
        fun createMessageContent(type: MessageType): MessageContent {
            return MessageContent(body, null, false, type)
        }
        val sharedContentMessagesTypes = arrayOf(
            TextMessageType(body, null),
            VideoMessageType(body, "url", null),
            AudioMessageType(body, "url", null),
            ImageMessageType(body, "url", null),
            FileMessageType(body, "url", null),
            NoticeMessageType(body, null),
            EmoteMessageType(body, null),
        )
        val senderName = "Someone"
        val resultsInRoom = mutableListOf<Pair<MessageType, CharSequence?>>()
        val resultsInDm = mutableListOf<Pair<MessageType, CharSequence?>>()

        // Create messages for all types in DM and Room mode
        sequenceOf(false, true).forEach { isDm ->
            sharedContentMessagesTypes.forEach { type ->
                val content = createMessageContent(type)
                val message = createRoomMessage(sentByYou = false, senderDisplayName = "Someone", content = content)
                val result = formatter.processMessageItem(message, isDmRoom = isDm)
                if (isDm) {
                    resultsInDm.add(type to result)
                } else {
                    resultsInRoom.add(type to result)
                }
            }
            val unknownMessage = createRoomMessage(sentByYou = false, senderDisplayName = "Someone", content = createMessageContent(UnknownMessageType))
            val result = UnknownMessageType to formatter.processMessageItem(unknownMessage, isDmRoom = isDm)
            if (isDm) {
                resultsInDm.add(result)
            } else {
                resultsInRoom.add(result)
            }
        }

        // Verify results of DM mode
        for ((type, result) in resultsInDm) {
            val expectedResult = when (type) {
                is VideoMessageType -> "Video."
                is AudioMessageType -> "Audio"
                is ImageMessageType -> "Image."
                is FileMessageType -> "File"
                is EmoteMessageType -> "- $senderName ${type.body}"
                is TextMessageType, is NoticeMessageType -> body
                UnknownMessageType -> "Event type not handled by EAX"
            }
            Truth.assertWithMessage("$type was not properly handled").that(result).isEqualTo(expectedResult)
        }

        // Verify results of Room mode
        for ((type, result) in resultsInRoom) {
            val string = result.toString()
            val expectedResult = when (type) {
                is VideoMessageType -> "$senderName: Video."
                is AudioMessageType -> "$senderName: Audio"
                is ImageMessageType -> "$senderName: Image."
                is FileMessageType -> "$senderName: File"
                is EmoteMessageType -> "- $senderName ${type.body}"
                is TextMessageType, is NoticeMessageType -> "$senderName: $body"
                UnknownMessageType -> "$senderName: Event type not handled by EAX"
            }
            val shouldCreateAnnotatedString = when (type) {
                is VideoMessageType -> true
                is AudioMessageType -> true
                is ImageMessageType -> true
                is FileMessageType -> true
                is EmoteMessageType -> false
                is TextMessageType, is NoticeMessageType -> true
                UnknownMessageType -> true
            }
            if (shouldCreateAnnotatedString) {
                Truth.assertWithMessage("$type doesn't produce an AnnotatedString")
                    .that(result)
                    .isInstanceOf(AnnotatedString::class.java)
            }
            Truth.assertWithMessage("$type was not properly handled").that(string).isEqualTo(expectedResult)
        }
    }

    // endregion

    // region Membership change

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - joined`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.JOINED)

        val youJoinedRoomMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youJoinedRoom = formatter.processMessageItem(youJoinedRoomMessage, false)
        Truth.assertThat(youJoinedRoom).isEqualTo("You joined the room")

        val someoneJoinedRoomMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneJoinedRoom = formatter.processMessageItem(someoneJoinedRoomMessage, false)
        Truth.assertThat(someoneJoinedRoom).isEqualTo("${someoneContent.userId.value} joined the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - left`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.LEFT)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.LEFT)

        val youLeftRoomMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youLeftRoom = formatter.processMessageItem(youLeftRoomMessage, false)
        Truth.assertThat(youLeftRoom).isEqualTo("You left the room")

        val someoneLeftRoomMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneLeftRoom = formatter.processMessageItem(someoneLeftRoomMessage, false)
        Truth.assertThat(someoneLeftRoom).isEqualTo("${someoneContent.userId.value} left the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - banned`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.BANNED)
        val youKickedContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KICKED_AND_BANNED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.BANNED)
        val someoneKickedContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KICKED_AND_BANNED)

        val youBannedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youBanned = formatter.processMessageItem(youBannedMessage, false)
        Truth.assertThat(youBanned).isEqualTo("You banned ${youContent.userId.value}")

        val youKickBannedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youKickedContent)
        val youKickedBanned = formatter.processMessageItem(youKickBannedMessage, false)
        Truth.assertThat(youKickedBanned).isEqualTo("You banned ${youContent.userId.value}")

        val someoneBannedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneBanned = formatter.processMessageItem(someoneBannedMessage, false)
        Truth.assertThat(someoneBanned).isEqualTo("$otherName banned ${someoneContent.userId.value}")

        val someoneKickBannedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneKickedContent)
        val someoneKickBanned = formatter.processMessageItem(someoneKickBannedMessage, false)
        Truth.assertThat(someoneKickBanned).isEqualTo("$otherName banned ${someoneContent.userId.value}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - unban`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.UNBANNED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.UNBANNED)

        val youUnbannedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youUnbanned = formatter.processMessageItem(youUnbannedMessage, false)
        Truth.assertThat(youUnbanned).isEqualTo("You unbanned ${youContent.userId.value}")

        val someoneUnbannedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneUnbanned = formatter.processMessageItem(someoneUnbannedMessage, false)
        Truth.assertThat(someoneUnbanned).isEqualTo("$otherName unbanned ${someoneContent.userId.value}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - kicked`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KICKED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KICKED)

        val youKickedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKicked = formatter.processMessageItem(youKickedMessage, false)
        Truth.assertThat(youKicked).isEqualTo("You removed ${youContent.userId.value}")

        val someoneKickedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKicked = formatter.processMessageItem(someoneKickedMessage, false)
        Truth.assertThat(someoneKicked).isEqualTo("$otherName removed ${someoneContent.userId.value}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invited`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.INVITED)

        val youWereInvitedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val youWereInvited = formatter.processMessageItem(youWereInvitedMessage, false)
        Truth.assertThat(youWereInvited).isEqualTo("$otherName invited you")

        val youInvitedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youInvited = formatter.processMessageItem(youInvitedMessage, false)
        Truth.assertThat(youInvited).isEqualTo("You invited ${someoneContent.userId.value}")

        val someoneInvitedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneInvited = formatter.processMessageItem(someoneInvitedMessage, false)
        Truth.assertThat(someoneInvited).isEqualTo("$otherName invited ${someoneContent.userId.value}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation accepted`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITATION_ACCEPTED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.INVITATION_ACCEPTED)

        val youAcceptedInviteMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youAcceptedInvite = formatter.processMessageItem(youAcceptedInviteMessage, false)
        Truth.assertThat(youAcceptedInvite).isEqualTo("You accepted the invite")

        val someoneAcceptedInviteMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedInvite = formatter.processMessageItem(someoneAcceptedInviteMessage, false)
        Truth.assertThat(someoneAcceptedInvite).isEqualTo("${someoneContent.userId.value} accepted the invite")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation rejected`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITATION_REJECTED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.INVITATION_REJECTED)

        val youRejectedInviteMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRejectedInvite = formatter.processMessageItem(youRejectedInviteMessage, false)
        Truth.assertThat(youRejectedInvite).isEqualTo("You rejected the invitation")

        val someoneRejectedInviteMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRejectedInvite = formatter.processMessageItem(someoneRejectedInviteMessage, false)
        Truth.assertThat(someoneRejectedInvite).isEqualTo("${someoneContent.userId.value} rejected the invitation")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation revoked`() {
        val otherName = "Someone"
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.INVITATION_REVOKED)

        val youRevokedInviteMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youRevokedInvite = formatter.processMessageItem(youRevokedInviteMessage, false)
        Truth.assertThat(youRevokedInvite).isEqualTo("You revoked the invitation for ${someoneContent.userId.value} to join the room")

        val someoneRevokedInviteMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRevokedInvite = formatter.processMessageItem(someoneRevokedInviteMessage, false)
        Truth.assertThat(someoneRevokedInvite).isEqualTo("$otherName revoked the invitation for ${someoneContent.userId.value} to join the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knocked`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCKED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KNOCKED)

        val youKnockedMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKnocked = formatter.processMessageItem(youKnockedMessage, false)
        Truth.assertThat(youKnocked).isEqualTo("You requested to join")

        val someoneKnockedMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKnocked = formatter.processMessageItem(someoneKnockedMessage, false)
        Truth.assertThat(someoneKnocked).isEqualTo("${someoneContent.userId.value} requested to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock accepted`() {
        val otherName = "Someone"
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KNOCK_ACCEPTED)

        val youAcceptedKnockMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youAcceptedKnock = formatter.processMessageItem(youAcceptedKnockMessage, false)
        Truth.assertThat(youAcceptedKnock).isEqualTo("${someoneContent.userId.value} allowed you to join")

        val someoneAcceptedKnockMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedKnock = formatter.processMessageItem(someoneAcceptedKnockMessage, false)
        Truth.assertThat(someoneAcceptedKnock).isEqualTo("$otherName allowed ${someoneContent.userId.value} to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock retracted`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCK_RETRACTED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KNOCK_RETRACTED)

        val youRetractedKnockMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRetractedKnock = formatter.processMessageItem(youRetractedKnockMessage, false)
        Truth.assertThat(youRetractedKnock).isEqualTo("You cancelled your request to join")

        val someoneRetractedKnockMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRetractedKnock = formatter.processMessageItem(someoneRetractedKnockMessage, false)
        Truth.assertThat(someoneRetractedKnock).isEqualTo("${someoneContent.userId.value} is no longer interested in joining")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock denied`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCK_DENIED)
        val someoneContent = RoomMembershipContent(UserId("someone_else"), MembershipChange.KNOCK_DENIED)

        val youDeniedKnockMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youDeniedKnock = formatter.processMessageItem(youDeniedKnockMessage, false)
        Truth.assertThat(youDeniedKnock).isEqualTo("You rejected ${someoneContent.userId.value}'s request to join")

        val someoneDeniedKnockMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneDeniedKnock = formatter.processMessageItem(someoneDeniedKnockMessage, false)
        Truth.assertThat(someoneDeniedKnock).isEqualTo("$otherName rejected ${someoneContent.userId.value}'s request to join")

        val someoneDeniedYourKnockMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val someoneDeniedYourKnock = formatter.processMessageItem(someoneDeniedYourKnockMessage, false)
        Truth.assertThat(someoneDeniedYourKnock).isEqualTo("$otherName rejected your request to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - others`() {
        val otherChanges = arrayOf(MembershipChange.NONE, MembershipChange.ERROR, MembershipChange.NOT_IMPLEMENTED)

        val results = otherChanges.map { change ->
            val content = RoomMembershipContent(A_USER_ID, change)
            val message = createRoomMessage(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.processMessageItem(message, false)
            change to result
        }
        val expected = otherChanges.map { it to null }
        Truth.assertThat(results).isEqualTo(expected)
    }

    // endregion

    // region Room State

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - avatar`() {
        val otherName = "Someone"
        val changedContent = StateContent("", OtherState.RoomAvatar("new_avatar"))
        val removedContent = StateContent("", OtherState.RoomAvatar(null))

        val youChangedRoomAvatarMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomAvatar = formatter.processMessageItem(youChangedRoomAvatarMessage, false)
        Truth.assertThat(youChangedRoomAvatar).isEqualTo("You changed the room avatar")

        val someoneChangedRoomAvatarMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomAvatar = formatter.processMessageItem(someoneChangedRoomAvatarMessage, false)
        Truth.assertThat(someoneChangedRoomAvatar).isEqualTo("$otherName changed the room avatar")

        val youRemovedRoomAvatarMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomAvatar = formatter.processMessageItem(youRemovedRoomAvatarMessage, false)
        Truth.assertThat(youRemovedRoomAvatar).isEqualTo("You removed the room avatar")

        val someoneRemovedRoomAvatarMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomAvatar = formatter.processMessageItem(someoneRemovedRoomAvatarMessage, false)
        Truth.assertThat(someoneRemovedRoomAvatar).isEqualTo("$otherName removed the room avatar")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - create`() {
        val otherName = "Someone"
        val content = StateContent("", OtherState.RoomCreate)

        val youCreatedRoomMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.processMessageItem(youCreatedRoomMessage, false)
        Truth.assertThat(youCreatedRoom).isEqualTo("You created the room")

        val someoneCreatedRoomMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.processMessageItem(someoneCreatedRoomMessage, false)
        Truth.assertThat(someoneCreatedRoom).isEqualTo("$otherName created the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - encryption`() {
        val otherName = "Someone"
        val content = StateContent("", OtherState.RoomEncryption)

        val youCreatedRoomMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.processMessageItem(youCreatedRoomMessage, false)
        Truth.assertThat(youCreatedRoom).isEqualTo("Encryption enabled")

        val someoneCreatedRoomMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.processMessageItem(someoneCreatedRoomMessage, false)
        Truth.assertThat(someoneCreatedRoom).isEqualTo("Encryption enabled")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room name`() {
        val otherName = "Someone"
        val newName = "New name"
        val changedContent = StateContent("", OtherState.RoomName(newName))
        val removedContent = StateContent("", OtherState.RoomName(null))

        val youChangedRoomNameMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomName = formatter.processMessageItem(youChangedRoomNameMessage, false)
        Truth.assertThat(youChangedRoomName).isEqualTo("You changed the room name to: $newName")

        val someoneChangedRoomNameMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomName = formatter.processMessageItem(someoneChangedRoomNameMessage, false)
        Truth.assertThat(someoneChangedRoomName).isEqualTo("$otherName changed the room name to: $newName")

        val youRemovedRoomNameMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomName = formatter.processMessageItem(youRemovedRoomNameMessage, false)
        Truth.assertThat(youRemovedRoomName).isEqualTo("You removed the room name")

        val someoneRemovedRoomNameMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomName = formatter.processMessageItem(someoneRemovedRoomNameMessage, false)
        Truth.assertThat(someoneRemovedRoomName).isEqualTo("$otherName removed the room name")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - third party invite`() {
        val otherName = "Someone"
        val inviteeName = "Alice"
        val changedContent = StateContent("", OtherState.RoomThirdPartyInvite(inviteeName))
        val removedContent = StateContent("", OtherState.RoomThirdPartyInvite(null))

        val youInvitedSomeoneMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youInvitedSomeone = formatter.processMessageItem(youInvitedSomeoneMessage, false)
        Truth.assertThat(youInvitedSomeone).isEqualTo("You sent an invitation to $inviteeName to join the room")

        val someoneInvitedSomeoneMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneInvitedSomeone = formatter.processMessageItem(someoneInvitedSomeoneMessage, false)
        Truth.assertThat(someoneInvitedSomeone).isEqualTo("$otherName sent an invitation to $inviteeName to join the room")

        val youInvitedNoOneMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youInvitedNoOne = formatter.processMessageItem(youInvitedNoOneMessage, false)
        Truth.assertThat(youInvitedNoOne).isNull()

        val someoneInvitedNoOneMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneInvitedNoOne = formatter.processMessageItem(someoneInvitedNoOneMessage, false)
        Truth.assertThat(someoneInvitedNoOne).isNull()
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room topic`() {
        val otherName = "Someone"
        val roomTopic = "New topic"
        val changedContent = StateContent("", OtherState.RoomTopic(roomTopic))
        val removedContent = StateContent("", OtherState.RoomTopic(null))

        val youChangedRoomTopicMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomTopic = formatter.processMessageItem(youChangedRoomTopicMessage, false)
        Truth.assertThat(youChangedRoomTopic).isEqualTo("You changed the topic to: $roomTopic")

        val someoneChangedRoomTopicMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomTopic = formatter.processMessageItem(someoneChangedRoomTopicMessage, false)
        Truth.assertThat(someoneChangedRoomTopic).isEqualTo("$otherName changed the topic to: $roomTopic")

        val youRemovedRoomTopicMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomTopic = formatter.processMessageItem(youRemovedRoomTopicMessage, false)
        Truth.assertThat(youRemovedRoomTopic).isEqualTo("You removed the room topic")

        val someoneRemovedRoomTopicMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomTopic = formatter.processMessageItem(someoneRemovedRoomTopicMessage, false)
        Truth.assertThat(someoneRemovedRoomTopic).isEqualTo("$otherName removed the room topic")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - others must return null`() {
        val otherStates = arrayOf(
            OtherState.PolicyRuleRoom, OtherState.PolicyRuleServer, OtherState.PolicyRuleUser, OtherState.RoomAliases, OtherState.RoomCanonicalAlias,
            OtherState.RoomGuestAccess, OtherState.RoomHistoryVisibility, OtherState.RoomJoinRules, OtherState.RoomPinnedEvents, OtherState.RoomPowerLevels,
            OtherState.RoomServerAcl, OtherState.RoomTombstone, OtherState.SpaceChild, OtherState.SpaceParent, OtherState.Custom("custom_event_type")
        )

        val results = otherStates.map { state ->
            val content = StateContent("", state)
            val message = createRoomMessage(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.processMessageItem(message, false)
            state to result
        }
        val expected = otherStates.map { it to null }
        Truth.assertThat(results).isEqualTo(expected)
    }

    // endregion

    // region Profile change

    @Test
    @Config(qualifiers = "en")
    fun `Profile change - avatar`() {
        val otherName = "Someone"
        val changedContent = aProfileChangeMessageContent(avatarUrl = "new_avatar_url", prevAvatarUrl = "old_avatar_url")
        val setContent = aProfileChangeMessageContent(avatarUrl = "new_avatar_url", prevAvatarUrl = null)
        val removedContent = aProfileChangeMessageContent(avatarUrl = null, prevAvatarUrl = "old_avatar_url")
        val invalidContent = aProfileChangeMessageContent(avatarUrl = null, prevAvatarUrl = null)
        val sameContent = aProfileChangeMessageContent(avatarUrl = "same_avatar_url", prevAvatarUrl = "same_avatar_url")

        val youChangedAvatarMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedAvatar = formatter.processMessageItem(youChangedAvatarMessage, false)
        Truth.assertThat(youChangedAvatar).isEqualTo("You changed your avatar")

        val someoneChangeAvatarMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangeAvatar = formatter.processMessageItem(someoneChangeAvatarMessage, false)
        Truth.assertThat(someoneChangeAvatar).isEqualTo("$otherName changed their avatar")

        val youSetAvatarMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetAvatar = formatter.processMessageItem(youSetAvatarMessage, false)
        Truth.assertThat(youSetAvatar).isEqualTo("You changed your avatar")

        val someoneSetAvatarMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetAvatar = formatter.processMessageItem(someoneSetAvatarMessage, false)
        Truth.assertThat(someoneSetAvatar).isEqualTo("$otherName changed their avatar")

        val youRemovedAvatarMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedAvatar = formatter.processMessageItem(youRemovedAvatarMessage, false)
        Truth.assertThat(youRemovedAvatar).isEqualTo("You changed your avatar")

        val someoneRemovedAvatarMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedAvatar = formatter.processMessageItem(someoneRemovedAvatarMessage, false)
        Truth.assertThat(someoneRemovedAvatar).isEqualTo("$otherName changed their avatar")

        val unchangedMessage = createRoomMessage(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.processMessageItem(unchangedMessage, false)
        Truth.assertThat(unchangedResult).isNull()

        val invalidMessage = createRoomMessage(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.processMessageItem(invalidMessage, false)
        Truth.assertThat(invalidResult).isNull()
    }

    @Test
    @Config(qualifiers = "en")
    fun `Profile change - display name`() {
        val newDisplayName = "New"
        val oldDisplayName = "Old"
        val otherName = "Someone"
        val changedContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = oldDisplayName)
        val setContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = null)
        val removedContent = aProfileChangeMessageContent(displayName = null, prevDisplayName = oldDisplayName)
        val sameContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = newDisplayName)
        val invalidContent = aProfileChangeMessageContent(displayName = null, prevDisplayName = null)

        val youChangedDisplayNameMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedDisplayName = formatter.processMessageItem(youChangedDisplayNameMessage, false)
        Truth.assertThat(youChangedDisplayName).isEqualTo("You changed your display name from $oldDisplayName to $newDisplayName")

        val someoneChangedDisplayNameMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedDisplayName = formatter.processMessageItem(someoneChangedDisplayNameMessage, false)
        Truth.assertThat(someoneChangedDisplayName).isEqualTo("$otherName changed their display name from $oldDisplayName to $newDisplayName")

        val youSetDisplayNameMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetDisplayName = formatter.processMessageItem(youSetDisplayNameMessage, false)
        Truth.assertThat(youSetDisplayName).isEqualTo("You set your display name to $newDisplayName")

        val someoneSetDisplayNameMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetDisplayName = formatter.processMessageItem(someoneSetDisplayNameMessage, false)
        Truth.assertThat(someoneSetDisplayName).isEqualTo("$otherName set their display name to $newDisplayName")

        val youRemovedDisplayNameMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedDisplayName = formatter.processMessageItem(youRemovedDisplayNameMessage, false)
        Truth.assertThat(youRemovedDisplayName).isEqualTo("You removed your display name (it was $oldDisplayName)")

        val someoneRemovedDisplayNameMessage = createRoomMessage(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedDisplayName = formatter.processMessageItem(someoneRemovedDisplayNameMessage, false)
        Truth.assertThat(someoneRemovedDisplayName).isEqualTo("$otherName removed their display name (it was $oldDisplayName)")

        val unchangedMessage = createRoomMessage(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.processMessageItem(unchangedMessage, false)
        Truth.assertThat(unchangedResult).isNull()

        val invalidMessage = createRoomMessage(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.processMessageItem(invalidMessage, false)
        Truth.assertThat(invalidResult).isNull()
    }

    @Test
    @Config(qualifiers = "en")
    fun `Profile change - display name & avatar`() {
        val newDisplayName = "New"
        val oldDisplayName = "Old"
        val changedContent = aProfileChangeMessageContent(
            displayName = newDisplayName,
            prevDisplayName = oldDisplayName,
            avatarUrl = "new_avatar_url",
            prevAvatarUrl = "old_avatar_url",
        )
        val invalidContent = aProfileChangeMessageContent(
            displayName = null,
            prevDisplayName = null,
            avatarUrl = null,
            prevAvatarUrl = null,
        )
        val sameContent = aProfileChangeMessageContent(
            displayName = newDisplayName,
            prevDisplayName = newDisplayName,
            avatarUrl = "same_avatar_url",
            prevAvatarUrl = "same_avatar_url",
        )

        val youChangedBothMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedBoth = formatter.processMessageItem(youChangedBothMessage, false)
        Truth.assertThat(youChangedBoth).isEqualTo("You changed your display name from $oldDisplayName to $newDisplayName\n(avatar was changed too)")

        val invalidContentMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = invalidContent)
        val invalidMessage = formatter.processMessageItem(invalidContentMessage, false)
        Truth.assertThat(invalidMessage).isNull()

        val sameContentMessage = createRoomMessage(sentByYou = true, senderDisplayName = null, content = sameContent)
        val sameMessage = formatter.processMessageItem(sameContentMessage, false)
        Truth.assertThat(sameMessage).isNull()
    }

    // endregion

    private fun createRoomMessage(sentByYou: Boolean, senderDisplayName: String?, content: EventContent): RoomMessage {
        val sender = if (sentByYou) A_USER_ID else UserId("someone_else")
        val profile = ProfileTimelineDetails.Ready(senderDisplayName, false, null)
        val event = anEventTimelineItem(content = content, senderProfile = profile, sender = sender)
        return aRoomMessage(event = event)
    }
}
