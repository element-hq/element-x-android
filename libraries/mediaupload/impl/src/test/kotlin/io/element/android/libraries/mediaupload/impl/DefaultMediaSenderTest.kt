/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DefaultMediaSenderTest {
    private val mediaOptimizationConfig = MediaOptimizationConfig(
        compressImages = true,
        videoCompressionPreset = VideoCompressionPreset.STANDARD,
    )

    @Test
    fun `given an attachment when sending it the preprocessor always runs`() = runTest {
        val preProcessor = FakeMediaPreProcessor()
        val sender = createDefaultMediaSender(
            preProcessor = preProcessor,
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = lambdaRecorder<
                        File,
                        FileInfo,
                        String?,
                        String?,
                        EventId?,
                        Result<FakeMediaUploadHandler>,
                        > { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                },
            )
        )

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(preProcessor.processCallCount).isEqualTo(1)
    }

    @Test
    fun `given an attachment when sending it the Room will call sendMedia`() = runTest {
        val sendImageResult =
            lambdaRecorder { _: File, _: File?, _: ImageInfo, _: String?, _: String?, _: EventId? ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendImageLambda = sendImageResult
            },
        )
        val sender = createDefaultMediaSender(room = room)

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)
    }

    @Test
    fun `given a failure in the preprocessor when sending the whole process fails`() = runTest {
        val preProcessor = FakeMediaPreProcessor().apply {
            givenResult(Result.failure(Exception()))
        }
        val sender = createDefaultMediaSender(preProcessor)

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `given a failure in the media upload when sending the whole process fails`() = runTest {
        val preProcessor = FakeMediaPreProcessor().apply {
            givenImageResult()
        }
        val sendImageResult =
            lambdaRecorder { _: File, _: File?, _: ImageInfo, _: String?, _: String?, _: EventId? ->
                Result.failure<FakeMediaUploadHandler>(Exception())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendImageLambda = sendImageResult
            },
        )
        val sender = createDefaultMediaSender(
            preProcessor = preProcessor,
            room = room,
        )

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given a cancellation in the media upload when sending the job is cancelled`() = runTest(StandardTestDispatcher()) {
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val sender = createDefaultMediaSender(room = room)
        val sendJob = launch {
            val uri = Uri.parse("content://image.jpg")
            sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)
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
        sendFileResult.assertions().isCalledOnce()
    }

    private fun createDefaultMediaSender(
        preProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        room: JoinedRoom = FakeJoinedRoom(),
        mediaOptimizationConfigProvider: MediaOptimizationConfigProvider = MediaOptimizationConfigProvider { mediaOptimizationConfig },
    ) = DefaultMediaSender(
        preProcessor = preProcessor,
        room = room,
        timelineMode = Timeline.Mode.Live,
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
    )
}
