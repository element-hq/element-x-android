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
import io.element.android.features.messages.impl.attachments.preview.SendActionState
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AttachmentsPreviewPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mediaPreProcessor = FakeMediaPreProcessor()
    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `present - send media success scenario`() = runTest {
        val sendMediaResult = lambdaRecorder<ProgressCallback?, Result<FakeMediaUploadHandler>> {
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            progressCallbackValues = listOf(
                Pair(0, 10),
                Pair(5, 10),
                Pair(10, 10)
            ),
            sendMediaResult = sendMediaResult,
        )
        val presenter = createAttachmentsPreviewPresenter(room = room)
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
            val successState = awaitItem()
            assertThat(successState.sendActionState).isEqualTo(SendActionState.Done)
            sendMediaResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media failure scenario`() = runTest {
        val failure = MediaPreProcessor.Failure(null)
        val sendMediaResult = lambdaRecorder<ProgressCallback?, Result<FakeMediaUploadHandler>> {
            Result.failure(failure)
        }
        val room = FakeMatrixRoom(
            sendMediaResult = sendMediaResult,
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
            sendMediaResult.assertions().isCalledOnce()
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
        room: MatrixRoom = FakeMatrixRoom()
    ): AttachmentsPreviewPresenter {
        return AttachmentsPreviewPresenter(
            attachment = Attachment.Media(localMedia, compressIfPossible = false),
            mediaSender = MediaSender(mediaPreProcessor, room)
        )
    }
}
