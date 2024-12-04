/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.attachments

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewEvents
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewPresenter
import io.element.android.features.messages.impl.attachments.preview.OnDoneListener
import io.element.android.features.messages.impl.attachments.preview.SendActionState
import io.element.android.features.messages.impl.fixtures.aMediaAttachment
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.A_CAPTION
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.fake.FakeTemporaryUriDeleter
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AttachmentsPreviewPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `present - initial state`() = runTest {
        createAttachmentsPreviewPresenter().test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(initialState.allowCaption).isTrue()
            assertThat(initialState.showCaptionCompatibilityWarning).isTrue()
        }
    }

    @Test
    fun `present - initial state no caption warning`() = runTest {
        createAttachmentsPreviewPresenter(
            showCaptionCompatibilityWarning = false,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.showCaptionCompatibilityWarning).isFalse()
        }
    }

    @Test
    fun `present - initial state - caption not allowed`() = runTest {
        createAttachmentsPreviewPresenter(
            allowCaption = false,
        ).test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(initialState.allowCaption).isFalse()
        }
    }

    @Test
    fun `present - send media success scenario`() = runTest {
        val sendFileResult = lambdaRecorder<File, FileInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            progressCallbackValues = listOf(
                Pair(0, 10),
                Pair(5, 10),
                Pair(10, 10)
            ),
            sendFileResult = sendFileResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(0f))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(0.5f))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(1f))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media after pre-processing success scenario`() = runTest {
        val sendFileResult = lambdaRecorder<File, FileInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            sendFileResult = sendFileResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor(
                processLatch = processLatch,
            ),
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            // Pre-processing finishes
            processLatch.complete(Unit)
            advanceUntilIdle()
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.InstantSending)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media before pre-processing success scenario`() = runTest {
        val sendFileResult = lambdaRecorder<File, FileInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            sendFileResult = sendFileResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor(
                processLatch = processLatch,
            ),
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            // Pre-processing finishes
            processLatch.complete(Unit)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media with pre-processing failure after user sends media`() = runTest {
        val room = FakeMatrixRoom()
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor().apply {
                givenResult(Result.failure(Exception()))
            },
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            // Pre-processing finishes
            processLatch.complete(Unit)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Failure::class.java)
        }
    }

    @Test
    fun `present - send media with pre-processing failure before user sends media`() = runTest {
        val room = FakeMatrixRoom()
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor().apply {
                givenResult(Result.failure(Exception()))
            },
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            // Pre-processing finishes
            processLatch.complete(Unit)
            advanceUntilIdle()
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.InstantSending)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Failure::class.java)
        }
    }

    @Test
    fun `present - cancel scenario`() = runTest {
        val onDoneListener = lambdaRecorder<Unit> { }
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createAttachmentsPreviewPresenter(
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.Cancel)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            deleteCallback.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send image with caption success scenario`() = runTest {
        val sendImageResult =
            lambdaRecorder<File, File?, ImageInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenImageResult()
        }
        val room = FakeMatrixRoom(
            sendImageResult = sendImageResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendImageResult.assertions().isCalledOnce().with(
                any(),
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send video with caption success scenario`() = runTest {
        val sendVideoResult =
            lambdaRecorder<File, File?, VideoInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenVideoResult()
        }
        val room = FakeMatrixRoom(
            sendVideoResult = sendVideoResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendVideoResult.assertions().isCalledOnce().with(
                any(),
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send audio with caption success scenario`() = runTest {
        val sendAudioResult =
            lambdaRecorder<File, AudioInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenAudioResult()
        }
        val room = FakeMatrixRoom(
            sendAudioResult = sendAudioResult,
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendAudioResult.assertions().isCalledOnce().with(
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media failure scenario`() = runTest {
        val failure = MediaPreProcessor.Failure(null)
        val sendFileResult = lambdaRecorder<File, FileInfo, String?, String?, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
            Result.failure(failure)
        }
        val room = FakeMatrixRoom(
            sendFileResult = sendFileResult,
        )
        val presenter = createAttachmentsPreviewPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            val failureState = awaitItem()
            assertThat(failureState.sendActionState).isEqualTo(SendActionState.Failure(failure))
            sendFileResult.assertions().isCalledOnce()
            failureState.eventSink(AttachmentsPreviewEvents.ClearSendState)
            val clearedState = awaitItem()
            assertThat(clearedState.sendActionState).isEqualTo(SendActionState.Idle)
        }
    }

    @Test
    fun `present - dismissing the progress dialog stops media upload`() = runTest {
        val presenter = createAttachmentsPreviewPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            initialState.eventSink(AttachmentsPreviewEvents.ClearSendState)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Idle)
        }
    }

    private fun createAttachmentsPreviewPresenter(
        localMedia: LocalMedia = aLocalMedia(
            uri = mockMediaUrl,
        ),
        room: MatrixRoom = FakeMatrixRoom(),
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        mediaPreProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        temporaryUriDeleter: TemporaryUriDeleter = FakeTemporaryUriDeleter(),
        onDoneListener: OnDoneListener = OnDoneListener { lambdaError() },
        mediaUploadOnSendQueueEnabled: Boolean = true,
        allowCaption: Boolean = true,
        showCaptionCompatibilityWarning: Boolean = true,
    ): AttachmentsPreviewPresenter {
        return AttachmentsPreviewPresenter(
            attachment = aMediaAttachment(localMedia),
            onDoneListener = onDoneListener,
            mediaSender = MediaSender(mediaPreProcessor, room, InMemorySessionPreferencesStore()),
            permalinkBuilder = permalinkBuilder,
            temporaryUriDeleter = temporaryUriDeleter,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.MediaUploadOnSendQueue.key to mediaUploadOnSendQueueEnabled,
                    FeatureFlags.MediaCaptionCreation.key to allowCaption,
                    FeatureFlags.MediaCaptionWarning.key to showCaptionCompatibilityWarning,
                ),
            )
        )
    }
}
