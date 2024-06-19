/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.call.notifications

import androidx.core.graphics.drawable.IconCompat
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RingingCallNotificationCreatorTest {
    @Test
    fun `createNotification - with no associated MatrixClient does nothing`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.failure(IllegalStateException("No client found")) })
        )

        val result = notificationCreator.createTestNotification()

        assertThat(result).isNull()
    }

    @Test
    fun `createNotification - creates a valid notification`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) })
        )

        val result = notificationCreator.createTestNotification()

        assertThat(result).isNotNull()
    }

    @Test
    fun `createNotification - tries to load the avatar URL`() = runTest {
        val getUserIconLambda = lambdaRecorder<String?, ImageLoader, IconCompat?> { _, _ -> null }
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
            notificationBitmapLoader = FakeNotificationBitmapLoader(getUserIconResult = getUserIconLambda)
        )

        notificationCreator.createTestNotification()

        getUserIconLambda.assertions().isCalledOnce()
    }

    private suspend fun RingingCallNotificationCreator.createTestNotification() = createNotification(
        sessionId = A_SESSION_ID,
        roomId = A_ROOM_ID,
        eventId = AN_EVENT_ID,
        senderId = A_USER_ID_2,
        roomName = "Room",
        senderDisplayName = "Johnnie Murphy",
        roomAvatarUrl = "https://example.com/avatar.jpg",
        notificationChannelId = "channelId",
        timestamp = 0L,
    )

    private fun createRingingCallNotificationCreator(
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
        imageLoaderHolder: FakeImageLoaderHolder = FakeImageLoaderHolder(),
        notificationBitmapLoader: FakeNotificationBitmapLoader = FakeNotificationBitmapLoader(),
    ) = RingingCallNotificationCreator(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        matrixClientProvider = matrixClientProvider,
        imageLoaderHolder = imageLoaderHolder,
        notificationBitmapLoader = notificationBitmapLoader,
    )
}
