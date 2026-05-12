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
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.impl.details.aMediaBottomSheetStateDetails
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.setSafeContent
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MediaViewerViewTest {
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
            pressBack()
        }
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.OnNavigateTo(0),
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
        val contentDescription = activity!!.getString(contentDescriptionRes)
        onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.OnNavigateTo(0),
                MediaViewerEvent.LoadMedia(data),
                expectedEvent,
            )
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on download emits expected Event`() {
        val data = aMediaViewerPageData()
        testBottomSheetAction(
            data,
            CommonStrings.action_download,
            MediaViewerEvent.SaveOnDisk(data),
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on share emits expected Event`() {
        val data = aMediaViewerPageData()
        testBottomSheetAction(
            data,
            CommonStrings.action_share,
            MediaViewerEvent.Share(data),
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
                MediaViewerEvent.OnNavigateTo(0),
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
                MediaViewerEvent.OnNavigateTo(0),
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
                MediaViewerEvent.OnNavigateTo(0),
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
        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.OnNavigateTo(0),
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
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvent.OnNavigateTo(0),
                MediaViewerEvent.LoadMedia(data),
                MediaViewerEvent.ClearLoadingError(data)
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
