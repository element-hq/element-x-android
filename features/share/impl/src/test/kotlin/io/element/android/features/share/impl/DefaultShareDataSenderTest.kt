/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.features.share.api.OnSharedData
import io.element.android.features.share.api.ShareIntentData
import io.element.android.features.share.api.UriToShare
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import io.element.android.libraries.mediaupload.api.MediaSenderRoomFactory
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.test.FakeMediaSender
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.appnavstate.impl.DefaultActiveRoomsHolder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultShareDataSenderTest : RobolectricTest() {
    @Test
    fun `send text ok`() = runTest {
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendMessageLambda = { _, _, _, _, _ -> Result.success(Unit) }
            },
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, joinedRoom)
        }
        val onSharedData = lambdaRecorder<ShareIntentData, Unit> { }
        val sender = createDefaultShareDataSender(
            onSharedData = { onSharedData(it) },
        )
        val result = sender.send(matrixClient, ShareIntentData.PlainText(A_MESSAGE), listOf(A_ROOM_ID))
        assertThat(result.isSuccess).isTrue()
        onSharedData.assertions().isCalledOnce()
    }

    @Test
    fun `send text, room not found`() = runTest {
        val onSharedData = lambdaRecorder<ShareIntentData, Unit> { }
        val sender = createDefaultShareDataSender(
            onSharedData = { onSharedData(it) },
        )
        val result = sender.send(FakeMatrixClient(), ShareIntentData.PlainText(A_MESSAGE), listOf(A_ROOM_ID))
        assertThat(result.isFailure).isTrue()
        onSharedData.assertions().isCalledOnce()
    }

    @Test
    fun `send media ok`() = runTest {
        val sendMediaResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline(),
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, joinedRoom)
        }
        val mediaSender = FakeMediaSender(
            sendMediaResult = sendMediaResult,
        )
        val sender = createDefaultShareDataSender(
            mediaSenderRoomFactory = { _, _ -> mediaSender },
        )
        val result = sender.send(
            client = matrixClient,
            shareIntentData = ShareIntentData.Uris(
                text = A_MESSAGE,
                listOf(
                    UriToShare(
                        uri = Uri.parse("content://image.jpg"),
                        mimeType = MimeTypes.Jpeg,
                    )
                )
            ),
            roomIds = listOf(A_ROOM_ID),
        )
        assertThat(result.isSuccess).isTrue()
        sendMediaResult.assertions().isCalledOnce()
    }

    @Test
    fun `send media with no uris is a failure`() = runTest {
        val sender = createDefaultShareDataSender()
        val result = sender.send(
            client = FakeMatrixClient(),
            shareIntentData = ShareIntentData.Uris(text = A_MESSAGE, emptyList()),
            roomIds = listOf(A_ROOM_ID),
        )
        assertThat(result.isFailure).isTrue()
    }
}

private fun createDefaultShareDataSender(
    mediaSenderRoomFactory: MediaSenderRoomFactory = MediaSenderRoomFactory { _, _ -> FakeMediaSender() },
    mediaOptimizationConfigProviderFactory: MediaOptimizationConfigProviderFactory = MediaOptimizationConfigProviderFactory {
        FakeMediaOptimizationConfigProvider()
    },
    activeRoomsHolder: ActiveRoomsHolder = DefaultActiveRoomsHolder(),
    onSharedData: OnSharedData = OnSharedData {},
) = DefaultShareDataSender(
    mediaSenderRoomFactory = mediaSenderRoomFactory,
    mediaOptimizationConfigProviderFactory = mediaOptimizationConfigProviderFactory,
    activeRoomsHolder = activeRoomsHolder,
    onSharedData = onSharedData,
)
