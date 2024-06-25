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

package io.element.android.libraries.push.impl.notifications.factories

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.impl.notifications.DefaultNotificationBitmapLoader
import io.element.android.libraries.push.impl.notifications.NotificationActionIds
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.channels.DefaultNotificationChannels
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.factories.action.AcceptInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.MarkAsReadActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.QuickReplyActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.RejectInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.libraries.push.test.notifications.FakeImageLoader
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultNotificationCreatorTest {
    @Test
    fun `test createDiagnosticNotification`() {
        val sut = createNotificationCreator()
        val result = sut.createDiagnosticNotification()
        result.commonAssertions(
            expectedGroup = null,
            expectedCategory = NotificationCompat.CATEGORY_STATUS,
        )
    }

    @Test
    fun `test createFallbackNotification`() {
        val sut = createNotificationCreator()
        val result = sut.createFallbackNotification(
            FallbackNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                description = "description",
                canBeReplaced = false,
                isRedacted = false,
                isUpdated = false,
                timestamp = A_FAKE_TIMESTAMP,
            )
        )
        result.commonAssertions(
            expectedCategory = null,
        )
    }

    @Test
    fun `test createSimpleEventNotification`() {
        val sut = createNotificationCreator()
        val result = sut.createSimpleEventNotification(
            SimpleNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = false,
                title = "title",
                description = "description",
                type = null,
                timestamp = A_FAKE_TIMESTAMP,
                soundName = null,
                canBeReplaced = false,
                isRedacted = false,
                isUpdated = false,
            )
        )
        result.commonAssertions(
            expectedCategory = null,
        )
    }

    @Test
    fun `test createSimpleEventNotification noisy`() {
        val sut = createNotificationCreator()
        val result = sut.createSimpleEventNotification(
            SimpleNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = true,
                title = "title",
                description = "description",
                type = null,
                timestamp = A_FAKE_TIMESTAMP,
                soundName = null,
                canBeReplaced = false,
                isRedacted = false,
                isUpdated = false,
            )
        )
        result.commonAssertions(
            expectedCategory = null,
        )
    }

    @Test
    fun `test createRoomInvitationNotification`() {
        val sut = createNotificationCreator()
        val result = sut.createRoomInvitationNotification(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = false,
                title = "title",
                description = "description",
                type = null,
                timestamp = A_FAKE_TIMESTAMP,
                soundName = null,
                canBeReplaced = false,
                isRedacted = false,
                isUpdated = false,
                roomName = "roomName",
            )
        )
        result.commonAssertions(
            expectedCategory = null,
        )
    }

    @Test
    fun `test createRoomInvitationNotification noisy`() {
        val sut = createNotificationCreator()
        val result = sut.createRoomInvitationNotification(
            InviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
                editedEventId = null,
                noisy = true,
                title = "title",
                description = "description",
                type = null,
                timestamp = A_FAKE_TIMESTAMP,
                soundName = null,
                canBeReplaced = false,
                isRedacted = false,
                isUpdated = false,
                roomName = "roomName",
            )
        )
        result.commonAssertions(
            expectedCategory = null,
        )
    }

    @Test
    fun `test createSummaryListNotification`() {
        val sut = createNotificationCreator()
        val matrixUser = aMatrixUser()
        val result = sut.createSummaryListNotification(
            currentUser = matrixUser,
            compatSummary = "compatSummary",
            noisy = false,
            lastMessageTimestamp = 123_456L,
        )
        result.commonAssertions(
            expectedGroup = matrixUser.userId.value,
        )
    }

    @Test
    fun `test createSummaryListNotification noisy`() {
        val sut = createNotificationCreator()
        val matrixUser = aMatrixUser()
        val result = sut.createSummaryListNotification(
            currentUser = matrixUser,
            compatSummary = "compatSummary",
            noisy = true,
            lastMessageTimestamp = 123_456L,
        )
        result.commonAssertions(
            expectedGroup = matrixUser.userId.value,
        )
    }

    @Test
    fun `test createMessagesListNotification`() = runTest {
        val sut = createNotificationCreator()
        aMatrixUser()
        val result = sut.createMessagesListNotification(
            roomInfo = RoomEventGroupInfo(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                roomDisplayName = "roomDisplayName",
                isDirect = false,
                hasSmartReplyError = false,
                shouldBing = false,
                customSound = null,
                isUpdated = false,
            ),
            threadId = null,
            largeIcon = null,
            lastMessageTimestamp = 123_456L,
            tickerText = "tickerText",
            currentUser = aMatrixUser(),
            existingNotification = null,
            imageLoader = FakeImageLoader().getImageLoader(),
            events = emptyList(),
        )
        result.commonAssertions()
    }

    @Test
    fun `test createMessagesListNotification should bing and thread`() = runTest {
        val sut = createNotificationCreator()
        aMatrixUser()
        val result = sut.createMessagesListNotification(
            roomInfo = RoomEventGroupInfo(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                roomDisplayName = "roomDisplayName",
                isDirect = false,
                hasSmartReplyError = false,
                shouldBing = true,
                customSound = null,
                isUpdated = false,
            ),
            threadId = A_THREAD_ID,
            largeIcon = null,
            lastMessageTimestamp = 123_456L,
            tickerText = "tickerText",
            currentUser = aMatrixUser(),
            existingNotification = null,
            imageLoader = FakeImageLoader().getImageLoader(),
            events = emptyList(),
        )
        result.commonAssertions()
    }

    private fun Notification.commonAssertions(
        expectedGroup: String? = A_SESSION_ID.value,
        expectedCategory: String? = NotificationCompat.CATEGORY_MESSAGE,
    ) {
        assertThat(contentIntent).isNotNull()
        assertThat(group).isEqualTo(expectedGroup)
        assertThat(category).isEqualTo(expectedCategory)
    }
}

