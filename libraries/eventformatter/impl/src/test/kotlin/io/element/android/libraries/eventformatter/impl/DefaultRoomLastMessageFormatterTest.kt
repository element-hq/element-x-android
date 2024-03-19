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

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
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
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.aProfileChangeMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class DefaultRoomLastMessageFormatterTest {
    private lateinit var context: Context
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var formatter: DefaultRoomLastMessageFormatter

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() as Context
        fakeMatrixClient = FakeMatrixClient()
        val stringProvider = AndroidStringProvider(context.resources)
        formatter = DefaultRoomLastMessageFormatter(
            sp = AndroidStringProvider(context.resources),
            roomMembershipContentFormatter = RoomMembershipContentFormatter(fakeMatrixClient, stringProvider),
            profileChangeContentFormatter = ProfileChangeContentFormatter(stringProvider),
            stateContentFormatter = StateContentFormatter(stringProvider)
        )
    }

    @Test
    @Config(qualifiers = "en")
    fun `Redacted content`() {
        val expected = "Message removed"
        val senderName = "Someone"
        sequenceOf(false, true).forEach { isDm ->
            val message = createRoomEvent(false, senderName, RedactedContent)
            val result = formatter.format(message, isDm)
            if (isDm) {
                assertThat(result).isEqualTo(expected)
            } else {
                assertThat(result).isInstanceOf(AnnotatedString::class.java)
                assertThat(result.toString()).isEqualTo("$senderName: $expected")
            }
        }
    }

    @Test
    @Config(qualifiers = "en")
    fun `Sticker content`() {
        val body = "body"
        val info = ImageInfo(null, null, null, null, null, null, null)
        val message = createRoomEvent(false, null, StickerContent(body, info, "url"))
        val result = formatter.format(message, false)
        assertThat(result).isEqualTo(body)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Unable to decrypt content`() {
        val expected = "Waiting for this message"
        val senderName = "Someone"
        sequenceOf(false, true).forEach { isDm ->
            val message = createRoomEvent(false, senderName, UnableToDecryptContent(UnableToDecryptContent.Data.Unknown))
            val result = formatter.format(message, isDm)
            if (isDm) {
                assertThat(result).isEqualTo(expected)
            } else {
                assertThat(result).isInstanceOf(AnnotatedString::class.java)
                assertThat(result.toString()).isEqualTo("$senderName: $expected")
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
                val message = createRoomEvent(false, senderName, type)
                val result = formatter.format(message, isDm)
                if (isDm) {
                    assertWithMessage("$type was not properly handled").that(result).isEqualTo(expected)
                } else {
                    assertWithMessage("$type does not create an AnnotatedString").that(result).isInstanceOf(AnnotatedString::class.java)
                    assertWithMessage("$type was not properly handled").that(result.toString()).isEqualTo("$senderName: $expected")
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
            return MessageContent(body, null, false, false, type)
        }

        val sharedContentMessagesTypes = arrayOf(
            TextMessageType(body, null),
            VideoMessageType(body, null, null, MediaSource("url"), null),
            AudioMessageType(body, MediaSource("url"), null),
            VoiceMessageType(body, MediaSource("url"), null, null),
            ImageMessageType(body, null, null, MediaSource("url"), null),
            StickerMessageType(body, MediaSource("url"), null),
            FileMessageType(body, MediaSource("url"), null),
            LocationMessageType(body, "geo:1,2", null),
            NoticeMessageType(body, null),
            EmoteMessageType(body, null),
            OtherMessageType(msgType = "a_type", body = body),
        )
        val senderName = "Someone"
        val resultsInRoom = mutableListOf<Pair<MessageType, CharSequence?>>()
        val resultsInDm = mutableListOf<Pair<MessageType, CharSequence?>>()

        // Create messages for all types in DM and Room mode
        sequenceOf(false, true).forEach { isDm ->
            sharedContentMessagesTypes.forEach { type ->
                val content = createMessageContent(type)
                val message = createRoomEvent(sentByYou = false, senderDisplayName = "Someone", content = content)
                val result = formatter.format(message, isDmRoom = isDm)
                if (isDm) {
                    resultsInDm.add(type to result)
                } else {
                    resultsInRoom.add(type to result)
                }
            }
        }

        // Verify results of DM mode
        for ((type, result) in resultsInDm) {
            val expectedResult = when (type) {
                is VideoMessageType -> "Video"
                is AudioMessageType -> "Audio"
                is VoiceMessageType -> "Voice message"
                is ImageMessageType -> "Image"
                is StickerMessageType -> "Sticker"
                is FileMessageType -> "File"
                is LocationMessageType -> "Shared location"
                is EmoteMessageType -> "* $senderName ${type.body}"
                is TextMessageType,
                is NoticeMessageType,
                is OtherMessageType -> body
            }
            assertWithMessage("$type was not properly handled for DM").that(result).isEqualTo(expectedResult)
        }

        // Verify results of Room mode
        for ((type, result) in resultsInRoom) {
            val string = result.toString()
            val expectedResult = when (type) {
                is VideoMessageType -> "$senderName: Video"
                is AudioMessageType -> "$senderName: Audio"
                is VoiceMessageType -> "$senderName: Voice message"
                is ImageMessageType -> "$senderName: Image"
                is StickerMessageType -> "$senderName: Sticker"
                is FileMessageType -> "$senderName: File"
                is LocationMessageType -> "$senderName: Shared location"
                is TextMessageType,
                is NoticeMessageType,
                is OtherMessageType -> "$senderName: $body"
                is EmoteMessageType -> "* $senderName ${type.body}"
            }
            val shouldCreateAnnotatedString = when (type) {
                is VideoMessageType -> true
                is AudioMessageType -> true
                is VoiceMessageType -> true
                is ImageMessageType -> true
                is StickerMessageType -> true
                is FileMessageType -> true
                is LocationMessageType -> false
                is EmoteMessageType -> false
                is TextMessageType, is NoticeMessageType -> true
                is OtherMessageType -> true
            }
            if (shouldCreateAnnotatedString) {
                assertWithMessage("$type doesn't produce an AnnotatedString")
                    .that(result)
                    .isInstanceOf(AnnotatedString::class.java)
            }
            assertWithMessage("$type was not properly handled for room").that(string).isEqualTo(expectedResult)
        }
    }

    // endregion

    // region Membership change

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - joined`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.JOINED)

        val youJoinedRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youJoinedRoom = formatter.format(youJoinedRoomEvent, false)
        assertThat(youJoinedRoom).isEqualTo("You joined the room")

        val someoneJoinedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneJoinedRoom = formatter.format(someoneJoinedRoomEvent, false)
        assertThat(someoneJoinedRoom).isEqualTo("${someoneContent.userId} joined the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - left`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.LEFT)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.LEFT)

        val youLeftRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youLeftRoom = formatter.format(youLeftRoomEvent, false)
        assertThat(youLeftRoom).isEqualTo("You left the room")

        val someoneLeftRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneLeftRoom = formatter.format(someoneLeftRoomEvent, false)
        assertThat(someoneLeftRoom).isEqualTo("${someoneContent.userId} left the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - banned`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.BANNED)
        val youKickedContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KICKED_AND_BANNED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.BANNED)
        val someoneKickedContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KICKED_AND_BANNED)

        val youBannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youBanned = formatter.format(youBannedEvent, false)
        assertThat(youBanned).isEqualTo("You banned ${youContent.userId}")

        val youKickBannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youKickedContent)
        val youKickedBanned = formatter.format(youKickBannedEvent, false)
        assertThat(youKickedBanned).isEqualTo("You banned ${youContent.userId}")

        val someoneBannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneBanned = formatter.format(someoneBannedEvent, false)
        assertThat(someoneBanned).isEqualTo("$otherName banned ${someoneContent.userId}")

        val someoneKickBannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneKickedContent)
        val someoneKickBanned = formatter.format(someoneKickBannedEvent, false)
        assertThat(someoneKickBanned).isEqualTo("$otherName banned ${someoneContent.userId}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - unban`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.UNBANNED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.UNBANNED)

        val youUnbannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youUnbanned = formatter.format(youUnbannedEvent, false)
        assertThat(youUnbanned).isEqualTo("You unbanned ${youContent.userId}")

        val someoneUnbannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneUnbanned = formatter.format(someoneUnbannedEvent, false)
        assertThat(someoneUnbanned).isEqualTo("$otherName unbanned ${someoneContent.userId}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - kicked`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KICKED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KICKED)

        val youKickedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKicked = formatter.format(youKickedEvent, false)
        assertThat(youKicked).isEqualTo("You removed ${youContent.userId}")

        val someoneKickedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKicked = formatter.format(someoneKickedEvent, false)
        assertThat(someoneKicked).isEqualTo("$otherName removed ${someoneContent.userId}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invited`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.INVITED)

        val youWereInvitedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val youWereInvited = formatter.format(youWereInvitedEvent, false)
        assertThat(youWereInvited).isEqualTo("$otherName invited you")

        val youInvitedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youInvited = formatter.format(youInvitedEvent, false)
        assertThat(youInvited).isEqualTo("You invited ${someoneContent.userId}")

        val someoneInvitedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneInvited = formatter.format(someoneInvitedEvent, false)
        assertThat(someoneInvited).isEqualTo("$otherName invited ${someoneContent.userId}")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation accepted`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITATION_ACCEPTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.INVITATION_ACCEPTED)

        val youAcceptedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youAcceptedInvite = formatter.format(youAcceptedInviteEvent, false)
        assertThat(youAcceptedInvite).isEqualTo("You accepted the invite")

        val someoneAcceptedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedInvite = formatter.format(someoneAcceptedInviteEvent, false)
        assertThat(someoneAcceptedInvite).isEqualTo("${someoneContent.userId} accepted the invite")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation rejected`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.INVITATION_REJECTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.INVITATION_REJECTED)

        val youRejectedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRejectedInvite = formatter.format(youRejectedInviteEvent, false)
        assertThat(youRejectedInvite).isEqualTo("You rejected the invitation")

        val someoneRejectedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRejectedInvite = formatter.format(someoneRejectedInviteEvent, false)
        assertThat(someoneRejectedInvite).isEqualTo("${someoneContent.userId} rejected the invitation")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation revoked`() {
        val otherName = "Someone"
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.INVITATION_REVOKED)

        val youRevokedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youRevokedInvite = formatter.format(youRevokedInviteEvent, false)
        assertThat(youRevokedInvite).isEqualTo("You revoked the invitation for ${someoneContent.userId} to join the room")

        val someoneRevokedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRevokedInvite = formatter.format(someoneRevokedInviteEvent, false)
        assertThat(someoneRevokedInvite).isEqualTo("$otherName revoked the invitation for ${someoneContent.userId} to join the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knocked`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCKED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KNOCKED)

        val youKnockedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKnocked = formatter.format(youKnockedEvent, false)
        assertThat(youKnocked).isEqualTo("You requested to join")

        val someoneKnockedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKnocked = formatter.format(someoneKnockedEvent, false)
        assertThat(someoneKnocked).isEqualTo("${someoneContent.userId} requested to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock accepted`() {
        val otherName = "Someone"
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KNOCK_ACCEPTED)

        val youAcceptedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youAcceptedKnock = formatter.format(youAcceptedKnockEvent, false)
        assertThat(youAcceptedKnock).isEqualTo("${someoneContent.userId} allowed you to join")

        val someoneAcceptedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedKnock = formatter.format(someoneAcceptedKnockEvent, false)
        assertThat(someoneAcceptedKnock).isEqualTo("$otherName allowed ${someoneContent.userId} to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock retracted`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCK_RETRACTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KNOCK_RETRACTED)

        val youRetractedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRetractedKnock = formatter.format(youRetractedKnockEvent, false)
        assertThat(youRetractedKnock).isEqualTo("You cancelled your request to join")

        val someoneRetractedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRetractedKnock = formatter.format(someoneRetractedKnockEvent, false)
        assertThat(someoneRetractedKnock).isEqualTo("${someoneContent.userId} is no longer interested in joining")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock denied`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.KNOCK_DENIED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.KNOCK_DENIED)

        val youDeniedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youDeniedKnock = formatter.format(youDeniedKnockEvent, false)
        assertThat(youDeniedKnock).isEqualTo("You rejected ${someoneContent.userId}'s request to join")

        val someoneDeniedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneDeniedKnock = formatter.format(someoneDeniedKnockEvent, false)
        assertThat(someoneDeniedKnock).isEqualTo("$otherName rejected ${someoneContent.userId}'s request to join")

        val someoneDeniedYourKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val someoneDeniedYourKnock = formatter.format(someoneDeniedYourKnockEvent, false)
        assertThat(someoneDeniedYourKnock).isEqualTo("$otherName rejected your request to join")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - None`() {
        val otherName = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, MembershipChange.NONE)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), MembershipChange.NONE)

        val youNoneRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youNoneRoom = formatter.format(youNoneRoomEvent, false)
        assertThat(youNoneRoom).isEqualTo("You made no changes")

        val someoneNoneRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneNoneRoom = formatter.format(someoneNoneRoomEvent, false)
        assertThat(someoneNoneRoom).isEqualTo("$otherName made no changes")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - others`() {
        val otherChanges = arrayOf(MembershipChange.ERROR, MembershipChange.NOT_IMPLEMENTED, null)

        val results = otherChanges.map { change ->
            val content = RoomMembershipContent(A_USER_ID, change)
            val event = createRoomEvent(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.format(event, false)
            change to result
        }
        val expected = otherChanges.map { it to null }
        assertThat(results).isEqualTo(expected)
    }

    // endregion

    // region Room State

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - avatar`() {
        val otherName = "Someone"
        val changedContent = StateContent("", OtherState.RoomAvatar("new_avatar"))
        val removedContent = StateContent("", OtherState.RoomAvatar(null))

        val youChangedRoomAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomAvatar = formatter.format(youChangedRoomAvatarEvent, false)
        assertThat(youChangedRoomAvatar).isEqualTo("You changed the room avatar")

        val someoneChangedRoomAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomAvatar = formatter.format(someoneChangedRoomAvatarEvent, false)
        assertThat(someoneChangedRoomAvatar).isEqualTo("$otherName changed the room avatar")

        val youRemovedRoomAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomAvatar = formatter.format(youRemovedRoomAvatarEvent, false)
        assertThat(youRemovedRoomAvatar).isEqualTo("You removed the room avatar")

        val someoneRemovedRoomAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomAvatar = formatter.format(someoneRemovedRoomAvatarEvent, false)
        assertThat(someoneRemovedRoomAvatar).isEqualTo("$otherName removed the room avatar")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - create`() {
        val otherName = "Someone"
        val content = StateContent("", OtherState.RoomCreate)

        val youCreatedRoomMessage = createRoomEvent(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.format(youCreatedRoomMessage, false)
        assertThat(youCreatedRoom).isEqualTo("You created the room")

        val someoneCreatedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.format(someoneCreatedRoomEvent, false)
        assertThat(someoneCreatedRoom).isEqualTo("$otherName created the room")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - encryption`() {
        val otherName = "Someone"
        val content = StateContent("", OtherState.RoomEncryption)

        val youCreatedRoomMessage = createRoomEvent(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.format(youCreatedRoomMessage, false)
        assertThat(youCreatedRoom).isEqualTo("Encryption enabled")

        val someoneCreatedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.format(someoneCreatedRoomEvent, false)
        assertThat(someoneCreatedRoom).isEqualTo("Encryption enabled")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room name`() {
        val otherName = "Someone"
        val newName = "New name"
        val changedContent = StateContent("", OtherState.RoomName(newName))
        val removedContent = StateContent("", OtherState.RoomName(null))

        val youChangedRoomNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomName = formatter.format(youChangedRoomNameEvent, false)
        assertThat(youChangedRoomName).isEqualTo("You changed the room name to: $newName")

        val someoneChangedRoomNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomName = formatter.format(someoneChangedRoomNameEvent, false)
        assertThat(someoneChangedRoomName).isEqualTo("$otherName changed the room name to: $newName")

        val youRemovedRoomNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomName = formatter.format(youRemovedRoomNameEvent, false)
        assertThat(youRemovedRoomName).isEqualTo("You removed the room name")

        val someoneRemovedRoomNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomName = formatter.format(someoneRemovedRoomNameEvent, false)
        assertThat(someoneRemovedRoomName).isEqualTo("$otherName removed the room name")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - third party invite`() {
        val otherName = "Someone"
        val inviteeName = "Alice"
        val changedContent = StateContent("", OtherState.RoomThirdPartyInvite(inviteeName))
        val removedContent = StateContent("", OtherState.RoomThirdPartyInvite(null))

        val youInvitedSomeoneEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youInvitedSomeone = formatter.format(youInvitedSomeoneEvent, false)
        assertThat(youInvitedSomeone).isEqualTo("You sent an invitation to $inviteeName to join the room")

        val someoneInvitedSomeoneEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneInvitedSomeone = formatter.format(someoneInvitedSomeoneEvent, false)
        assertThat(someoneInvitedSomeone).isEqualTo("$otherName sent an invitation to $inviteeName to join the room")

        val youInvitedNoOneEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youInvitedNoOne = formatter.format(youInvitedNoOneEvent, false)
        assertThat(youInvitedNoOne).isNull()

        val someoneInvitedNoOneEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneInvitedNoOne = formatter.format(someoneInvitedNoOneEvent, false)
        assertThat(someoneInvitedNoOne).isNull()
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room topic`() {
        val otherName = "Someone"
        val roomTopic = "New topic"
        val changedContent = StateContent("", OtherState.RoomTopic(roomTopic))
        val removedContent = StateContent("", OtherState.RoomTopic(null))

        val youChangedRoomTopicEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomTopic = formatter.format(youChangedRoomTopicEvent, false)
        assertThat(youChangedRoomTopic).isEqualTo("You changed the topic to: $roomTopic")

        val someoneChangedRoomTopicEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomTopic = formatter.format(someoneChangedRoomTopicEvent, false)
        assertThat(someoneChangedRoomTopic).isEqualTo("$otherName changed the topic to: $roomTopic")

        val youRemovedRoomTopicEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomTopic = formatter.format(youRemovedRoomTopicEvent, false)
        assertThat(youRemovedRoomTopic).isEqualTo("You removed the room topic")

        val someoneRemovedRoomTopicEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomTopic = formatter.format(someoneRemovedRoomTopicEvent, false)
        assertThat(someoneRemovedRoomTopic).isEqualTo("$otherName removed the room topic")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - others must return null`() {
        val otherStates = arrayOf(
            OtherState.PolicyRuleRoom,
            OtherState.PolicyRuleServer,
            OtherState.PolicyRuleUser,
            OtherState.RoomAliases,
            OtherState.RoomCanonicalAlias,
            OtherState.RoomGuestAccess,
            OtherState.RoomHistoryVisibility,
            OtherState.RoomJoinRules,
            OtherState.RoomPinnedEvents,
            OtherState.RoomUserPowerLevels(emptyMap()),
            OtherState.RoomServerAcl,
            OtherState.RoomTombstone,
            OtherState.SpaceChild,
            OtherState.SpaceParent,
            OtherState.Custom("custom_event_type")
        )

        val results = otherStates.map { state ->
            val content = StateContent("", state)
            val event = createRoomEvent(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.format(event, false)
            state to result
        }
        val expected = otherStates.map { it to null }
        assertThat(results).isEqualTo(expected)
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

        val youChangedAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedAvatar = formatter.format(youChangedAvatarEvent, false)
        assertThat(youChangedAvatar).isEqualTo("You changed your avatar")

        val someoneChangeAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangeAvatar = formatter.format(someoneChangeAvatarEvent, false)
        assertThat(someoneChangeAvatar).isEqualTo("$otherName changed their avatar")

        val youSetAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetAvatar = formatter.format(youSetAvatarEvent, false)
        assertThat(youSetAvatar).isEqualTo("You changed your avatar")

        val someoneSetAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetAvatar = formatter.format(someoneSetAvatarEvent, false)
        assertThat(someoneSetAvatar).isEqualTo("$otherName changed their avatar")

        val youRemovedAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedAvatar = formatter.format(youRemovedAvatarEvent, false)
        assertThat(youRemovedAvatar).isEqualTo("You changed your avatar")

        val someoneRemovedAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedAvatar = formatter.format(someoneRemovedAvatarEvent, false)
        assertThat(someoneRemovedAvatar).isEqualTo("$otherName changed their avatar")

        val unchangedEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.format(unchangedEvent, false)
        assertThat(unchangedResult).isNull()

        val invalidEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.format(invalidEvent, false)
        assertThat(invalidResult).isNull()
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

        val youChangedDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedDisplayName = formatter.format(youChangedDisplayNameEvent, false)
        assertThat(youChangedDisplayName).isEqualTo("You changed your display name from $oldDisplayName to $newDisplayName")

        val someoneChangedDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedDisplayName = formatter.format(someoneChangedDisplayNameEvent, false)
        assertThat(someoneChangedDisplayName).isEqualTo("$someoneElseId changed their display name from $oldDisplayName to $newDisplayName")

        val youSetDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetDisplayName = formatter.format(youSetDisplayNameEvent, false)
        assertThat(youSetDisplayName).isEqualTo("You set your display name to $newDisplayName")

        val someoneSetDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetDisplayName = formatter.format(someoneSetDisplayNameEvent, false)
        assertThat(someoneSetDisplayName).isEqualTo("$someoneElseId set their display name to $newDisplayName")

        val youRemovedDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedDisplayName = formatter.format(youRemovedDisplayNameEvent, false)
        assertThat(youRemovedDisplayName).isEqualTo("You removed your display name (it was $oldDisplayName)")

        val someoneRemovedDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedDisplayName = formatter.format(someoneRemovedDisplayNameEvent, false)
        assertThat(someoneRemovedDisplayName).isEqualTo("$someoneElseId removed their display name (it was $oldDisplayName)")

        val unchangedEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.format(unchangedEvent, false)
        assertThat(unchangedResult).isNull()

        val invalidEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.format(invalidEvent, false)
        assertThat(invalidResult).isNull()
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

        val youChangedBothEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedBoth = formatter.format(youChangedBothEvent, false)
        assertThat(youChangedBoth).isEqualTo("You changed your display name from $oldDisplayName to $newDisplayName\n(avatar was changed too)")

        val invalidContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = invalidContent)
        val invalidMessage = formatter.format(invalidContentEvent, false)
        assertThat(invalidMessage).isNull()

        val sameContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = sameContent)
        val sameMessage = formatter.format(sameContentEvent, false)
        assertThat(sameMessage).isNull()
    }

    // endregion

    // region Polls

    @Test
    @Config(qualifiers = "en")
    fun `Computes last message for poll in DM`() {
        val pollContent = aPollContent()

        val mineContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = "Alice", content = pollContent)
        assertThat(formatter.format(mineContentEvent, true)).isEqualTo("Poll: Do you like polls?")

        val contentEvent = createRoomEvent(sentByYou = false, senderDisplayName = "Bob", content = pollContent)
        assertThat(formatter.format(contentEvent, true)).isEqualTo("Poll: Do you like polls?")
    }

    @Test
    @Config(qualifiers = "en")
    fun `Computes last message for poll in room`() {
        val pollContent = aPollContent()

        val mineContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = "Alice", content = pollContent)
        assertThat(formatter.format(mineContentEvent, false).toString()).isEqualTo("Alice: Poll: Do you like polls?")

        val contentEvent = createRoomEvent(sentByYou = false, senderDisplayName = "Bob", content = pollContent)
        assertThat(formatter.format(contentEvent, false).toString()).isEqualTo("Bob: Poll: Do you like polls?")
    }

    // endregion

    private fun createRoomEvent(sentByYou: Boolean, senderDisplayName: String?, content: EventContent): EventTimelineItem {
        val sender = if (sentByYou) A_USER_ID else someoneElseId
        val profile = ProfileTimelineDetails.Ready(senderDisplayName, false, null)
        return anEventTimelineItem(
            content = content,
            senderProfile = profile,
            sender = sender,
            isOwn = sentByYou,
        )
    }

    private val someoneElseId = UserId("@someone_else:domain")
}
