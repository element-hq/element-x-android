/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val TESTED_MEDIA_INFO = anApkMediaInfo(
    senderId = A_USER_ID,
)

class MediaViewerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)

    @Test
    fun `present - initial state null Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state cannot show info`() = runTest {
        val presenter = createMediaViewerPresenter(
            canShowInfo = false,
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isFalse()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state Event from other`() = runTest {
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            room = FakeMatrixRoom(
                sessionId = A_SESSION_ID_2,
                canRedactOtherResult = { Result.success(false) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - download media success scenario`() = runTest {
        val presenter = createMediaViewerPresenter(
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
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
        val mediaActions = FakeLocalMediaActions()
        val snackbarDispatcher = SnackbarDispatcher()
        val presenter = createMediaViewerPresenter(
            localMediaActions = mediaActions,
            snackbarDispatcher = snackbarDispatcher,
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
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
        val presenter = createMediaViewerPresenter(
            matrixMediaLoader = matrixMediaLoader,
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
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

    @Test
    fun `present - delete media success scenario`() = runTest {
        val redactEventLambda = lambdaRecorder<EventOrTransactionId, String?, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.redactEventLambda = redactEventLambda
        }
        val onItemDeletedLambda = lambdaRecorder<Unit> { }
        val navigator = FakeMediaViewerNavigator(
            onItemDeletedLambda = onItemDeletedLambda,
        )

        val presenter = createMediaViewerPresenter(
            room = FakeMatrixRoom(
                liveTimeline = timeline,
                canRedactOwnResult = { Result.success(true) },
            ),
            mediaViewerNavigator = navigator,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            successState.eventSink(MediaViewerEvents.Delete(AN_EVENT_ID))
            redactEventLambda.assertions()
                .isCalledOnce()
                .with(
                    value(AN_EVENT_ID.toEventOrTransactionId()),
                    value(null),
                )
            onItemDeletedLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - view in timeline invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaViewerNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaViewerPresenter(
            mediaViewerNavigator = navigator,
            room = FakeMatrixRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.downloadedMedia).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.mediaInfo).isEqualTo(TESTED_MEDIA_INFO)
            val loadingState = awaitItem()
            assertThat(loadingState.downloadedMedia).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.downloadedMedia).isInstanceOf(AsyncData.Success::class.java)
            successState.eventSink(MediaViewerEvents.ViewInTimeline(AN_EVENT_ID))
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    private fun createMediaViewerPresenter(
        eventId: EventId? = null,
        matrixMediaLoader: FakeMatrixMediaLoader = FakeMatrixMediaLoader(),
        localMediaActions: FakeLocalMediaActions = FakeLocalMediaActions(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        canShowInfo: Boolean = true,
        mediaViewerNavigator: MediaViewerNavigator = FakeMediaViewerNavigator(),
        room: MatrixRoom = FakeMatrixRoom(
            liveTimeline = FakeTimeline(),
        ),
    ): MediaViewerPresenter {
        return MediaViewerPresenter(
            inputs = MediaViewerEntryPoint.Params(
                eventId = eventId,
                mediaInfo = TESTED_MEDIA_INFO,
                mediaSource = aMediaSource(),
                thumbnailSource = null,
                canShowInfo = canShowInfo,
            ),
            localMediaFactory = localMediaFactory,
            mediaLoader = matrixMediaLoader,
            localMediaActions = localMediaActions,
            snackbarDispatcher = snackbarDispatcher,
            navigator = mediaViewerNavigator,
            room = room,
        )
    }
}
