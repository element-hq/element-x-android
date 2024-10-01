/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.notification.CallNotifyType
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_REDACTION_REASON
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_TIMESTAMP
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.notification.FakeNotificationService
import io.element.android.libraries.matrix.test.notification.aNotificationData
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationMediaRepo
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import io.element.android.services.toolbox.impl.systemclock.DefaultSystemClock
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class DefaultNotifiableEventResolverTest {
    @Test
    fun `resolve event no session`() = runTest {
        val sut = createDefaultNotifiableEventResolver(notificationService = null)
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve event failure`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.failure(AN_EXCEPTION)
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve event null`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(null)
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve event message text`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = TextMessageType(body = "Hello world", formatted = null)
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @Config(qualifiers = "en")
    fun `resolve event message with mention`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = TextMessageType(body = "Hello world", formatted = null)
                    ),
                    hasMention = true,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world", hasMentionOrReply = true)
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve HTML formatted event message text takes plain text version`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = TextMessageType(
                            body = "Hello world!",
                            formatted = FormattedBody(
                                body = "<b>Hello world</b>",
                                format = MessageFormat.HTML,
                            )
                        )
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve incorrectly formatted event message text uses fallback`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = TextMessageType(
                            body = "Hello world",
                            formatted = FormattedBody(
                                body = "???Hello world!???",
                                format = MessageFormat.UNKNOWN,
                            )
                        )
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message audio`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = AudioMessageType("Audio", null, null, MediaSource("url"), null)
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Audio")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message video`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = VideoMessageType("Video", null, null, MediaSource("url"), null)
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Video")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message voice`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = VoiceMessageType("Voice", null, null, MediaSource("url"), null, null)
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Voice message")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message image`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = ImageMessageType("Image", null, null, MediaSource("url"), null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Image")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message sticker`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = StickerMessageType("Sticker", null, null, MediaSource("url"), null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Sticker")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message file`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = FileMessageType("File", null, null, MediaSource("url"), null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "File")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message location`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = LocationMessageType("Location", "geo:1,2", null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Location")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message notice`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = NoticeMessageType("Notice", null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Notice")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve event message emote`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomMessage(
                        senderId = A_USER_ID_2,
                        messageType = EmoteMessageType("is happy", null),
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "* Bob is happy")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve poll`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.Poll(
                        senderId = A_USER_ID_2,
                        question = "A question"
                    ),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Poll: A question")
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve RoomMemberContent invite room`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.StateEvent.RoomMemberContent(
                        userId = A_USER_ID_2,
                        membershipState = RoomMembershipState.INVITE
                    ),
                    isDirect = false,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve invite room`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.Invite(
                        senderId = A_USER_ID_2,
                    ),
                    isDirect = false,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                canBeReplaced = true,
                roomName = A_ROOM_NAME,
                noisy = false,
                title = null,
                description = "Bob invited you to join the room",
                type = null,
                timestamp = A_TIMESTAMP,
                soundName = null,
                isRedacted = false,
                isUpdated = false,
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve invite direct`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.Invite(
                        senderId = A_USER_ID_2,
                    ),
                    isDirect = true,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                canBeReplaced = true,
                roomName = A_ROOM_NAME,
                noisy = false,
                title = null,
                description = "Bob invited you to chat",
                type = null,
                timestamp = A_TIMESTAMP,
                soundName = null,
                isRedacted = false,
                isUpdated = false,
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve invite direct, no display name`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.Invite(
                        senderId = A_USER_ID_2,
                    ),
                    isDirect = true,
                    senderDisplayName = null,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                canBeReplaced = true,
                roomName = A_ROOM_NAME,
                noisy = false,
                title = null,
                description = "@bob:server.org invited you to chat",
                type = null,
                timestamp = A_TIMESTAMP,
                soundName = null,
                isRedacted = false,
                isUpdated = false,
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve invite direct, ambiguous display name`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.Invite(
                        senderId = A_USER_ID_2,
                    ),
                    isDirect = false,
                    senderIsNameAmbiguous = true,
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                canBeReplaced = true,
                roomName = A_ROOM_NAME,
                noisy = false,
                title = null,
                description = "Bob (@bob:server.org) invited you to join the room",
                type = null,
                timestamp = A_TIMESTAMP,
                soundName = null,
                isRedacted = false,
                isUpdated = false,
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve RoomMemberContent other`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.StateEvent.RoomMemberContent(
                        userId = A_USER_ID_2,
                        membershipState = RoomMembershipState.JOIN
                    )
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve RoomEncrypted`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomEncrypted
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            FallbackNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                description = "Notification",
                canBeReplaced = true,
                isRedacted = false,
                isUpdated = false,
                timestamp = A_FAKE_TIMESTAMP,
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve CallInvite`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.CallInvite(A_USER_ID_2),
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        val expectedResult = ResolvedPushEvent.Event(
            NotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                canBeReplaced = false,
                senderId = A_USER_ID_2,
                noisy = false,
                timestamp = A_TIMESTAMP,
                senderDisambiguatedDisplayName = A_USER_NAME_2,
                body = "Call in progress (unsupported)",
                imageUriString = null,
                threadId = null,
                roomName = A_ROOM_NAME,
                roomAvatarPath = null,
                senderAvatarPath = null,
                soundName = null,
                outGoingMessage = false,
                outGoingMessageFailed = false,
                isRedacted = false,
                isUpdated = false
            )
        )
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve CallNotify - ringing`() = runTest {
        val timestamp = DefaultSystemClock().epochMillis()
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.CallNotify(
                        A_USER_ID_2,
                        CallNotifyType.RING
                    ),
                    timestamp = timestamp,
                )
            )
        )
        val expectedResult = ResolvedPushEvent.Event(
            NotifiableRingingCallEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                senderId = A_USER_ID_2,
                roomName = A_ROOM_NAME,
                editedEventId = null,
                description = "Incoming call",
                timestamp = timestamp,
                canBeReplaced = true,
                isRedacted = false,
                isUpdated = false,
                senderDisambiguatedDisplayName = A_USER_NAME_2,
                senderAvatarUrl = null,
                callNotifyType = CallNotifyType.RING,
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve CallNotify - ring but timed out displays the same as notify`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.CallNotify(
                        A_USER_ID_2,
                        CallNotifyType.RING
                    ),
                    timestamp = 0L,
                )
            )
        )
        val expectedResult = ResolvedPushEvent.Event(
            NotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = true,
                timestamp = 0L,
                senderDisambiguatedDisplayName = A_USER_NAME_2,
                senderId = A_USER_ID_2,
                body = "☎\uFE0F Incoming call",
                roomId = A_ROOM_ID,
                threadId = null,
                roomName = A_ROOM_NAME,
                canBeReplaced = false,
                isRedacted = false,
                imageUriString = null,
                type = EventType.CALL_NOTIFY,
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve CallNotify - notify`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.CallNotify(
                        A_USER_ID_2,
                        CallNotifyType.NOTIFY
                    ),
                )
            )
        )
        val expectedResult = ResolvedPushEvent.Event(
            NotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = true,
                timestamp = A_TIMESTAMP,
                senderDisambiguatedDisplayName = A_USER_NAME_2,
                senderId = A_USER_ID_2,
                body = "☎\uFE0F Incoming call",
                roomId = A_ROOM_ID,
                threadId = null,
                roomName = A_ROOM_NAME,
                canBeReplaced = false,
                isRedacted = false,
                imageUriString = null,
                type = EventType.CALL_NOTIFY,
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve RoomRedaction`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomRedaction(
                        AN_EVENT_ID_2,
                        A_REDACTION_REASON,
                    )
                )
            )
        )
        val expectedResult = ResolvedPushEvent.Redaction(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            redactedEventId = AN_EVENT_ID_2,
            reason = A_REDACTION_REASON,
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `resolve RoomRedaction with null redactedEventId should return null`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = NotificationContent.MessageLike.RoomRedaction(
                        null,
                        A_REDACTION_REASON,
                    )
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve null cases`() {
        testNull(NotificationContent.MessageLike.CallAnswer)
        testNull(NotificationContent.MessageLike.CallHangup)
        testNull(NotificationContent.MessageLike.CallCandidates)
        testNull(NotificationContent.MessageLike.KeyVerificationReady)
        testNull(NotificationContent.MessageLike.KeyVerificationStart)
        testNull(NotificationContent.MessageLike.KeyVerificationCancel)
        testNull(NotificationContent.MessageLike.KeyVerificationAccept)
        testNull(NotificationContent.MessageLike.KeyVerificationKey)
        testNull(NotificationContent.MessageLike.KeyVerificationMac)
        testNull(NotificationContent.MessageLike.KeyVerificationDone)
        testNull(NotificationContent.MessageLike.ReactionContent(relatedEventId = AN_EVENT_ID_2.value))
        testNull(NotificationContent.MessageLike.Sticker)
        testNull(NotificationContent.StateEvent.PolicyRuleRoom)
        testNull(NotificationContent.StateEvent.PolicyRuleServer)
        testNull(NotificationContent.StateEvent.PolicyRuleUser)
        testNull(NotificationContent.StateEvent.RoomAliases)
        testNull(NotificationContent.StateEvent.RoomAvatar)
        testNull(NotificationContent.StateEvent.RoomCanonicalAlias)
        testNull(NotificationContent.StateEvent.RoomCreate)
        testNull(NotificationContent.StateEvent.RoomEncryption)
        testNull(NotificationContent.StateEvent.RoomGuestAccess)
        testNull(NotificationContent.StateEvent.RoomHistoryVisibility)
        testNull(NotificationContent.StateEvent.RoomJoinRules)
        testNull(NotificationContent.StateEvent.RoomName)
        testNull(NotificationContent.StateEvent.RoomPinnedEvents)
        testNull(NotificationContent.StateEvent.RoomPowerLevels)
        testNull(NotificationContent.StateEvent.RoomServerAcl)
        testNull(NotificationContent.StateEvent.RoomThirdPartyInvite)
        testNull(NotificationContent.StateEvent.RoomTombstone)
        testNull(NotificationContent.StateEvent.RoomTopic)
        testNull(NotificationContent.StateEvent.SpaceChild)
        testNull(NotificationContent.StateEvent.SpaceParent)
    }

    private fun testNull(content: NotificationContent) = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                aNotificationData(
                    content = content
                )
            )
        )
        val result = sut.resolveEvent(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID)
        assertThat(result).isNull()
    }

    private fun createDefaultNotifiableEventResolver(
        notificationService: FakeNotificationService? = FakeNotificationService(),
        notificationResult: Result<NotificationData?> = Result.success(null),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ): DefaultNotifiableEventResolver {
        val context = RuntimeEnvironment.getApplication() as Context
        notificationService?.givenGetNotificationResult(notificationResult)
        val matrixClientProvider = FakeMatrixClientProvider(getClient = {
            if (notificationService == null) {
                Result.failure(IllegalStateException("Client not found"))
            } else {
                Result.success(FakeMatrixClient(notificationService = notificationService))
            }
        })
        val notificationMediaRepoFactory = NotificationMediaRepo.Factory {
            FakeNotificationMediaRepo()
        }
        return DefaultNotifiableEventResolver(
            stringProvider = AndroidStringProvider(context.resources),
            clock = FakeSystemClock(),
            matrixClientProvider = matrixClientProvider,
            notificationMediaRepoFactory = notificationMediaRepoFactory,
            context = context,
            permalinkParser = FakePermalinkParser(),
            callNotificationEventResolver = DefaultCallNotificationEventResolver(
                stringProvider = AndroidStringProvider(context.resources)
            ),
            appPreferencesStore = appPreferencesStore,
        )
    }
}
