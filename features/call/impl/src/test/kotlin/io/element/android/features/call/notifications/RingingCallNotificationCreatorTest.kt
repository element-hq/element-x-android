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
import io.element.android.features.call.impl.ui.IncomingCallActivity
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
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.Shadows.shadowOf

class RingingCallNotificationCreatorTest : RobolectricTest() {
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

    @Test
    fun `createNotification - use the correct style for video call`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
        )

        val notification = notificationCreator.createTestNotification()
        assertThat(notification?.category).isEqualTo("call")

        val acceptAction = notification?.actions?.get(1)
        assertThat(acceptAction?.title?.toString()).isEqualTo("Video")
    }

    @Test
    fun `createNotification - use the correct style for audio call`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
        )

        val notification = notificationCreator.createTestNotification(audioOnly = true)
        assertThat(notification?.category).isEqualTo("call")

        val acceptAction = notification?.actions?.get(1)
        assertThat(acceptAction?.title?.toString()).isEqualTo("Answer")
    }

    @Test
    fun `createNotification - the answer action goes through IncomingCallActivity, asking it to answer straight away`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
        )

        val notification = notificationCreator.createTestNotification()

        // Not ElementCallActivity: naming the WebView activity here made the Answer button open the
        // WebView call whatever FeatureFlags.NativeCall was set to.
        val answerIntent = shadowOf(notification?.actions?.get(1)?.actionIntent).savedIntent
        assertThat(answerIntent.component?.className).isEqualTo(IncomingCallActivity::class.java.name)
        assertThat(answerIntent.getBooleanExtra(IncomingCallActivity.EXTRA_ANSWER_IMMEDIATELY, false)).isTrue()
        assertThat(answerIntent.hasExtra(IncomingCallActivity.EXTRA_NOTIFICATION_DATA)).isTrue()
    }

    @Test
    fun `createNotification - tapping the notification body answers too`() = runTest {
        val notificationCreator = createRingingCallNotificationCreator(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
        )

        val notification = notificationCreator.createTestNotification()

        val contentIntent = shadowOf(notification?.contentIntent).savedIntent
        assertThat(contentIntent.component?.className).isEqualTo(IncomingCallActivity::class.java.name)
        assertThat(contentIntent.getBooleanExtra(IncomingCallActivity.EXTRA_ANSWER_IMMEDIATELY, false)).isTrue()
    }

    private suspend fun RingingCallNotificationCreator.createTestNotification(audioOnly: Boolean = false) = createNotification(
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
        audioOnly = audioOnly
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
