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
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.push.impl.notifications.factories.createNotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.test.notifications.FakeImageLoader
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

private const val A_TIMESTAMP = 6480L
private const val A_ROOM_AVATAR = "mxc://roomAvatar"
private const val A_USER_AVATAR_1 = "mxc://userAvatar1"
private const val A_USER_AVATAR_2 = "mxc://userAvatar2"

@RunWith(RobolectricTestRunner::class)
class DefaultRoomGroupMessageCreatorTest {
    @Test
    fun `test createRoomMessage with one Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    imageUriString = "aUri",
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        assertThat(result.number).isEqualTo(1)
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_LOW)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP)
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `test createRoomMessage with one noisy Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    noisy = true,
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_DEFAULT)
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `test createRoomMessage with room avatar and sender avatar android O`() {
        `test createRoomMessage with room avatar and sender avatar`(
            api = Build.VERSION_CODES.O,
            // Only the Room avatar is loaded
            expectedCoilRequests = listOf(
                MediaRequestData(
                    source = MediaSource(url = A_ROOM_AVATAR),
                    kind = MediaRequestData.Kind.Thumbnail(1024)
                )
            )
        )
    }

    @Test
    fun `test createRoomMessage with room avatar and sender avatar android P`() = runTest {
        `test createRoomMessage with room avatar and sender avatar`(
            api = Build.VERSION_CODES.P,
            // Room and user avatar are loaded
            expectedCoilRequests = listOf(
                MediaRequestData(
                    source = MediaSource(url = A_USER_AVATAR_1),
                    kind = MediaRequestData.Kind.Thumbnail(1024)
                ),
                MediaRequestData(
                    source = MediaSource(url = A_USER_AVATAR_2),
                    kind = MediaRequestData.Kind.Thumbnail(1024)
                ),
                MediaRequestData(
                    source = MediaSource(url = A_ROOM_AVATAR),
                    kind = MediaRequestData.Kind.Thumbnail(1024)
                ),
            )
        )
    }

    private fun `test createRoomMessage with room avatar and sender avatar`(
        api: Int,
        expectedCoilRequests: List<Any>,
    ) = runTest {
        val fakeImageLoader = FakeImageLoader()
        val sut = createRoomGroupMessageCreator(
            sdkIntProvider = FakeBuildVersionSdkIntProvider(api)
        )
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(
                // Some user avatar
                avatarUrl = A_USER_AVATAR_1,
            ),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    roomAvatarPath = A_ROOM_AVATAR,
                    senderAvatarPath = A_USER_AVATAR_2,
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        assertThat(result.number).isEqualTo(1)
        assertThat(fakeImageLoader.getCoilRequests()).containsExactlyElementsIn(expectedCoilRequests)
    }

    @Test
    fun `test createRoomMessage with two Events`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP),
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP + 10),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        assertThat(result.number).isEqualTo(2)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP + 10)
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `test createRoomMessage with smart reply error`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    outGoingMessage = true,
                    outGoingMessageFailed = true,
                ),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        assertThat(result.actions).isNull()
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `test createRoomMessage for direct room`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            currentUser = aMatrixUser(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    roomIsDirect = true,
                ),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader.getImageLoader(),
            existingNotification = null,
        )
        assertThat(result.number).isEqualTo(1)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP)
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }
}

fun createRoomGroupMessageCreator(
    sdkIntProvider: BuildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.O),
): RoomGroupMessageCreator {
    val context = RuntimeEnvironment.getApplication() as Context
    val bitmapLoader = DefaultNotificationBitmapLoader(
        context = RuntimeEnvironment.getApplication(),
        sdkIntProvider = sdkIntProvider,
    )
    return DefaultRoomGroupMessageCreator(
        notificationCreator = createNotificationCreator(bitmapLoader = bitmapLoader),
        bitmapLoader = bitmapLoader,
        stringProvider = AndroidStringProvider(context.resources)
    )
}
