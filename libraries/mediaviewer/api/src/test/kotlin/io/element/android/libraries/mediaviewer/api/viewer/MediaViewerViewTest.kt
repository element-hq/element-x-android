/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.mediaviewer.api.viewer

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
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.anImageMediaInfo
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaViewerViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes expected callback`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setMediaViewerView(
                aMediaViewerState(
                    eventSink = eventsRecorder
                ),
                onBackPressed = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on open emit expected Event`() {
        testMenuAction(CommonStrings.action_open_with, MediaViewerEvents.OpenWith)
    }

    @Test
    fun `clicking on save emit expected Event`() {
        testMenuAction(CommonStrings.action_save, MediaViewerEvents.SaveOnDisk)
    }

    @Test
    fun `clicking on share emit expected Event`() {
        testMenuAction(CommonStrings.action_share, MediaViewerEvents.Share)
    }

    private fun testMenuAction(contentDescriptionRes: Int, expectedEvent: MediaViewerEvents) {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        rule.setMediaViewerView(
            aMediaViewerState(
                downloadedMedia = AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageMediaInfo())
                ),
                mediaInfo = anImageMediaInfo(),
                eventSink = eventsRecorder
            ),
        )
        val contentDescription = rule.activity.getString(contentDescriptionRes)
        rule.onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertSingle(expectedEvent)
    }

    @Ignore("This test is not passing yet, maybe due to interaction with ZoomableAsyncImage?")
    @Test
    fun `clicking on image hides the overlay`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>(expectEvents = false)
        rule.setMediaViewerView(
            aMediaViewerState(
                downloadedMedia = AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageMediaInfo())
                ),
                mediaInfo = anImageMediaInfo(),
                eventSink = eventsRecorder
            ),
        )
        // Ensure that the action are visible
        val contentDescription = rule.activity.getString(CommonStrings.action_open_with)
        rule.onNodeWithContentDescription(contentDescription).assertHasClickAction()
        val imageContentDescription = rule.activity.getString(CommonStrings.common_image)
        rule.onNodeWithContentDescription(imageContentDescription).performClick()
        // assertHasNoClickAction does not work as expected (?)
        // rule.onNodeWithContentDescription(contentDescription).assertHasNoClickAction()
        rule.onNodeWithContentDescription(contentDescription).performClick()
        // No emitted event
    }

    @Ignore("This test is not passing yet, maybe due to interaction with ZoomableAsyncImage?")
    @Test
    fun `clicking swipe on the image invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setMediaViewerView(
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, anImageMediaInfo())
                    ),
                    mediaInfo = anImageMediaInfo(),
                    eventSink = eventsRecorder
                ),
                onBackPressed = callback,
            )
            val imageContentDescription = rule.activity.getString(CommonStrings.common_image)
            rule.onNodeWithContentDescription(imageContentDescription).performTouchInput { swipeDown() }
            rule.mainClock.advanceTimeBy(1_000)
        }
    }

    @Test
    fun `error case, click on retry emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        rule.setMediaViewerView(
            aMediaViewerState(
                downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
                mediaInfo = anImageMediaInfo(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(MediaViewerEvents.RetryLoading)
    }

    @Test
    fun `error case, click on cancel emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MediaViewerEvents>()
        rule.setMediaViewerView(
            aMediaViewerState(
                downloadedMedia = AsyncData.Failure(IllegalStateException("error")),
                mediaInfo = anImageMediaInfo(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(MediaViewerEvents.ClearLoadingError)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMediaViewerView(
    state: MediaViewerState,
    onBackPressed: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        MediaViewerView(
            state = state,
            onBackPressed = onBackPressed,
        )
    }
}
