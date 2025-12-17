/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.knockrequests.impl.R
import io.element.android.features.knockrequests.impl.data.aKnockRequestPresentable
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnockRequestsBannerViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on view on single request invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setKnockRequestsBannerView(
                state = aKnockRequestsBannerState(
                    eventSink = eventsRecorder,
                ),
                onViewRequestsClick = it
            )
            rule.clickOn(R.string.screen_room_single_knock_request_view_button_title)
        }
    }

    @Test
    fun `clicking on view all when multiple requests invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setKnockRequestsBannerView(
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
            rule.clickOn(R.string.screen_room_multiple_knock_requests_view_all_button_title)
        }
    }

    @Test
    fun `clicking on accept on a single request emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>()
        rule.setKnockRequestsBannerView(
            state = aKnockRequestsBannerState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(KnockRequestsBannerEvents.AcceptSingleRequest)
    }

    @Test
    fun `clicking on dismiss emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsBannerEvents>()
        rule.setKnockRequestsBannerView(
            state = aKnockRequestsBannerState(
                eventSink = eventsRecorder,
            ),
        )
        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(KnockRequestsBannerEvents.Dismiss)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setKnockRequestsBannerView(
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
