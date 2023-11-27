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

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.push.impl.notifications.factories.createNotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

private const val A_TIMESTAMP = 6480L

@RunWith(RobolectricTestRunner::class)
class RoomGroupMessageCreatorTest {
    @Test
    fun `test createRoomMessage with one Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    imageUriString = "aUri",
                )
            ),
            roomId = A_ROOM_ID,
        )
        val resultMetaWithoutFormatting = result.meta.copy(
            summaryLine = result.meta.summaryLine.toString()
        )
        assertThat(resultMetaWithoutFormatting).isEqualTo(
            RoomNotification.Message.Meta(
                roomId = A_ROOM_ID,
                summaryLine = "room-name:  message-body",
                messageCount = 1,
                latestTimestamp = A_TIMESTAMP,
                shouldBing = false,
            )
        )
    }

    @Test
    fun `test createRoomMessage with one noisy Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    noisy = true,
                )
            ),
            roomId = A_ROOM_ID,
        )
        val resultMetaWithoutFormatting = result.meta.copy(
            summaryLine = result.meta.summaryLine.toString()
        )
        assertThat(resultMetaWithoutFormatting).isEqualTo(
            RoomNotification.Message.Meta(
                roomId = A_ROOM_ID,
                summaryLine = "room-name:  message-body",
                messageCount = 1,
                latestTimestamp = A_TIMESTAMP,
                shouldBing = true,
            )
        )
    }

    @Test
    fun `test createRoomMessage with two Events`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP),
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP + 10),
            ),
            roomId = A_ROOM_ID,
        )
        val resultMetaWithoutFormatting = result.meta.copy(
            summaryLine = result.meta.summaryLine.toString()
        )
        assertThat(resultMetaWithoutFormatting).isEqualTo(
            RoomNotification.Message.Meta(
                roomId = A_ROOM_ID,
                summaryLine = "room-name: 2 messages",
                messageCount = 2,
                latestTimestamp = A_TIMESTAMP + 10,
                shouldBing = false,
            )
        )
    }

    @Test
    fun `test createRoomMessage with smart reply error`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    outGoingMessage = true,
                    outGoingMessageFailed = true,
                ),
            ),
            roomId = A_ROOM_ID,
        )
        val resultMetaWithoutFormatting = result.meta.copy(
            summaryLine = result.meta.summaryLine.toString()
        )
        assertThat(resultMetaWithoutFormatting).isEqualTo(
            RoomNotification.Message.Meta(
                roomId = A_ROOM_ID,
                summaryLine = "room-name:  message-body",
                messageCount = 0,
                latestTimestamp = A_TIMESTAMP,
                shouldBing = false,
            )
        )
    }

    @Test
    fun `test createRoomMessage for direct room`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    roomIsDirect = true,
                ),
            ),
            roomId = A_ROOM_ID,
        )
        val resultMetaWithoutFormatting = result.meta.copy(
            summaryLine = result.meta.summaryLine.toString()
        )
        assertThat(resultMetaWithoutFormatting).isEqualTo(
            RoomNotification.Message.Meta(
                roomId = A_ROOM_ID,
                summaryLine = "sender-name: message-body",
                messageCount = 1,
                latestTimestamp = A_TIMESTAMP,
                shouldBing = false,
            )
        )
    }
}

fun createRoomGroupMessageCreator(): RoomGroupMessageCreator {
    val context = RuntimeEnvironment.getApplication() as Context
    return RoomGroupMessageCreator(
        notificationCreator = createNotificationCreator(),
        bitmapLoader = NotificationBitmapLoader(RuntimeEnvironment.getApplication()),
        stringProvider = AndroidStringProvider(context.resources)
    )
}
