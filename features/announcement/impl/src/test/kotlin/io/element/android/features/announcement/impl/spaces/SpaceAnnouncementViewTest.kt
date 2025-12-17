/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpaceAnnouncementViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back sends a SpaceAnnouncementEvents`() {
        val eventsRecorder = EventsRecorder<SpaceAnnouncementEvents>()
        rule.setSpaceAnnouncementView(
            aSpaceAnnouncementState(
                eventSink = eventsRecorder,
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(SpaceAnnouncementEvents.Continue)
    }

    @Test
    fun `clicking on Continue sends a SpaceAnnouncementEvents`() {
        val eventsRecorder = EventsRecorder<SpaceAnnouncementEvents>()
        rule.setSpaceAnnouncementView(
            aSpaceAnnouncementState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(SpaceAnnouncementEvents.Continue)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setSpaceAnnouncementView(
    state: SpaceAnnouncementState,
) {
    setContent {
        SpaceAnnouncementView(
            state = state,
        )
    }
}
