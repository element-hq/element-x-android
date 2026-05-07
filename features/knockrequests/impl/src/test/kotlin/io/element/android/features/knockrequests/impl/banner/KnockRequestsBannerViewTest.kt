/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.knockrequests.impl.banner

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.knockrequests.impl.R
import io.element.android.features.knockrequests.impl.data.aKnockRequestPresentable
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnockRequestsBannerViewTest {
    @Test
    fun `clicking on view on single request invoke the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>(expectEvents = false)
        ensureCalledOnce {
            setKnockRequestsBannerView(
                state = aKnockRequestsBannerState(
                    eventSink = eventsRecorder,
                ),
                onViewRequestsClick = it
            )
            clickOn(R.string.screen_room_single_knock_request_view_button_title)
        }
    }

    @Test
    fun `clicking on view all when multiple requests invoke the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>(expectEvents = false)
        ensureCalledOnce {
            setKnockRequestsBannerView(
                state = aKnockRequestsBannerState(
                    knockRequests = listOf(
                        aKnockRequestPresentable(displayName = "Alice"),
                        aKnockRequestPresentable(displayName = "Bob"),
                        aKnockRequestPresentable(displayName = "Charlie")
                    ),
                    eventSink = eventsRecorder,
                ),
                onViewRequestsClick = it
            )
            clickOn(R.string.screen_room_multiple_knock_requests_view_all_button_title)
        }
    }

    @Test
    fun `clicking on accept on a single request emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>()
        setKnockRequestsBannerView(
            state = aKnockRequestsBannerState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(KnockRequestsBannerEvents.AcceptSingleRequest)
    }

    @Test
    fun `clicking on dismiss emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>()
        setKnockRequestsBannerView(
            state = aKnockRequestsBannerState(
                eventSink = eventsRecorder,
            ),
        )
        val close = activity!!.getString(CommonStrings.action_close)
        onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(KnockRequestsBannerEvents.Dismiss)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setKnockRequestsBannerView(
    state: KnockRequestsBannerState,
    onViewRequestsClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        KnockRequestsBannerView(
            state = state,
            onViewRequestsClick = onViewRequestsClick,
        )
    }
}
