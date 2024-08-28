/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinnedMessagesBannerViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on the banner invoke expected callback`() {
        val eventsRecorder = EventsRecorder<PinnedMessagesBannerEvents>()
        val state = aLoadedPinnedMessagesBannerState(
            eventSink = eventsRecorder
        )
        val pinnedEventId = state.currentPinnedMessage.eventId
        ensureCalledOnceWithParam(pinnedEventId) { callback ->
            rule.setPinnedMessagesBannerView(
                state = state,
                onClick = callback
            )
            rule.onRoot().performClick()
            eventsRecorder.assertSingle(PinnedMessagesBannerEvents.MoveToNextPinned)
        }
    }

    @Test
    fun `clicking on view all emit the expected event`() {
        val eventsRecorder = EventsRecorder<PinnedMessagesBannerEvents>(expectEvents = true)
        val state = aLoadedPinnedMessagesBannerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setPinnedMessagesBannerView(
                state = state,
                onViewAllClick = callback
            )
            rule.clickOn(CommonStrings.screen_room_pinned_banner_view_all_button_title)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setPinnedMessagesBannerView(
    state: PinnedMessagesBannerState,
    onClick: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onViewAllClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PinnedMessagesBannerView(
            state = state,
            onClick = onClick,
            onViewAllClick = onViewAllClick
        )
    }
}
