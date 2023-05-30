/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.attachments

import androidx.media3.common.MimeTypes
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.fixtures.aLocalMedia
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewEvents
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewPresenter
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.FAKE_DELAY_IN_MS
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AttachmentsPreviewPresenterTest {

    private val mediaPreProcessor = FakeMediaPreProcessor()

    @Test
    fun `present - send media success scenario`() = runTest {
        val room = FakeMatrixRoom()
        val presenter = anAttachmentsPreviewPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(Async.Uninitialized)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            val loadingState = awaitItem()
            assertThat(loadingState.sendActionState).isEqualTo(Async.Loading<Unit>())
            testScheduler.advanceTimeBy(FAKE_DELAY_IN_MS)
            runCurrent()
            val successState = awaitItem()
            assertThat(successState.sendActionState).isEqualTo(Async.Success(Unit))
            assertThat(room.sendMediaCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - send media failure scenario`() = runTest {
        val room = FakeMatrixRoom()
        val failure = MediaPreProcessor.Failure(null)
        room.givenSendMediaResult(Result.failure(failure))
        val presenter = anAttachmentsPreviewPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(Async.Uninitialized)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            val loadingState = awaitItem()
            assertThat(loadingState.sendActionState).isEqualTo(Async.Loading<Unit>())
            testScheduler.advanceTimeBy(FAKE_DELAY_IN_MS)
            runCurrent()
            val failureState = awaitItem()
            assertThat(failureState.sendActionState).isEqualTo(Async.Failure<Unit>(failure))
            assertThat(room.sendMediaCount).isEqualTo(0)
            failureState.eventSink(AttachmentsPreviewEvents.ClearSendState)
            val clearedState = awaitItem()
            assertThat(clearedState.sendActionState).isEqualTo(Async.Uninitialized)
        }
    }

    private fun TestScope.anAttachmentsPreviewPresenter(
        localMedia: LocalMedia = aLocalMedia(mimeType = MimeTypes.IMAGE_JPEG),
        room: MatrixRoom = FakeMatrixRoom()
    ): AttachmentsPreviewPresenter {
        return AttachmentsPreviewPresenter(
            attachment = Attachment.Media(localMedia, compressIfPossible = false),
            mediaSender = MediaSender(mediaPreProcessor, room),
            dispatchers = testCoroutineDispatchers(testScheduler, useUnconfinedTestDispatcher = false),
        )
    }
}
