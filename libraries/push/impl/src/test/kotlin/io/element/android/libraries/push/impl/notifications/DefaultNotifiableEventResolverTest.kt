/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
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
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.test.notifications.FakeCallNotificationEventResolver
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
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
        val result = sut.resolveEvents(A_SESSION_ID, listOf(NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `resolve event failure`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.failure(AN_EXCEPTION)
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)?.isFailure).isTrue()
    }

    @Test
    fun `resolve event message text`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = TextMessageType(body = "Hello world", formatted = null)
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    @Config(qualifiers = "en")
    fun `resolve event message with mention`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = TextMessageType(body = "Hello world", formatted = null)
                        ),
                        hasMention = true,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world", hasMentionOrReply = true)
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve HTML formatted event message text takes plain text version`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
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
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve incorrectly formatted event message text uses fallback`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
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
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Hello world")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message audio`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = AudioMessageType("Audio", null, null, MediaSource("url"), null)
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Audio")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message video`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = VideoMessageType("Video", null, null, MediaSource("url"), null)
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Video")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message voice`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = VoiceMessageType("Voice", null, null, MediaSource("url"), null, null)
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Voice message")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message image`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = ImageMessageType("Image", null, null, MediaSource("url"), null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Image")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message sticker`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                    mapOf(
                        AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = StickerMessageType("Sticker", null, null, MediaSource("url"), null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Sticker")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message file`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = FileMessageType("File", null, null, MediaSource("url"), null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "File")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message location`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = LocationMessageType("Location", "geo:1,2", null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Location")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message notice`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = NoticeMessageType("Notice", null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Notice")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve event message emote`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomMessage(
                            senderId = A_USER_ID_2,
                            messageType = EmoteMessageType("is happy", null),
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "* Bob is happy")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve poll`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.Poll(
                            senderId = A_USER_ID_2,
                            question = "A question"
                        ),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            aNotifiableMessageEvent(body = "Poll: A question")
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve RoomMemberContent invite room`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.StateEvent.RoomMemberContent(
                            userId = A_USER_ID_2,
                            membershipState = RoomMembershipState.INVITE
                        ),
                        isDirect = false,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)?.getOrNull()).isNull()
    }

    @Test
    fun `resolve invite room`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.Invite(
                            senderId = A_USER_ID_2,
                        ),
                        isDirect = false,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
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
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve invite direct`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.Invite(
                            senderId = A_USER_ID_2,
                        ),
                        isDirect = true,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
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
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve invite direct, no display name`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.Invite(
                            senderId = A_USER_ID_2,
                        ),
                        isDirect = true,
                        senderDisplayName = null,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
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
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve invite direct, ambiguous display name`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.Invite(
                            senderId = A_USER_ID_2,
                        ),
                        isDirect = false,
                        senderIsNameAmbiguous = true,
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
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
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve RoomMemberContent other`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.StateEvent.RoomMemberContent(
                            userId = A_USER_ID_2,
                            membershipState = RoomMembershipState.JOIN
                        )
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)?.getOrNull()).isNull()
    }

    @Test
    fun `resolve RoomEncrypted`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(AN_EVENT_ID to aNotificationData(content = NotificationContent.MessageLike.RoomEncrypted))
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            FallbackNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                description = "You have new messages.",
                canBeReplaced = true,
                isRedacted = false,
                isUpdated = false,
                timestamp = A_FAKE_TIMESTAMP,
            )
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve UnableToResolve`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(AN_EVENT_ID to aNotificationData(content = NotificationContent.MessageLike.UnableToResolve))
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        val expectedResult = ResolvedPushEvent.Event(
            FallbackNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                description = "You have new messages.",
                canBeReplaced = true,
                isRedacted = false,
                isUpdated = false,
                timestamp = A_FAKE_TIMESTAMP,
            )
        )
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve CallInvite`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.CallInvite(A_USER_ID_2),
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
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
                body = "Unsupported call",
                imageUriString = null,
                imageMimeType = null,
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
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve CallNotify - goes through CallNotificationEventResolver`() = runTest {
        val callNotificationEventResolver = FakeCallNotificationEventResolver()
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.CallNotify(
                            A_USER_ID_2,
                            CallNotifyType.NOTIFY
                        ),
                    )
                )
            ),
            callNotificationEventResolver = callNotificationEventResolver,
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
                body = "📹 Incoming call",
                roomId = A_ROOM_ID,
                threadId = null,
                roomName = A_ROOM_NAME,
                canBeReplaced = false,
                isRedacted = false,
                imageUriString = null,
                imageMimeType = null,
                type = EventType.CALL_NOTIFY,
            )
        )
        callNotificationEventResolver.resolveEventLambda = { _, _, _ -> Result.success(expectedResult.notifiableEvent) }
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve RoomRedaction`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomRedaction(
                            AN_EVENT_ID_2,
                            A_REDACTION_REASON,
                        )
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
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)).isEqualTo(Result.success(expectedResult))
    }

    @Test
    fun `resolve RoomRedaction with null redactedEventId should return null`() = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(
                    AN_EVENT_ID to aNotificationData(
                        content = NotificationContent.MessageLike.RoomRedaction(
                            null,
                            A_REDACTION_REASON,
                        )
                    )
                )
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)?.getOrNull()).isNull()
    }

    @Test
    fun `resolve null cases`() {
        testNoResults(NotificationContent.MessageLike.CallAnswer)
        testNoResults(NotificationContent.MessageLike.CallHangup)
        testNoResults(NotificationContent.MessageLike.CallCandidates)
        testNoResults(NotificationContent.MessageLike.KeyVerificationReady)
        testNoResults(NotificationContent.MessageLike.KeyVerificationStart)
        testNoResults(NotificationContent.MessageLike.KeyVerificationCancel)
        testNoResults(NotificationContent.MessageLike.KeyVerificationAccept)
        testNoResults(NotificationContent.MessageLike.KeyVerificationKey)
        testNoResults(NotificationContent.MessageLike.KeyVerificationMac)
        testNoResults(NotificationContent.MessageLike.KeyVerificationDone)
        testNoResults(NotificationContent.MessageLike.ReactionContent(relatedEventId = AN_EVENT_ID_2.value))
        testNoResults(NotificationContent.MessageLike.Sticker)
        testNoResults(NotificationContent.StateEvent.PolicyRuleRoom)
        testNoResults(NotificationContent.StateEvent.PolicyRuleServer)
        testNoResults(NotificationContent.StateEvent.PolicyRuleUser)
        testNoResults(NotificationContent.StateEvent.RoomAliases)
        testNoResults(NotificationContent.StateEvent.RoomAvatar)
        testNoResults(NotificationContent.StateEvent.RoomCanonicalAlias)
        testNoResults(NotificationContent.StateEvent.RoomCreate)
        testNoResults(NotificationContent.StateEvent.RoomEncryption)
        testNoResults(NotificationContent.StateEvent.RoomGuestAccess)
        testNoResults(NotificationContent.StateEvent.RoomHistoryVisibility)
        testNoResults(NotificationContent.StateEvent.RoomJoinRules)
        testNoResults(NotificationContent.StateEvent.RoomName)
        testNoResults(NotificationContent.StateEvent.RoomPinnedEvents)
        testNoResults(NotificationContent.StateEvent.RoomPowerLevels)
        testNoResults(NotificationContent.StateEvent.RoomServerAcl)
        testNoResults(NotificationContent.StateEvent.RoomThirdPartyInvite)
        testNoResults(NotificationContent.StateEvent.RoomTombstone)
        testNoResults(NotificationContent.StateEvent.RoomTopic(""))
        testNoResults(NotificationContent.StateEvent.SpaceChild)
        testNoResults(NotificationContent.StateEvent.SpaceParent)
    }

    private fun testNoResults(content: NotificationContent) = runTest {
        val sut = createDefaultNotifiableEventResolver(
            notificationResult = Result.success(
                mapOf(AN_EVENT_ID to aNotificationData(content = content))
            )
        )
        val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, "firebase")
        val result = sut.resolveEvents(A_SESSION_ID, listOf(request))
        assertThat(result.getEvent(request)?.getOrNull()).isNull()
    }

    private fun Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>.getEvent(
        request: NotificationEventRequest
    ): Result<ResolvedPushEvent>? {
        return getOrNull()?.get(request)
    }

    private fun createDefaultNotifiableEventResolver(
        notificationService: FakeNotificationService? = FakeNotificationService(),
        notificationResult: Result<Map<EventId, NotificationData>> = Result.success(emptyMap()),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        callNotificationEventResolver: FakeCallNotificationEventResolver = FakeCallNotificationEventResolver(),
    ): DefaultNotifiableEventResolver {
        val context = RuntimeEnvironment.getApplication() as Context
        notificationService?.givenGetNotificationsResult(notificationResult)
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
            callNotificationEventResolver = callNotificationEventResolver,
            appPreferencesStore = appPreferencesStore,
        )
    }
}
