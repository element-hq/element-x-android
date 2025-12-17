/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class TroubleshootNotificationsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `press menu back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<TroubleshootNotificationsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setTroubleshootNotificationsView(
                state = aTroubleshootNotificationsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on run test emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TroubleshootNotificationsEvents>()
        rule.setTroubleshootNotificationsView(
            aTroubleshootNotificationsState(
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Run tests").performClick()
        eventsRecorder.assertSingle(TroubleshootNotificationsEvents.StartTests)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on run test again emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TroubleshootNotificationsEvents>()
        rule.setTroubleshootNotificationsView(
            aTroubleshootNotificationsState(
                tests = listOf(
                    aTroubleshootTestStateFailure(
                        hasQuickFix = false
                    )
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Run tests again").performClick()
        eventsRecorder.assertList(
            listOf(
                TroubleshootNotificationsEvents.RetryFailedTests,
                TroubleshootNotificationsEvents.StartTests,
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on quick fix emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TroubleshootNotificationsEvents>()
        rule.setTroubleshootNotificationsView(
            aTroubleshootNotificationsState(
                tests = listOf(
                    aTroubleshootTestStateFailure(
                        hasQuickFix = true
                    )
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Attempt to fix").performClick()
        eventsRecorder.assertList(
            listOf(
                TroubleshootNotificationsEvents.RetryFailedTests,
                TroubleshootNotificationsEvents.QuickFix(0),
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setTroubleshootNotificationsView(
    state: TroubleshootNotificationsState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        TroubleshootNotificationsView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
