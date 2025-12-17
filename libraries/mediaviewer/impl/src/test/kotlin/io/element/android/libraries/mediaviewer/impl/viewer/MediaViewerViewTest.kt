/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.impl.details.aMediaDetailsBottomSheetState
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.setSafeContent
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MediaViewerViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `clicking on back invokes expected callback`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMediaViewerView(
                state = state,
                onBackClick = callback,
            )
            rule.pressBack()
        }
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `clicking on open emit expected Event`() {
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Success(aLocalMedia(uri = mockMediaUrl)),
        )
        testMenuAction(
            data,
            CommonStrings.action_open_with,
            MediaViewerEvents.OpenWith(data),
        )
    }

    @Test
    fun `clicking on info emit expected Event`() {
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Success(aLocalMedia(uri = mockMediaUrl)),
        )
        testMenuAction(
            data,
            CommonStrings.a11y_view_details,
            MediaViewerEvents.OpenInfo(data),
        )
    }

    private fun testMenuAction(
        data: MediaViewerPageData.MediaViewerData,
        contentDescriptionRes: Int,
        expectedEvent: MediaViewerEvents,
    ) {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        rule.setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )
        val contentDescription = rule.activity.getString(contentDescriptionRes)
        rule.onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(data),
                expectedEvent,
            )
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on save emit expected Event`() {
        val data = aMediaViewerPageData()
        testBottomSheetAction(
            data,
            CommonStrings.action_save,
            MediaViewerEvents.SaveOnDisk(data),
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on share emit expected Event`() {
        val data = aMediaViewerPageData()
        testBottomSheetAction(
            data,
            CommonStrings.action_share,
            MediaViewerEvents.Share(data),
        )
    }

    private fun testBottomSheetAction(
        data: MediaViewerPageData.MediaViewerData,
        contentDescriptionRes: Int,
        expectedEvent: MediaViewerEvents,
    ) {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        rule.setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                mediaBottomSheetState = aMediaDetailsBottomSheetState(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(contentDescriptionRes)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(data),
                expectedEvent,
            )
        )
    }

    @Test
    fun `clicking on image hides the overlay`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        rule.setMediaViewerView(
            state = state,
        )
        // Ensure that the action are visible
        val contentDescription = rule.activity.getString(CommonStrings.action_open_with)
        rule.onNodeWithContentDescription(contentDescription)
            .assertExists()
            .assertHasClickAction()
        val imageContentDescription = rule.activity.getString(CommonStrings.common_image)
        rule.onNodeWithContentDescription(imageContentDescription).performClick()
        // Give time for the animation (? since even by removing AnimatedVisibility it still fails)
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithContentDescription(contentDescription)
            .assertDoesNotExist()
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `clicking swipe on the image invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        val state = aMediaViewerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMediaViewerView(
                state = state,
                onBackClick = callback,
            )
            val imageContentDescription = rule.activity.getString(CommonStrings.common_image)
            rule.onNodeWithContentDescription(imageContentDescription).performTouchInput { swipeDown(startY = centerY) }
            rule.mainClock.advanceTimeBy(1_000)
        }
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(state.listData.first() as MediaViewerPageData.MediaViewerData),
            )
        )
    }

    @Test
    fun `error case, click on retry emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
        )
        rule.setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(data),
                MediaViewerEvents.LoadMedia(data),
            )
        )
    }

    @Test
    fun `error case, click on cancel emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        val data = aMediaViewerPageData(
            downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
        )
        rule.setMediaViewerView(
            aMediaViewerState(
                listData = listOf(data),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertList(
            listOf(
                MediaViewerEvents.OnNavigateTo(0),
                MediaViewerEvents.LoadMedia(data),
                MediaViewerEvents.ClearLoadingError(data)
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMediaViewerView(
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
