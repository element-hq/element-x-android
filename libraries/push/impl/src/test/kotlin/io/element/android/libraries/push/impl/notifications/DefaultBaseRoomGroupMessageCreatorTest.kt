/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_TIMESTAMP
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.media.AVATAR_THUMBNAIL_SIZE_IN_PIXEL
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoader
import io.element.android.libraries.matrix.ui.media.test.FakeInitialsAvatarBitmapGenerator
import io.element.android.libraries.push.impl.notifications.factories.MARK_AS_READ_ACTION_TITLE
import io.element.android.libraries.push.impl.notifications.factories.QUICK_REPLY_ACTION_TITLE
import io.element.android.libraries.push.impl.notifications.factories.aNotificationAccountParams
import io.element.android.libraries.push.impl.notifications.factories.createNotificationCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

private const val A_ROOM_AVATAR = "mxc://roomAvatar"
private const val A_USER_AVATAR_1 = "mxc://userAvatar1"
private const val A_USER_AVATAR_2 = "mxc://userAvatar2"

@RunWith(RobolectricTestRunner::class)
class DefaultBaseRoomGroupMessageCreatorTest {
    @Test
    fun `test createRoomMessage with one Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            notificationAccountParams = aNotificationAccountParams(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    imageUriString = "aUri",
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        assertThat(result.number).isEqualTo(1)
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_LOW)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP)
        assertThat(fakeImageLoader.getExecutedRequestsData()).isEmpty()
    }

    @Test
    fun `test createRoomMessage with one noisy Event`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            notificationAccountParams = aNotificationAccountParams(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    noisy = true,
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_DEFAULT)
        assertThat(fakeImageLoader.getExecutedRequestsData()).isEmpty()
    }

    @Test
    fun `test createRoomMessage with room avatar and sender avatar android O`() {
        `test createRoomMessage with room avatar and sender avatar`(
            api = Build.VERSION_CODES.O,
            // Only the Room avatar is loaded
            expectedCoilRequests = listOf(
                MediaRequestData(
                    source = MediaSource(url = A_ROOM_AVATAR),
                    kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
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
                    kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
                ),
                MediaRequestData(
                    source = MediaSource(url = A_USER_AVATAR_2),
                    kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
                ),
                MediaRequestData(
                    source = MediaSource(url = A_ROOM_AVATAR),
                    kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
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
            notificationAccountParams = aNotificationAccountParams(
                user = aMatrixUser(
                    // Some user avatar
                    avatarUrl = A_USER_AVATAR_1,
                )
            ),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    roomAvatarPath = A_ROOM_AVATAR,
                    senderAvatarPath = A_USER_AVATAR_2,
                )
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        assertThat(result.number).isEqualTo(1)
        assertThat(fakeImageLoader.getExecutedRequestsData()).containsExactlyElementsIn(expectedCoilRequests)
    }

    @Test
    fun `test createRoomMessage with two Events`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            notificationAccountParams = aNotificationAccountParams(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP),
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP + 10),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        assertThat(result.number).isEqualTo(2)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP + 10)
        val actionTitles = result.actions?.map { it.title }
        assertThat(actionTitles).isEqualTo(
            listOfNotNull(
                MARK_AS_READ_ACTION_TITLE.takeIf { NotificationConfig.SHOW_MARK_AS_READ_ACTION },
                QUICK_REPLY_ACTION_TITLE.takeIf { NotificationConfig.SHOW_QUICK_REPLY_ACTION },
            )
        )
        assertThat(fakeImageLoader.getExecutedRequestsData()).isEmpty()
    }

    @Test
    fun `test createRoomMessage with smart reply error`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            notificationAccountParams = aNotificationAccountParams(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    outGoingMessage = true,
                    outGoingMessageFailed = true,
                ),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        val actionTitles = result.actions?.map { it.title }
        assertThat(actionTitles).isEqualTo(
            listOfNotNull(
                MARK_AS_READ_ACTION_TITLE.takeIf { NotificationConfig.SHOW_MARK_AS_READ_ACTION }
            )
        )
        assertThat(fakeImageLoader.getExecutedRequestsData()).isEmpty()
    }

    @Test
    fun `test createRoomMessage for DM`() = runTest {
        val sut = createRoomGroupMessageCreator()
        val fakeImageLoader = FakeImageLoader()
        val result = sut.createRoomMessage(
            notificationAccountParams = aNotificationAccountParams(),
            events = listOf(
                aNotifiableMessageEvent(timestamp = A_TIMESTAMP).copy(
                    roomIsDm = true,
                ),
            ),
            roomId = A_ROOM_ID,
            imageLoader = fakeImageLoader,
            existingNotification = null,
            threadId = null,
        )
        assertThat(result.number).isEqualTo(1)
        assertThat(result.`when`).isEqualTo(A_TIMESTAMP)
        assertThat(fakeImageLoader.getExecutedRequestsData()).isEmpty()
    }
}

fun createRoomGroupMessageCreator(
    sdkIntProvider: BuildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.O),
): RoomGroupMessageCreator {
    val context = RuntimeEnvironment.getApplication() as Context
    val bitmapLoader = DefaultNotificationBitmapLoader(
        context = RuntimeEnvironment.getApplication(),
        sdkIntProvider = sdkIntProvider,
        initialsAvatarBitmapGenerator = FakeInitialsAvatarBitmapGenerator(),
    )
    return DefaultRoomGroupMessageCreator(
        notificationCreator = createNotificationCreator(bitmapLoader = bitmapLoader),
        bitmapLoader = bitmapLoader,
        stringProvider = AndroidStringProvider(context.resources)
    )
}
