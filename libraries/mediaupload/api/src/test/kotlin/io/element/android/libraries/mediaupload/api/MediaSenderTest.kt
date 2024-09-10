/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaSenderTest {
    @Test
    fun `given an attachment when sending it the preprocessor always runs`() = runTest {
        val preProcessor = FakeMediaPreProcessor()
        val sender = aMediaSender(preProcessor)

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, compressIfPossible = true)

        assertThat(preProcessor.processCallCount).isEqualTo(1)
    }

    @Test
    fun `given an attachment when sending it the MatrixRoom will call sendMedia`() = runTest {
        val sendMediaResult = lambdaRecorder<ProgressCallback?, Result<FakeMediaUploadHandler>> {
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            sendMediaResult = sendMediaResult
        )
        val sender = aMediaSender(room = room)

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, compressIfPossible = true)
        sendMediaResult.assertions().isCalledOnce()
    }

    @Test
    fun `given a failure in the preprocessor when sending the whole process fails`() = runTest {
        val preProcessor = FakeMediaPreProcessor().apply {
            givenResult(Result.failure(Exception()))
        }
        val sender = aMediaSender(preProcessor)

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, compressIfPossible = true)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `given a failure in the media upload when sending the whole process fails`() = runTest {
        val room = FakeMatrixRoom(
            sendMediaResult = { Result.failure(Exception()) }
        )
        val sender = aMediaSender(room = room)

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, compressIfPossible = true)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given a cancellation in the media upload when sending the job is cancelled`() = runTest(StandardTestDispatcher()) {
        val room = FakeMatrixRoom(
            sendMediaResult = { Result.success(FakeMediaUploadHandler()) }
        )
        val sender = aMediaSender(room = room)
        val sendJob = launch {
            val uri = Uri.parse("content://image.jpg")
            sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, compressIfPossible = true)
        }
        // Wait until several internal tasks run and the file is being uploaded
        advanceTimeBy(3L)

        // Assert the file is being uploaded
        assertThat(sender.hasOngoingMediaUploads).isTrue()

        // Cancel the coroutine
        sendJob.cancel()

        // Wait for the coroutine cleanup to happen
        advanceTimeBy(1L)

        // Assert the file is not being uploaded anymore
        assertThat(sender.hasOngoingMediaUploads).isFalse()
    }

    private fun aMediaSender(
        preProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        room: MatrixRoom = FakeMatrixRoom(),
    ) = MediaSender(
        preProcessor,
        room,
    )
}
