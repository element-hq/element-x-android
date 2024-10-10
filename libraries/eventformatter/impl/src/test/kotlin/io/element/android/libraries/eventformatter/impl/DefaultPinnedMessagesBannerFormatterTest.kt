/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.aProfileChangeMessageContent
import io.element.android.libraries.matrix.test.timeline.aProfileTimelineDetails
import io.element.android.libraries.matrix.test.timeline.aStickerContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class DefaultPinnedMessagesBannerFormatterTest {
    private lateinit var context: Context
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var formatter: DefaultPinnedMessagesBannerFormatter
    private lateinit var unsupportedEvent: String

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() as Context
        fakeMatrixClient = FakeMatrixClient()
        val stringProvider = AndroidStringProvider(context.resources)
        formatter = DefaultPinnedMessagesBannerFormatter(
            sp = stringProvider,
            permalinkParser = FakePermalinkParser(),
        )
        unsupportedEvent = stringProvider.getString(CommonStrings.common_unsupported_event)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Redacted content`() {
        val expected = "Message removed"
        val senderName = "Someone"
        val message = createRoomEvent(false, senderName, RedactedContent)
        val result = formatter.format(message)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Sticker content`() {
        val body = "a sticker body"
        val info = ImageInfo(null, null, null, null, null, null, null)
        val message = createRoomEvent(false, null, aStickerContent(body, info, aMediaSource(url = "url")))
        val result = formatter.format(message)
        val expectedBody = "Sticker: a sticker body"
        assertThat(result.toString()).isEqualTo(expectedBody)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Unable to decrypt content`() {
        val expected = "Waiting for this message"
        val senderName = "Someone"
        val message = createRoomEvent(false, senderName, UnableToDecryptContent(UnableToDecryptContent.Data.Unknown))
        val result = formatter.format(message)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @Config(qualifiers = "en")
    fun `FailedToParseMessageLike, FailedToParseState & Unknown content`() {
        val senderName = "Someone"
        sequenceOf(
            FailedToParseMessageLikeContent("", ""),
            FailedToParseStateContent("", "", ""),
            UnknownContent,
        ).forEach { type ->
            val message = createRoomEvent(false, senderName, type)
            val result = formatter.format(message)
            assertWithMessage("$type was not properly handled").that(result).isEqualTo(unsupportedEvent)
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
            AudioMessageType(body, null, null, MediaSource("url"), null),
            VoiceMessageType(body, null, null, MediaSource("url"), null, null),
            ImageMessageType(body, null, null, MediaSource("url"), null),
            StickerMessageType(body, null, null, MediaSource("url"), null),
            FileMessageType(body, null, null, MediaSource("url"), null),
            LocationMessageType(body, "geo:1,2", null),
            NoticeMessageType(body, null),
            EmoteMessageType(body, null),
            OtherMessageType(msgType = "a_type", body = body),
        )
        val results = mutableListOf<Pair<MessageType, CharSequence?>>()

        sharedContentMessagesTypes.forEach { type ->
            val content = createMessageContent(type)
            val message = createRoomEvent(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.format(message)
            results.add(type to result)
        }

        // Verify results type
        for ((type, result) in results) {
            val expectedResult = when (type) {
                is VideoMessageType,
                is AudioMessageType,
                is VoiceMessageType,
                is ImageMessageType,
                is StickerMessageType,
                is FileMessageType,
                is LocationMessageType -> AnnotatedString::class.java
                is EmoteMessageType,
                is TextMessageType,
                is NoticeMessageType,
                is OtherMessageType -> String::class.java
            }
            assertThat(result).isInstanceOf(expectedResult)
        }
        // Verify results content
        for ((type, result) in results) {
            val expectedResult = when (type) {
                is VideoMessageType -> "Video: Shared body"
                is AudioMessageType -> "Audio: Shared body"
                is VoiceMessageType -> "Voice message: Shared body"
                is ImageMessageType -> "Image: Shared body"
                is StickerMessageType -> "Sticker: Shared body"
                is FileMessageType -> "File: Shared body"
                is LocationMessageType -> "Shared location: Shared body"
                is EmoteMessageType -> "* Someone ${type.body}"
                is TextMessageType,
                is NoticeMessageType,
                is OtherMessageType -> body
            }
            assertWithMessage("$type was not properly handled").that(result.toString()).isEqualTo(expectedResult)
        }
    }

    // endregion

    // region Membership change

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - joined`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.JOINED)

        val youJoinedRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youJoinedRoom = formatter.format(youJoinedRoomEvent)
        assertThat(youJoinedRoom).isEqualTo(unsupportedEvent)

        val someoneJoinedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneJoinedRoom = formatter.format(someoneJoinedRoomEvent)
        assertThat(someoneJoinedRoom).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - left`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.LEFT)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.LEFT)

        val youLeftRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youLeftRoom = formatter.format(youLeftRoomEvent)
        assertThat(youLeftRoom).isEqualTo(unsupportedEvent)

        val someoneLeftRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneLeftRoom = formatter.format(someoneLeftRoomEvent)
        assertThat(someoneLeftRoom).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - banned`() {
        val otherName = "Other"
        val third = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.BANNED)
        val youKickedContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KICKED_AND_BANNED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.BANNED)
        val someoneKickedContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KICKED_AND_BANNED)

        val youBannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youBanned = formatter.format(youBannedEvent)
        assertThat(youBanned).isEqualTo(unsupportedEvent)

        val youKickBannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youKickedContent)
        val youKickedBanned = formatter.format(youKickBannedEvent)
        assertThat(youKickedBanned).isEqualTo(unsupportedEvent)

        val someoneBannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneBanned = formatter.format(someoneBannedEvent)
        assertThat(someoneBanned).isEqualTo(unsupportedEvent)

        val someoneKickBannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneKickedContent)
        val someoneKickBanned = formatter.format(someoneKickBannedEvent)
        assertThat(someoneKickBanned).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - unban`() {
        val otherName = "Other"
        val third = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.UNBANNED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.UNBANNED)

        val youUnbannedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youUnbanned = formatter.format(youUnbannedEvent)
        assertThat(youUnbanned).isEqualTo(unsupportedEvent)

        val someoneUnbannedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneUnbanned = formatter.format(someoneUnbannedEvent)
        assertThat(someoneUnbanned).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - kicked`() {
        val otherName = "Other"
        val third = "Someone"
        val youContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KICKED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KICKED)

        val youKickedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKicked = formatter.format(youKickedEvent)
        assertThat(youKicked).isEqualTo(unsupportedEvent)

        val someoneKickedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKicked = formatter.format(someoneKickedEvent)
        assertThat(someoneKicked).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invited`() {
        val otherName = "Other"
        val third = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.INVITED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.INVITED)

        val youWereInvitedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val youWereInvited = formatter.format(youWereInvitedEvent)
        assertThat(youWereInvited).isEqualTo(unsupportedEvent)

        val youInvitedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youInvited = formatter.format(youInvitedEvent)
        assertThat(youInvited).isEqualTo(unsupportedEvent)

        val someoneInvitedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneInvited = formatter.format(someoneInvitedEvent)
        assertThat(someoneInvited).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation accepted`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.INVITATION_ACCEPTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.INVITATION_ACCEPTED)

        val youAcceptedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youAcceptedInvite = formatter.format(youAcceptedInviteEvent)
        assertThat(youAcceptedInvite).isEqualTo(unsupportedEvent)

        val someoneAcceptedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedInvite = formatter.format(someoneAcceptedInviteEvent)
        assertThat(someoneAcceptedInvite).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation rejected`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.INVITATION_REJECTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.INVITATION_REJECTED)

        val youRejectedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRejectedInvite = formatter.format(youRejectedInviteEvent)
        assertThat(youRejectedInvite).isEqualTo(unsupportedEvent)

        val someoneRejectedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRejectedInvite = formatter.format(someoneRejectedInviteEvent)
        assertThat(someoneRejectedInvite).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - invitation revoked`() {
        val otherName = "Other"
        val third = "Someone"
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.INVITATION_REVOKED)

        val youRevokedInviteEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youRevokedInvite = formatter.format(youRevokedInviteEvent)
        assertThat(youRevokedInvite).isEqualTo(unsupportedEvent)

        val someoneRevokedInviteEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRevokedInvite = formatter.format(someoneRevokedInviteEvent)
        assertThat(someoneRevokedInvite).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knocked`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.KNOCKED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.KNOCKED)

        val youKnockedEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youKnocked = formatter.format(youKnockedEvent)
        assertThat(youKnocked).isEqualTo(unsupportedEvent)

        val someoneKnockedEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneKnocked = formatter.format(someoneKnockedEvent)
        assertThat(someoneKnocked).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock accepted`() {
        val otherName = "Other"
        val third = "Someone"
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KNOCK_ACCEPTED)

        val youAcceptedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youAcceptedKnock = formatter.format(youAcceptedKnockEvent)
        assertThat(youAcceptedKnock).isEqualTo(unsupportedEvent)

        val someoneAcceptedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneAcceptedKnock = formatter.format(someoneAcceptedKnockEvent)
        assertThat(someoneAcceptedKnock).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock retracted`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.KNOCK_RETRACTED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), null, MembershipChange.KNOCK_RETRACTED)

        val youRetractedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youRetractedKnock = formatter.format(youRetractedKnockEvent)
        assertThat(youRetractedKnock).isEqualTo(unsupportedEvent)

        val someoneRetractedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneRetractedKnock = formatter.format(someoneRetractedKnockEvent)
        assertThat(someoneRetractedKnock).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - knock denied`() {
        val otherName = "Other"
        val third = "Someone"
        val youContent = RoomMembershipContent(A_USER_ID, third, MembershipChange.KNOCK_DENIED)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), third, MembershipChange.KNOCK_DENIED)

        val youDeniedKnockEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = someoneContent)
        val youDeniedKnock = formatter.format(youDeniedKnockEvent)
        assertThat(youDeniedKnock).isEqualTo(unsupportedEvent)

        val someoneDeniedKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneDeniedKnock = formatter.format(someoneDeniedKnockEvent)
        assertThat(someoneDeniedKnock).isEqualTo(unsupportedEvent)

        val someoneDeniedYourKnockEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = youContent)
        val someoneDeniedYourKnock = formatter.format(someoneDeniedYourKnockEvent)
        assertThat(someoneDeniedYourKnock).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - None`() {
        val otherName = "Other"
        val youContent = RoomMembershipContent(A_USER_ID, null, MembershipChange.NONE)
        val someoneContent = RoomMembershipContent(UserId("@someone_else:domain"), otherName, MembershipChange.NONE)

        val youNoneRoomEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = youContent)
        val youNoneRoom = formatter.format(youNoneRoomEvent)
        assertThat(youNoneRoom).isEqualTo(unsupportedEvent)

        val someoneNoneRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = someoneContent)
        val someoneNoneRoom = formatter.format(someoneNoneRoomEvent)
        assertThat(someoneNoneRoom).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Membership change - others`() {
        val otherChanges = arrayOf(MembershipChange.ERROR, MembershipChange.NOT_IMPLEMENTED, null)

        val results = otherChanges.map { change ->
            val content = RoomMembershipContent(A_USER_ID, null, change)
            val event = createRoomEvent(sentByYou = false, senderDisplayName = "Someone", content = content)
            val result = formatter.format(event)
            change to result
        }
        val expected = otherChanges.map { it to unsupportedEvent }
        assertThat(results).isEqualTo(expected)
    }

    // endregion

    // region Room State

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - avatar`() {
        val otherName = "Other"
        val changedContent = StateContent("", OtherState.RoomAvatar("new_avatar"))
        val removedContent = StateContent("", OtherState.RoomAvatar(null))

        val youChangedRoomAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomAvatar = formatter.format(youChangedRoomAvatarEvent)
        assertThat(youChangedRoomAvatar).isEqualTo(unsupportedEvent)

        val someoneChangedRoomAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomAvatar = formatter.format(someoneChangedRoomAvatarEvent)
        assertThat(someoneChangedRoomAvatar).isEqualTo(unsupportedEvent)

        val youRemovedRoomAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomAvatar = formatter.format(youRemovedRoomAvatarEvent)
        assertThat(youRemovedRoomAvatar).isEqualTo(unsupportedEvent)

        val someoneRemovedRoomAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomAvatar = formatter.format(someoneRemovedRoomAvatarEvent)
        assertThat(someoneRemovedRoomAvatar).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - create`() {
        val otherName = "Other"
        val content = StateContent("", OtherState.RoomCreate)

        val youCreatedRoomMessage = createRoomEvent(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.format(youCreatedRoomMessage)
        assertThat(youCreatedRoom).isEqualTo(unsupportedEvent)

        val someoneCreatedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.format(someoneCreatedRoomEvent)
        assertThat(someoneCreatedRoom).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - encryption`() {
        val otherName = "Other"
        val content = StateContent("", OtherState.RoomEncryption)

        val youCreatedRoomMessage = createRoomEvent(sentByYou = true, senderDisplayName = null, content = content)
        val youCreatedRoom = formatter.format(youCreatedRoomMessage)
        assertThat(youCreatedRoom).isEqualTo(unsupportedEvent)

        val someoneCreatedRoomEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = content)
        val someoneCreatedRoom = formatter.format(someoneCreatedRoomEvent)
        assertThat(someoneCreatedRoom).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room name`() {
        val otherName = "Other"
        val newName = "New name"
        val changedContent = StateContent("", OtherState.RoomName(newName))
        val removedContent = StateContent("", OtherState.RoomName(null))

        val youChangedRoomNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomName = formatter.format(youChangedRoomNameEvent)
        assertThat(youChangedRoomName).isEqualTo(unsupportedEvent)

        val someoneChangedRoomNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomName = formatter.format(someoneChangedRoomNameEvent)
        assertThat(someoneChangedRoomName).isEqualTo(unsupportedEvent)

        val youRemovedRoomNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomName = formatter.format(youRemovedRoomNameEvent)
        assertThat(youRemovedRoomName).isEqualTo(unsupportedEvent)

        val someoneRemovedRoomNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomName = formatter.format(someoneRemovedRoomNameEvent)
        assertThat(someoneRemovedRoomName).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - third party invite`() {
        val otherName = "Other"
        val inviteeName = "Alice"
        val changedContent = StateContent("", OtherState.RoomThirdPartyInvite(inviteeName))
        val removedContent = StateContent("", OtherState.RoomThirdPartyInvite(null))

        val youInvitedSomeoneEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youInvitedSomeone = formatter.format(youInvitedSomeoneEvent)
        assertThat(youInvitedSomeone).isEqualTo(unsupportedEvent)

        val someoneInvitedSomeoneEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneInvitedSomeone = formatter.format(someoneInvitedSomeoneEvent)
        assertThat(someoneInvitedSomeone).isEqualTo(unsupportedEvent)

        val youInvitedNoOneEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youInvitedNoOne = formatter.format(youInvitedNoOneEvent)
        assertThat(youInvitedNoOne).isEqualTo(unsupportedEvent)

        val someoneInvitedNoOneEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneInvitedNoOne = formatter.format(someoneInvitedNoOneEvent)
        assertThat(someoneInvitedNoOne).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Room state change - room topic`() {
        val otherName = "Other"
        val roomTopic = "New topic"
        val changedContent = StateContent("", OtherState.RoomTopic(roomTopic))
        val removedContent = StateContent("", OtherState.RoomTopic(null))

        val youChangedRoomTopicEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedRoomTopic = formatter.format(youChangedRoomTopicEvent)
        assertThat(youChangedRoomTopic).isEqualTo(unsupportedEvent)

        val someoneChangedRoomTopicEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedRoomTopic = formatter.format(someoneChangedRoomTopicEvent)
        assertThat(someoneChangedRoomTopic).isEqualTo(unsupportedEvent)

        val youRemovedRoomTopicEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedRoomTopic = formatter.format(youRemovedRoomTopicEvent)
        assertThat(youRemovedRoomTopic).isEqualTo(unsupportedEvent)

        val someoneRemovedRoomTopicEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedRoomTopic = formatter.format(someoneRemovedRoomTopicEvent)
        assertThat(someoneRemovedRoomTopic).isEqualTo(unsupportedEvent)
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
            OtherState.RoomPinnedEvents(OtherState.RoomPinnedEvents.Change.CHANGED),
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
            val result = formatter.format(event)
            state to result
        }
        val expected = otherStates.map { it to unsupportedEvent }
        assertThat(results).isEqualTo(expected)
    }

    // endregion

    // region Profile change

    @Test
    @Config(qualifiers = "en")
    fun `Profile change - avatar`() {
        val otherName = "Other"
        val changedContent = aProfileChangeMessageContent(avatarUrl = "new_avatar_url", prevAvatarUrl = "old_avatar_url")
        val setContent = aProfileChangeMessageContent(avatarUrl = "new_avatar_url", prevAvatarUrl = null)
        val removedContent = aProfileChangeMessageContent(avatarUrl = null, prevAvatarUrl = "old_avatar_url")
        val invalidContent = aProfileChangeMessageContent(avatarUrl = null, prevAvatarUrl = null)
        val sameContent = aProfileChangeMessageContent(avatarUrl = "same_avatar_url", prevAvatarUrl = "same_avatar_url")

        val youChangedAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedAvatar = formatter.format(youChangedAvatarEvent)
        assertThat(youChangedAvatar).isEqualTo(unsupportedEvent)

        val someoneChangeAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangeAvatar = formatter.format(someoneChangeAvatarEvent)
        assertThat(someoneChangeAvatar).isEqualTo(unsupportedEvent)

        val youSetAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetAvatar = formatter.format(youSetAvatarEvent)
        assertThat(youSetAvatar).isEqualTo(unsupportedEvent)

        val someoneSetAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetAvatar = formatter.format(someoneSetAvatarEvent)
        assertThat(someoneSetAvatar).isEqualTo(unsupportedEvent)

        val youRemovedAvatarEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedAvatar = formatter.format(youRemovedAvatarEvent)
        assertThat(youRemovedAvatar).isEqualTo(unsupportedEvent)

        val someoneRemovedAvatarEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedAvatar = formatter.format(someoneRemovedAvatarEvent)
        assertThat(someoneRemovedAvatar).isEqualTo(unsupportedEvent)

        val unchangedEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.format(unchangedEvent)
        assertThat(unchangedResult).isEqualTo(unsupportedEvent)

        val invalidEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.format(invalidEvent)
        assertThat(invalidResult).isEqualTo(unsupportedEvent)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Profile change - display name`() {
        val newDisplayName = "New"
        val oldDisplayName = "Old"
        val otherName = "Other"
        val changedContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = oldDisplayName)
        val setContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = null)
        val removedContent = aProfileChangeMessageContent(displayName = null, prevDisplayName = oldDisplayName)
        val sameContent = aProfileChangeMessageContent(displayName = newDisplayName, prevDisplayName = newDisplayName)
        val invalidContent = aProfileChangeMessageContent(displayName = null, prevDisplayName = null)

        val youChangedDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = changedContent)
        val youChangedDisplayName = formatter.format(youChangedDisplayNameEvent)
        assertThat(youChangedDisplayName).isEqualTo(unsupportedEvent)

        val someoneChangedDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = changedContent)
        val someoneChangedDisplayName = formatter.format(someoneChangedDisplayNameEvent)
        assertThat(someoneChangedDisplayName).isEqualTo(unsupportedEvent)

        val youSetDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = setContent)
        val youSetDisplayName = formatter.format(youSetDisplayNameEvent)
        assertThat(youSetDisplayName).isEqualTo(unsupportedEvent)

        val someoneSetDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = setContent)
        val someoneSetDisplayName = formatter.format(someoneSetDisplayNameEvent)
        assertThat(someoneSetDisplayName).isEqualTo(unsupportedEvent)

        val youRemovedDisplayNameEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = removedContent)
        val youRemovedDisplayName = formatter.format(youRemovedDisplayNameEvent)
        assertThat(youRemovedDisplayName).isEqualTo(unsupportedEvent)

        val someoneRemovedDisplayNameEvent = createRoomEvent(sentByYou = false, senderDisplayName = otherName, content = removedContent)
        val someoneRemovedDisplayName = formatter.format(someoneRemovedDisplayNameEvent)
        assertThat(someoneRemovedDisplayName).isEqualTo(unsupportedEvent)

        val unchangedEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = sameContent)
        val unchangedResult = formatter.format(unchangedEvent)
        assertThat(unchangedResult).isEqualTo(unsupportedEvent)

        val invalidEvent = createRoomEvent(sentByYou = true, senderDisplayName = otherName, content = invalidContent)
        val invalidResult = formatter.format(invalidEvent)
        assertThat(invalidResult).isEqualTo(unsupportedEvent)
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
        val youChangedBoth = formatter.format(youChangedBothEvent)
        assertThat(youChangedBoth).isEqualTo(unsupportedEvent)

        val invalidContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = invalidContent)
        val invalidMessage = formatter.format(invalidContentEvent)
        assertThat(invalidMessage).isEqualTo(unsupportedEvent)

        val sameContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = null, content = sameContent)
        val sameMessage = formatter.format(sameContentEvent)
        assertThat(sameMessage).isEqualTo(unsupportedEvent)
    }

    // endregion

    // region Polls

    @Test
    @Config(qualifiers = "en")
    fun `Computes last message for poll`() {
        val pollContent = aPollContent()

        val mineContentEvent = createRoomEvent(sentByYou = true, senderDisplayName = "Alice", content = pollContent)
        val result = formatter.format(mineContentEvent)
        assertThat(result).isInstanceOf(AnnotatedString::class.java)
        assertThat(result.toString()).isEqualTo("Poll: Do you like polls?")

        val contentEvent = createRoomEvent(sentByYou = false, senderDisplayName = "Bob", content = pollContent)
        val result2 = formatter.format(contentEvent)
        assertThat(result2).isInstanceOf(AnnotatedString::class.java)
        assertThat(result2.toString()).isEqualTo("Poll: Do you like polls?")
    }

    // endregion

    private fun createRoomEvent(
        sentByYou: Boolean,
        senderDisplayName: String?,
        content: EventContent,
    ): EventTimelineItem {
        val sender = if (sentByYou) A_USER_ID else someoneElseId
        val profile = aProfileTimelineDetails(senderDisplayName)
        return anEventTimelineItem(
            content = content,
            senderProfile = profile,
            sender = sender,
            isOwn = sentByYou,
        )
    }

    private val someoneElseId = UserId("@someone_else:domain")
}