fun createNotificationCreator(
    context: Context = RuntimeEnvironment.getApplication(),
    buildMeta: BuildMeta = aBuildMeta(),
    notificationChannels: NotificationChannels = createNotificationChannels(),
    bitmapLoader: NotificationBitmapLoader = DefaultNotificationBitmapLoader(context, FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.R)),
): NotificationCreator {
    return DefaultNotificationCreator(
        context = context,
        notificationChannels = notificationChannels,
        stringProvider = FakeStringProvider("test"),
        buildMeta = buildMeta,
        pendingIntentFactory = PendingIntentFactory(
            context,
            FakeIntentProvider(),
            FakeSystemClock(),
            NotificationActionIds(buildMeta),
        ),
        markAsReadActionFactory = MarkAsReadActionFactory(
            context = context,
            actionIds = NotificationActionIds(buildMeta),
            stringProvider = FakeStringProvider("MarkAsReadActionFactory"),
            clock = FakeSystemClock(),
        ),
        quickReplyActionFactory = QuickReplyActionFactory(
            context = context,
            actionIds = NotificationActionIds(buildMeta),
            stringProvider = FakeStringProvider("QuickReplyActionFactory"),
            clock = FakeSystemClock(),
        ),
        bitmapLoader = bitmapLoader,
        acceptInvitationActionFactory = AcceptInvitationActionFactory(
            context = context,
            actionIds = NotificationActionIds(buildMeta),
            stringProvider = FakeStringProvider("AcceptInvitationActionFactory"),
            clock = FakeSystemClock(),
        ),
        rejectInvitationActionFactory = RejectInvitationActionFactory(
            context = context,
            actionIds = NotificationActionIds(buildMeta),
            stringProvider = FakeStringProvider("RejectInvitationActionFactory"),
            clock = FakeSystemClock(),
        ),
    )
}

fun createNotificationChannels(): NotificationChannels {
    val context = RuntimeEnvironment.getApplication()
    return DefaultNotificationChannels(context, NotificationManagerCompat.from(context), FakeStringProvider(""))
}
