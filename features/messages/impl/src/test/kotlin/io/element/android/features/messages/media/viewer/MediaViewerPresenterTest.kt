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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.media.viewer

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.media.local.aFileInfo
import io.element.android.features.messages.impl.media.viewer.MediaViewerEvents
import io.element.android.features.messages.impl.media.viewer.MediaViewerNode
import io.element.android.features.messages.impl.media.viewer.MediaViewerPresenter
import io.element.android.features.messages.media.FakeLocalMediaActions
import io.element.android.features.messages.media.FakeLocalMediaFactory
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.matrix.test.media.FakeMediaLoader
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val TESTED_MEDIA_INFO = aFileInfo()

class MediaViewerPresenterTest {

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)

    @Test
    fun `present - download media success scenario`() = runTest {
        val mediaLoader = FakeMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val presenter = aMediaViewerPresenter(mediaLoader, mediaActions)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            assertThat(state.downloadedMedia).isEqualTo(Async.Uninitialized)
            assertThat(state.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            state = awaitItem()
            val successData = state.downloadedMedia.dataOrNull()
            assertThat(state.downloadedMedia).isInstanceOf(Async.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    @Test
    fun `present - check all actions `() = runTest {
        val mediaLoader = FakeMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val presenter = aMediaViewerPresenter(mediaLoader, mediaActions)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            assertThat(state.downloadedMedia).isEqualTo(Async.Uninitialized)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            // no state changes while media is loading
            state.eventSink(MediaViewerEvents.OpenWith)
            state.eventSink(MediaViewerEvents.Share)
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(Async.Success::class.java)
            // Should succeed without change of state
            state.eventSink(MediaViewerEvents.OpenWith)
            // Should succeed without change of state
            state.eventSink(MediaViewerEvents.Share)
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            state = awaitItem()
            assertThat(state.snackbarMessage).isNull()

            // Check failures
            mediaActions.shouldFail = true
            state.eventSink(MediaViewerEvents.OpenWith)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            state = awaitItem()
            assertThat(state.snackbarMessage).isNull()
            state.eventSink(MediaViewerEvents.Share)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            state = awaitItem()
            assertThat(state.snackbarMessage).isNull()
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            state = awaitItem()
            assertThat(state.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - download media failure then retry with success scenario`() = runTest {
        val mediaLoader = FakeMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val presenter = aMediaViewerPresenter(mediaLoader, mediaActions)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            mediaLoader.shouldFail = true
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(Async.Uninitialized)
            assertThat(initialState.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            val failureState = awaitItem()
            assertThat(failureState.downloadedMedia).isInstanceOf(Async.Failure::class.java)
            mediaLoader.shouldFail = false
            failureState.eventSink(MediaViewerEvents.RetryLoading)
            //There is one recomposition because of the retry mechanism
            skipItems(1)
            val retryLoadingState = awaitItem()
            assertThat(retryLoadingState.downloadedMedia).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            val successData = successState.downloadedMedia.dataOrNull()
            assertThat(successState.downloadedMedia).isInstanceOf(Async.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    private fun aMediaViewerPresenter(
        mediaLoader: FakeMediaLoader,
        localMediaActions: FakeLocalMediaActions,
    ): MediaViewerPresenter {
        return MediaViewerPresenter(
            inputs = MediaViewerNode.Inputs(
                mediaInfo = TESTED_MEDIA_INFO,
                mediaSource = aMediaSource(),
                thumbnailSource = null
            ),
            localMediaFactory = localMediaFactory,
            mediaLoader = mediaLoader,
            localMediaActions = localMediaActions,
            snackbarDispatcher = SnackbarDispatcher()
        )
    }
}
