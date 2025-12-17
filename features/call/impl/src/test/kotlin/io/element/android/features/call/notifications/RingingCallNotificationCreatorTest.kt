/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.notifications

import androidx.core.graphics.drawable.IconCompat
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
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
        val getUserIconLambda = lambdaRecorder<AvatarData, ImageLoader, IconCompat?> { _, _ -> null }
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
        expirationTimestamp = 20L,
        textContent = "textContent",
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
