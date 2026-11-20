/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.signedout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class SignedOutViewTest : RobolectricTest() {
    @Test
    @Config(qualifiers = "h1024dp")
    fun `the signed out user is displayed`() = runAndroidComposeUiTest {
        setSignedOutView(
            aSignedOutState(
                signedOutSession = aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
                eventSink = EventsRecorder(expectEvents = false),
            ),
        )
        onNodeWithText("Alice").assertIsDisplayed()
        onNodeWithText("@alice:server.org").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `the signed out user id is displayed when there is no display name`() = runAndroidComposeUiTest {
        setSignedOutView(
            aSignedOutState(
                signedOutSession = aMatrixUser(id = "@alice:server.org", displayName = null),
                eventSink = EventsRecorder(expectEvents = false),
            ),
        )
        onNodeWithText("@alice:server.org").assertIsDisplayed()
    }

    @Test
    fun `clicking on OK sends a Submit event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<SignedOutEvent>()
        setSignedOutView(
            aSignedOutState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(SignedOutEvent.Submit)
    }

    @Test
    fun `pressing back sends a Submit event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<SignedOutEvent>()
        setSignedOutView(
            aSignedOutState(
                eventSink = eventsRecorder,
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(SignedOutEvent.Submit)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setSignedOutView(
    state: SignedOutState,
) {
    setContent {
        SignedOutView(
            state = state,
        )
    }
}
