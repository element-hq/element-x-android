/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.invite.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeclineAndBlockViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setDeclineAndBlockView(
                aDeclineAndBlockState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on decline when enabled emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>()
        rule.setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(DeclineAndBlockEvents.Decline)
    }

    @Test
    fun `clicking on decline when disabled does not emit event`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>(expectEvents = false)
        rule.setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = false,
                reportRoom = false,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `clicking on block option emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>()
        rule.setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_decline_and_block_block_user_option_title)
        eventsRecorder.assertSingle(DeclineAndBlockEvents.ToggleBlockUser)
    }

    @Test
    fun `clicking on report room option emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>()
        rule.setDeclineAndBlockView(
            aDeclineAndBlockState(
                reportRoom = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_report_room)
        eventsRecorder.assertSingle(DeclineAndBlockEvents.ToggleReportRoom)
    }

    @Test
    fun `typing text in the reason field emits the expected Event`() {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvents>()
        rule.setDeclineAndBlockView(
            aDeclineAndBlockState(
                reportRoom = true,
                reportReason = "",
                eventSink = eventsRecorder,
            ),
        )
        rule.onNodeWithText("").performTextInput("Spam!")
        eventsRecorder.assertSingle(DeclineAndBlockEvents.UpdateReportReason("Spam!"))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setDeclineAndBlockView(
    state: DeclineAndBlockState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        DeclineAndBlockView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
