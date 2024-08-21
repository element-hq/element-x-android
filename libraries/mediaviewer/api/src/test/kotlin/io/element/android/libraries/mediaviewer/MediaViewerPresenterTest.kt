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

package io.element.android.libraries.mediaviewer

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.mediaviewer.api.local.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.viewer.MediaViewerEvents
import io.element.android.libraries.mediaviewer.api.viewer.MediaViewerNode
import io.element.android.libraries.mediaviewer.api.viewer.MediaViewerPresenter
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val TESTED_MEDIA_INFO = anApkMediaInfo()

class MediaViewerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)

    @Test
    fun `present - download media success scenario`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val presenter = createMediaViewerPresenter(matrixMediaLoader, mediaActions)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            assertThat(state.downloadedMedia).isEqualTo(AsyncData.Uninitialized)
            assertThat(state.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            state = awaitItem()
            val successData = state.downloadedMedia.dataOrNull()
            assertThat(state.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    @Test
    fun `present - check all actions`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val snackbarDispatcher = SnackbarDispatcher()
        val presenter = createMediaViewerPresenter(matrixMediaLoader, mediaActions, snackbarDispatcher)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            assertThat(state.downloadedMedia).isEqualTo(AsyncData.Uninitialized)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            // no state changes while media is loading
            state.eventSink(MediaViewerEvents.OpenWith)
            state.eventSink(MediaViewerEvents.Share)
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            // Should succeed without change of state
            state.eventSink(MediaViewerEvents.OpenWith)
            // Should succeed without change of state
            state.eventSink(MediaViewerEvents.Share)
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            snackbarDispatcher.clear()
            assertThat(awaitItem().snackbarMessage).isNull()

            // Check failures
            mediaActions.shouldFail = true
            state.eventSink(MediaViewerEvents.OpenWith)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            snackbarDispatcher.clear()
            assertThat(awaitItem().snackbarMessage).isNull()
            state.eventSink(MediaViewerEvents.Share)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
            snackbarDispatcher.clear()
            assertThat(awaitItem().snackbarMessage).isNull()
            state.eventSink(MediaViewerEvents.SaveOnDisk)
            state = awaitItem()
            assertThat(state.snackbarMessage).isNotNull()
        }
    }

    @Test
    fun `present - download media failure then retry with success scenario`() = runTest {
        val matrixMediaLoader = FakeMatrixMediaLoader()
        val mediaActions = FakeLocalMediaActions()
        val presenter = createMediaViewerPresenter(matrixMediaLoader, mediaActions)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            matrixMediaLoader.shouldFail = true
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            val failureState = awaitItem()
            assertThat(failureState.downloadedMedia).isInstanceOf(AsyncData.Failure::class.java)
            matrixMediaLoader.shouldFail = false
            failureState.eventSink(MediaViewerEvents.RetryLoading)
            // There is one recomposition because of the retry mechanism
            skipItems(1)
            val retryLoadingState = awaitItem()
            assertThat(retryLoadingState.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            val successData = successState.downloadedMedia.dataOrNull()
            assertThat(successState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successData).isNotNull()
        }
    }

    private fun createMediaViewerPresenter(
        matrixMediaLoader: FakeMatrixMediaLoader,
        localMediaActions: FakeLocalMediaActions,
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        canShare: Boolean = true,
        canDownload: Boolean = true,
    ): MediaViewerPresenter {
        return MediaViewerPresenter(
            inputs = MediaViewerNode.Inputs(
                mediaInfo = TESTED_MEDIA_INFO,
                mediaSource = aMediaSource(),
                thumbnailSource = null,
                canShare = canShare,
                canDownload = canDownload,
            ),
            localMediaFactory = localMediaFactory,
            mediaLoader = matrixMediaLoader,
            localMediaActions = localMediaActions,
            snackbarDispatcher = snackbarDispatcher,
        )
    }
}
