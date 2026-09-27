/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.impl.details.aMediaBottomSheetStateDetails
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Test
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds

class MediaViewerViewTest : RobolectricTest() {
    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `clicking on back invokes expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            setMediaViewerView(
                state = state,
                onBackClick = callback,
            )

            // Wait for enough time for the onVisibilityChanged modifier to trigger
            mainClock.advanceTimeBy(200)

            pressBack()
        }
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `clicking on info emits expected Event`() {
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Success(aLocalMedia(uri = mockMediaUrl)),
        )
        testMenuAction(
            data,
            CommonStrings.a11y_view_details,
            MediaViewerEvent.OpenInfo(data),
        )
    }

    @Test
    fun `clicking on top action share emits expected Event`() {
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Success(aLocalMedia(uri = mockMediaUrl)),
        )
        testMenuAction(
            data,
            CommonStrings.action_share,
            MediaViewerEvent.Share(data),
        )
    }

    @Test
    fun `clicking on top action download emits expected Event`() {
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Success(aLocalMedia(uri = mockMediaUrl)),
        )
        testMenuAction(
            data,
            CommonStrings.action_download,
            MediaViewerEvent.SaveOnDisk(data),
        )
    }

    private fun testMenuAction(
        data: MediaViewerPageData.MediaViewerData,
        @StringRes contentDescriptionRes: Int,
        expectedEvent: MediaViewerEvent,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )

        // Wait for enough time for the onVisibilityChanged modifier to trigger
        mainClock.advanceTimeBy(200)

        val contentDescription = activity!!.getString(contentDescriptionRes)
        onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(data),
                expectedEvent,
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on open in emits expected Event`() {
        val data = aMediaViewerPageData()
        testBottomSheetAction(
            data,
            CommonStrings.action_open_with,
            MediaViewerEvent.OpenWith(data),
        )
    }

    private fun testBottomSheetAction(
        data: MediaViewerPageData.MediaViewerData,
        @StringRes textRes: Int,
        expectedEvent: MediaViewerEvent,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                mediaBottomSheetState = aMediaBottomSheetStateDetails(),
                eventSink = eventsRecorder
            ),
        )
        clickOn(textRes)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(data),
                expectedEvent,
            )
        )
    }

    @Test
    fun `clicking on image hides the overlay`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        setMediaViewerView(
            state = state,
        )
        // Ensure that the action are visible
        val resources = activity!!.resources
        val contentDescription = resources.getString(CommonStrings.action_share)
        onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertHasClickAction()
        val imageContentDescription = resources.getString(CommonStrings.common_image)
        onNodeWithContentDescription(imageContentDescription).performClick()
        // Give time for the animation (? since even by removing AnimatedVisibility it still fails)
        mainClock.advanceTimeBy(1_000)
        onNodeWithContentDescription(contentDescription)
            .assertDoesNotExist()
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `clicking swipe on the image invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            setMediaViewerView(
                state = state,
                onBackClick = callback,
            )
            val imageContentDescription = activity!!.getString(CommonStrings.common_image)
            onNodeWithContentDescription(imageContentDescription).performTouchInput { swipeDown(startY = centerY) }
            mainClock.advanceTimeBy(1_000)
        }
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `error case, click on retry emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
        )
        setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )

        // Wait for enough time for the onVisibilityChanged modifier to trigger
        mainClock.advanceTimeBy(200)

        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(data),
                MediaViewerEvent.LoadMedia(data),
            )
        )
    }

    @Test
    fun `error case, click on cancel emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
        )
        setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )

        // Wait for enough time for the onVisibilityChanged modifier to trigger
        mainClock.advanceTimeBy(200)

        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMedia(data),
                MediaViewerEvent.ClearLoadingError(data)
            )
        )
    }

    @Test
    fun `loading event after an error triggers load more Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaViewerEvent>()
        val states = listOf(
            aMediaViewerState(
                listData = listOf(aMediaViewerPageDataLoading(timestamp = 0L)),
                eventSink = eventsRecorder,
            ),
            aMediaViewerState(
                listData = listOf(MediaViewerPageData.Failure(IllegalStateException("error"))),
                eventSink = eventsRecorder,
            ),
            aMediaViewerState(
                listData = listOf(aMediaViewerPageDataLoading(timestamp = 0L)),
                eventSink = eventsRecorder,
            ),
            // This one should be ignored since it has the same timestamp as the last one, it should not trigger a recomposition
            aMediaViewerState(
                listData = listOf(aMediaViewerPageDataLoading(timestamp = 0L)),
                eventSink = eventsRecorder,
            ),
        )
        setSafeContent {
            // Iterate over the states with a delay to give the view some time to trigger the `LoadMore` Event
            var state by remember { mutableStateOf(states.first()) }
            LaunchedEffect(Unit) {
                val iterator = states.iterator()
                while (iterator.hasNext()) {
                    delay(200.milliseconds)
                    state = iterator.next()
                }
            }
            MediaViewerView(
                state = state,
                textFileViewer = { _, _ -> },
                onBackClick = EnsureNeverCalled(),
                audioFocus = null,
            )
        }

        // Advance time to let the states update
        mainClock.advanceTimeBy(3_000)

        // `LoadMore` should be called twice, once for the first loading state, and once for the second one even though they have the same timestamp because
        // of the intermediate error state.
        // The third one will be ignored since it has the same timestamp as the second one and it'll be discarded by the Compose's equality diffing.
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.LoadMore(direction = Timeline.PaginationDirection.BACKWARDS),
                MediaViewerEvent.LoadMore(direction = Timeline.PaginationDirection.BACKWARDS),
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setMediaViewerView(
    state: MediaViewerState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setSafeContent {
        MediaViewerView(
            state = state,
            audioFocus = null,
            textFileViewer = { _, _ -> },
            onBackClick = onBackClick,
        )
    }
}
