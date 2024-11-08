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
import io.element.android.libraries.matrix.api.core.ProgressCallback
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
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.mockk.mockk
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
    fun `present - send media success scenario`() = runTest {
        val sendFileResult = lambdaRecorder<File, FileInfo, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _ ->
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
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(0f))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(0.5f))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(1f))
            advanceUntilIdle()
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
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
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            advanceUntilIdle()
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
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing)
            advanceUntilIdle()
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
    fun `present - send media failure scenario`() = runTest {
        val failure = MediaPreProcessor.Failure(null)
        val sendFileResult = lambdaRecorder<File, FileInfo, ProgressCallback?, Result<FakeMediaUploadHandler>> { _, _, _ ->
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
            val loadingState = awaitItem()
            assertThat(loadingState.sendActionState).isEqualTo(SendActionState.Sending.Processing)
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
        onDoneListener: OnDoneListener = OnDoneListener {},
    ): AttachmentsPreviewPresenter {
        return AttachmentsPreviewPresenter(
            attachment = aMediaAttachment(localMedia),
            onDoneListener = onDoneListener,
            mediaSender = MediaSender(mediaPreProcessor, room, InMemorySessionPreferencesStore()),
            permalinkBuilder = permalinkBuilder,
            temporaryUriDeleter = temporaryUriDeleter,
        )
    }
}
