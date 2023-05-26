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

package io.element.android.features.messages.media.viewer

import androidx.media3.common.MimeTypes
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.media.viewer.MediaViewerEvents
import io.element.android.features.messages.impl.media.viewer.MediaViewerNode
import io.element.android.features.messages.impl.media.viewer.MediaViewerPresenter
import io.element.android.features.messages.media.FakeLocalMediaFactory
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.FAKE_DELAY_IN_MS
import io.element.android.libraries.matrix.test.media.FakeMediaLoader
import io.element.android.libraries.matrix.test.media.aMediaSource
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val TESTED_MIME_TYPE = MimeTypes.IMAGE_JPEG
private const val TESTED_MEDIA_NAME = "MediaName"

class MediaViewerPresenterTest {

    private val localMediaFactory = FakeLocalMediaFactory()
    private val mediaLoader = FakeMediaLoader()

    @Test
    fun `present - download media success scenario`() = runTest {
        val presenter = aMediaViewerPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(Async.Uninitialized)
            assertThat(initialState.name).isEqualTo(TESTED_MEDIA_NAME)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            testScheduler.advanceTimeBy(FAKE_DELAY_IN_MS)
            val successState = awaitItem()
            val successData = successState.downloadedMedia.dataOrNull()
            assertThat(successState.downloadedMedia).isInstanceOf(Async.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    @Test
    fun `present - download media failure then retry with success scenario`() = runTest {
        val presenter = aMediaViewerPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            mediaLoader.shouldFail = true
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(Async.Uninitialized)
            assertThat(initialState.name).isEqualTo(TESTED_MEDIA_NAME)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            testScheduler.advanceTimeBy(FAKE_DELAY_IN_MS)
            val failureState = awaitItem()
            assertThat(failureState.downloadedMedia).isInstanceOf(Async.Failure::class.java)
            mediaLoader.shouldFail = false
            failureState.eventSink(MediaViewerEvents.RetryLoading)
            //There is one recomposition because of the retry mechanism
            skipItems(1)
            val retryLoadingState = awaitItem()
            assertThat(retryLoadingState.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            testScheduler.advanceTimeBy(FAKE_DELAY_IN_MS)
            val successState = awaitItem()
            val successData = successState.downloadedMedia.dataOrNull()
            assertThat(successState.downloadedMedia).isInstanceOf(Async.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    private fun aMediaViewerPresenter(mimeType: String = TESTED_MIME_TYPE): MediaViewerPresenter {
        return MediaViewerPresenter(
            inputs = MediaViewerNode.Inputs(
                name = TESTED_MEDIA_NAME,
                mediaSource = aMediaSource(),
                mimeType = mimeType,
                thumbnailSource = null
            ),
            localMediaFactory = localMediaFactory,
            mediaLoader = mediaLoader
        )
    }
}
